package de.fu.profiler.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;

import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import de.fu.profiler.model.AgentMessageProtos.AgentMessage;

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
	 * A message history of all received agent messages mapped by their target
	 * JVMs.
	 */
	Map<Integer, List<AgentMessage>> messageHistory;

	/**
	 * The current event being displayed.
	 */
	AgentMessage currentEvent;

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
		this.messageHistory = new ConcurrentHashMap<Integer, List<AgentMessage>>();
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
		notifyGUI();
	}

	public TableModel getTableModel() {
		return tableModel;
	}

	public PieDataset getThreadPieDataset() {
		return threadPieDataset;
	}

	public void addThreadInfo(int pid, ThreadInfo threadInfo) {
		IDsToJVMs.get(pid).addThread(threadInfo);
	}

	public void setThreadInfoState(int pid, ThreadInfo threadInfo, String state) {
		IDsToJVMs.get(pid).getThread(threadInfo.getId()).setState(state);
	}

	public void setThreadInfoMonitorStatus(int pid, ThreadInfo threadInfo,
			long timestamp, String status, boolean isContendedEvent) {

		if (isContendedEvent) {
			IDsToJVMs.get(pid).synchronizedLog.put(timestamp, status);
		} else {
			IDsToJVMs.get(pid).notifyWaitLog.put(timestamp, status);
		}
	}

	public JVM getCurrentJVM() {
		return currentJVM;
	}

	public void setCurrentJVM(JVM currentJVM) {
		this.currentJVM = currentJVM;
	}

	public void addMonitor(int pid, Monitor monitor) {
		IDsToJVMs.get(pid).monitors.put(monitor.id, monitor);
	}

	public void addAgentMessage(int pid, AgentMessage agentMessage) {
		if (!messageHistory.containsKey(pid)) {
			messageHistory.put(pid, new ArrayList<AgentMessage>());
		}
		messageHistory.get(pid).add(agentMessage);
		currentEvent = agentMessage;
	}

	public Map<Integer, List<AgentMessage>> getMessageHistory() {
		return messageHistory;
	}

	public AgentMessage getCurrentEvent() {
		return currentEvent;
	}

	public List<AgentMessage> getCurrentEventHistory() {
		return messageHistory.get(currentJVM.id);
	}

	public void setCurrentEvent(int index) {
		currentEvent = getCurrentEventHistory().get(index);
	}

	public void applyData(AgentMessage agentMessage, boolean isLogging) {
		int jvm_id = agentMessage.getJvmId();
		JVM jvm = null;

		synchronized (IDsToJVMs) {
			jvm = IDsToJVMs.get(jvm_id);
			if (jvm == null) {
				jvm = new JVM(jvm_id, "default");
				IDsToJVMs.put(jvm_id, jvm);
				((ThreadTableModel) getTableModel()).setCurrentJVM(jvm);
				setCurrentJVM(jvm);
			}
		}

		if (isLogging) {
			addAgentMessage(jvm_id, agentMessage);
		}

		if (agentMessage.hasThreadEvent()) {
			for (de.fu.profiler.model.AgentMessageProtos.AgentMessage.Thread thread : agentMessage
					.getThreadEvent().getThreadList()) {

				ThreadInfo threadInfo = new ThreadInfo(thread.getId(),
						thread.getName(), thread.getPriority(), thread
								.getState().toString(),
						thread.getIsContextClassLoaderSet());

				if (thread.hasCpuTime()) {
					threadInfo.setCpuTime(thread.getCpuTime());
				}

				addThreadInfo(jvm_id, threadInfo);
			}
		}

		if (agentMessage.hasMonitorEvent()) {

			de.fu.profiler.model.AgentMessageProtos.AgentMessage.Thread t = agentMessage
					.getMonitorEvent().getThread();

			ThreadInfo thread = jvm.getThread(agentMessage.getMonitorEvent()
					.getThread().getId());

			if (thread == null) {
				thread = new ThreadInfo(t.getId(), t.getName(),
						t.getPriority(), t.getState().toString(),
						t.getIsContextClassLoaderSet());
				addThreadInfo(jvm_id, thread);
			}

			String stateChangeNotification = null;
			if (!thread.getState().equals(
					agentMessage.getMonitorEvent().getThread().getState()
							.toString())) {

				stateChangeNotification = thread.getName()
						+ " switched from "
						+ thread.getState()
						+ " to "
						+ agentMessage.getMonitorEvent().getThread().getState()
								.toString();

			}

			setThreadInfoState(jvm_id, thread, agentMessage.getMonitorEvent()
					.getThread().getState().toString());

			addThreadInfo(jvm_id, thread);

			String monitorStatus = null;
			switch (agentMessage.getMonitorEvent().getEventType()) {
			case WAIT:
				monitorStatus = thread.getName()
				+ " invoked"
				+ " wait() in "
				+ agentMessage.getMonitorEvent()
						.getMonitorClass()
				+ "."
				+ agentMessage.getMonitorEvent()
						.getContextMethod() + "\n";
				
				if (stateChangeNotification != null) {
					monitorStatus += stateChangeNotification + "\n"; 
				}
				
				setThreadInfoMonitorStatus(jvm_id, thread,
						agentMessage.getTimestamp(), monitorStatus, false);
				thread.increaseWaitCounter();
				break;
			case WAITED:
				monitorStatus = thread.getName()
				+ " left"
				+ " wait() in "
				+ agentMessage.getMonitorEvent()
						.getMonitorClass()
				+ "."
				+ agentMessage.getMonitorEvent()
						.getContextMethod() + "\n";
				
				if (stateChangeNotification != null) {
					monitorStatus += stateChangeNotification + "\n"; 
				}
				setThreadInfoMonitorStatus(jvm_id, thread,
						agentMessage.getTimestamp(), monitorStatus, false);
				break;
			case NOTIFY_ALL:
				monitorStatus = thread.getName()
				+ " invoked"
				+ " notifyAll() in "
				+ agentMessage.getMonitorEvent()
						.getMonitorClass()
				+ "."
				+ agentMessage.getMonitorEvent()
						.getContextMethod() + "\n";
				
				if (stateChangeNotification != null) {
					monitorStatus += stateChangeNotification + "\n"; 
				}
				setThreadInfoMonitorStatus(jvm_id, thread,
						agentMessage.getTimestamp(), monitorStatus, false);
				break;
			case CONTENDED:
				setThreadInfoMonitorStatus(jvm_id, thread,
						agentMessage.getTimestamp(), thread.getName()
								+ " failed to acquire a monitor." + "\n", true);
				break;
			case ENTERED:
				setThreadInfoMonitorStatus(jvm_id, thread,
						agentMessage.getTimestamp(), thread.getName()
								+ " acquired a monitor." + "\n", true);
				break;
			}
			
			if (agentMessage.getMonitorEvent().hasMonitorId()) {
				Monitor monitor = new Monitor(agentMessage.getMonitorEvent()
						.getMonitorId(), agentMessage.getMonitorEvent()
						.getMonitorClass(), agentMessage.getMonitorEvent()
						.getEntryCount(), agentMessage.getMonitorEvent()
						.getWaiterCount(), agentMessage.getMonitorEvent()
						.getNotifyWaiterCount());
				addMonitor(jvm_id, monitor);
			}
		}
	}

	public void applyDataUntilEvent(final AgentMessage currentEvent) {

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				clearAllStates();

				int index = getCurrentEventHistory().indexOf(currentEvent);
				for (int i = 0; i < index; ++i) {
					applyData(getCurrentEventHistory().get(i), false);
				}
				notifyGUI();
			}
		});
	}

	private void clearAllStates() {
		IDsToJVMs.remove(currentJVM.id);
	}

	public void notifyGUI() {
		setChanged();
		notifyObservers();
	}
}
