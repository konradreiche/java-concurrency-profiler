package de.fu.profiler.model;

import java.io.IOException;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.table.TableModel;

import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

/**
 * Models the state of the profiler itself.
 * 
 * @author Konrad Johannes Reiche
 * 
 */
public class ProfilerModel extends Observable {

	/**
	 * All available JVMs.
	 */
	final Map<Integer, JVM> IDsToJVMs;

	/**
	 * The table model for displaying the threads of the JVM.
	 */
	TableModel tableModel;

	/**
	 * The dataset on which the thread state pie chart is based on.
	 */
	PieDataset threadPieDataset;

	/**
	 * The currently inspected JVM;
	 */
	JVM currentJVM;

	/**
	 * At the start of the profiler all available JVMs are read and listed.
	 * 
	 * @throws IOException
	 *             if an I/O error occurs when opening the socket.
	 */
	public ProfilerModel() throws IOException {
		super();
		this.IDsToJVMs = new ConcurrentHashMap<Integer, JVM>();
		this.tableModel = new ThreadTableModel(threadPieDataset);		
		initializePieDataset();
		initializeJVMs();
	}

	public Map<Integer, JVM> getIDsToJVMs() {
		return IDsToJVMs;
	}

	private void initializePieDataset() {

		threadPieDataset = new DefaultPieDataset();
		((DefaultPieDataset) threadPieDataset).setValue("New", 0);
		((DefaultPieDataset) threadPieDataset).setValue("Terminated", 0);
		((DefaultPieDataset) threadPieDataset).setValue("Runnable", 0);
		((DefaultPieDataset) threadPieDataset).setValue("Blocked", 0);
		((DefaultPieDataset) threadPieDataset).setValue("Waiting", 0);
		((DefaultPieDataset) threadPieDataset).setValue("Timed Waiting", 0);
	}
	
	private void initializeJVMs() {
		
		for (VirtualMachineDescriptor vmd : VirtualMachine.list()) {
			IDsToJVMs.put(Integer.parseInt(vmd.id()),
					new JVM(Integer.parseInt(vmd.id()), vmd.displayName()));
		}
		
		setChanged();
	}

	public TableModel getTableModel() {
		return tableModel;
	}

	public PieDataset getThreadPieDataset() {
		return threadPieDataset;
	}

	public void addThreadInfo(int pid, ThreadInfo threadInfo) {
		IDsToJVMs.get(pid).addThread(threadInfo);
		setChanged();
		notifyObservers();
	}
	
	public void setThreadInfoState(int pid, ThreadInfo threadInfo, String state) {
		IDsToJVMs.get(pid).getThread(threadInfo.getId()).setState(state);
		setChanged();
		notifyObservers();
	}
	
	public void setThreadInfoMonitorStatus(int pid, ThreadInfo threadInfo, String status) {
		IDsToJVMs.get(pid).getThread(threadInfo.getId()).changeMonitorStatus(status);
		setChanged();
		notifyObservers();
	}

	public JVM getCurrentJVM() {
		return currentJVM;
	}

	public void setCurrentJVM(JVM currentJVM) {
		this.currentJVM = currentJVM;
	}
	
	

}