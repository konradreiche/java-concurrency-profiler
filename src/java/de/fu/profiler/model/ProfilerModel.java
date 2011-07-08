package de.fu.profiler.model;

import java.io.IOException;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import de.fu.profiler.service.Message;
import de.fu.profiler.service.MethodMessage;
import de.fu.profiler.service.MonitorMessage;
import de.fu.profiler.service.ThreadMessage;

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
	final Map<JVM, ThreadTableModel> threadTableModels;
	final Map<JVM, LockTableModel> lockTableModels;
	final Map<JVM, ThreadStatsTableModel> threadStatsTableModels;
	final Map<JVM, StackTracesTree> notifyWaitStackTracesTrees;
	final Map<JVM, StackTracesTree> lockStackTracesTrees;
	final Map<JVM, MonitorLogTableModel> notifyWaitTables;
	final Map<JVM, MethodProfilingTableModel> timeTableModels;
	final Map<JVM, DefaultPieDataset> threadStatePieDataset;
	final Map<JVM, DefaultCategoryDataset> threadStateOverTimeDataset;

	final ConcurrentMap<JVM, ConcurrentMap<Long, Message>> threadMessageHistory;
	final ConcurrentMap<JVM, ConcurrentMap<Long, Message>> monitorMessageHistory;
	final ConcurrentMap<JVM, ConcurrentMap<Long, Message>> methodMessageHistory;


	/**
	 * At the start of the profiler all available JVMs are read and listed.
	 * 
	 * @throws IOException
	 *             if an I/O error occurs when opening the socket.
	 */
	public ProfilerModel() throws IOException {
		super();
		IDsToJVMs = new ConcurrentSkipListMap<Integer, JVM>();
		threadTableModels = new ConcurrentSkipListMap<JVM, ThreadTableModel>();
		lockTableModels = new ConcurrentSkipListMap<JVM, LockTableModel>();
		threadStatsTableModels = new ConcurrentSkipListMap<JVM, ThreadStatsTableModel>();
		notifyWaitStackTracesTrees = new ConcurrentSkipListMap<JVM, StackTracesTree>();
		lockStackTracesTrees = new ConcurrentSkipListMap<JVM, StackTracesTree>();
		notifyWaitTables = new ConcurrentSkipListMap<JVM, MonitorLogTableModel>();
		timeTableModels = new ConcurrentSkipListMap<JVM, MethodProfilingTableModel>();
		threadStatePieDataset = new ConcurrentSkipListMap<JVM, DefaultPieDataset>();
		threadStateOverTimeDataset = new ConcurrentSkipListMap<JVM, DefaultCategoryDataset>();

		threadMessageHistory = new ConcurrentSkipListMap<JVM, ConcurrentMap<Long, Message>>();
		monitorMessageHistory = new ConcurrentSkipListMap<JVM, ConcurrentMap<Long, Message>>();
		methodMessageHistory = new ConcurrentSkipListMap<JVM, ConcurrentMap<Long, Message>>();

		initializeJVMs();
	}

	public Map<Integer, JVM> getIDsToJVMs() {
		return IDsToJVMs;
	}

	public JVM newJvmInstance(int id, String name, String host, long deltaSystemTime) {

		JVM jvm = IDsToJVMs.get(id);

		if (jvm == null) {
			jvm = new JVM(id, name, host, deltaSystemTime);
			IDsToJVMs.put(id, jvm);
			PieDataset pieDataset = new DefaultPieDataset();
			threadTableModels.put(jvm, new ThreadTableModel(jvm, pieDataset));
			lockTableModels.put(jvm, new LockTableModel(jvm));
			threadStatsTableModels.put(jvm, new ThreadStatsTableModel(jvm));
			notifyWaitStackTracesTrees.put(jvm, new StackTracesTree());
			notifyWaitTables.put(jvm, new MonitorLogTableModel(jvm));
			lockStackTracesTrees.put(jvm, new StackTracesTree());
			timeTableModels.put(jvm, new MethodProfilingTableModel(jvm));
			threadStatePieDataset.put(jvm, new DefaultPieDataset());
			threadStateOverTimeDataset.put(jvm, new DefaultCategoryDataset());

			threadMessageHistory.put(jvm,
					new ConcurrentSkipListMap<Long, Message>());
			monitorMessageHistory.put(jvm,
					new ConcurrentSkipListMap<Long, Message>());
			methodMessageHistory.put(jvm,
					new ConcurrentSkipListMap<Long, Message>());

			setChanged();
			notifyObservers(jvm);
		}

		return jvm;
	}

	private void initializeJVMs() {

		for (VirtualMachineDescriptor vmd : VirtualMachine.list()) {
			IDsToJVMs.put(Integer.parseInt(vmd.id()),
					new JVM(Integer.parseInt(vmd.id()), vmd.displayName(),
							"localhost", 0));
		}
		// TODO: notify Observer
	}

	public void addThreadInfo(int pid, ThreadInfo threadInfo) {
		IDsToJVMs.get(pid).addThread(threadInfo);
	}


	public void addMonitor(int pid, MonitorInfo monitor) {
		IDsToJVMs.get(pid).monitors.put(monitor.id, monitor);
	}

	public void addMessage(JVM jvm, long timestamp, Message message) {

		if (message instanceof ThreadMessage) {
			threadMessageHistory.get(jvm).put(timestamp, message);
		} else if (message instanceof MonitorMessage) {
			monitorMessageHistory.get(jvm).put(timestamp, message);
		} else if (message instanceof MethodMessage) {
			methodMessageHistory.get(jvm).put(timestamp, message);
		} else {
			throw new IllegalStateException("Unknown message type.");
		}
	}

	public void notifyGUI(JVM jvm) {
		setChanged();
		notifyObservers(jvm);
	}

	public Map<JVM, ThreadTableModel> getThreadTableModels() {
		return threadTableModels;
	}

	public Map<JVM, LockTableModel> getLockTableModels() {
		return lockTableModels;
	}

	public Map<JVM, ThreadStatsTableModel> getThreadStatsTableModels() {
		return threadStatsTableModels;
	}
	public Map<JVM, StackTracesTree> getNotifyWaitStackTracesTrees() {
		return notifyWaitStackTracesTrees;
	}

	public Map<JVM, StackTracesTree> getLockStackTracesTrees() {
		return lockStackTracesTrees;
	}

	public Map<JVM, MonitorLogTableModel> getMonitorLogTables() {
		return notifyWaitTables;
	}

	public Map<JVM, MethodProfilingTableModel> getTimeTableModels() {
		return timeTableModels;
	}

	public Map<JVM, DefaultPieDataset> getThreadStatePieDataset() {
		return threadStatePieDataset;
	}

	public Map<JVM, DefaultCategoryDataset> getThreadStateOverTimeDataset() {
		return threadStateOverTimeDataset;
	}
}
