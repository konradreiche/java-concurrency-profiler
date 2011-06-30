package de.fu.profiler.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import de.fu.profiler.model.AgentMessageProtos.AgentMessage;
import de.fu.profiler.model.AgentMessageProtos.AgentMessage.MethodEvent;
import de.fu.profiler.model.AgentMessageProtos.AgentMessage.StackTrace.StackTraceElement;
import de.fu.profiler.model.AgentMessageProtos.AgentMessage.Thread;

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
	 * The table model for displaying the statistical data of each thread.
	 */
	TableModel threadStatsTableModel;

	LockTableModel lockTableModel;

	/**
	 * The data set on which the thread state pie chart is based on.
	 */
	DefaultPieDataset threadPieDataSet;

	/**
	 * The data set on which the thread state over time diagram is based on.
	 */
	DefaultCategoryDataset threadOverTimeDataSet;

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
	 * The table model for displaying the measured time and equivalent data of
	 * each method
	 */
	TimeTableModel timeTableModel;

	NotifyWaitTableModel notifyWaitTableModel;

	volatile boolean hasChanged;

	StackTracesTree stackTracesTree;

	SynchronizedTableModel synchronizedTableModel;

	/**
	 * At the start of the profiler all available JVMs are read and listed.
	 * 
	 * @throws IOException
	 *             if an I/O error occurs when opening the socket.
	 */
	public ProfilerModel() throws IOException {
		super();
		this.IDsToJVMs = new ConcurrentHashMap<Integer, JVM>();
		this.tableModel = new ThreadTableModel(threadPieDataSet);
		this.threadStatsTableModel = new ThreadStatsTableModel();
		this.synchronizedTableModel = new SynchronizedTableModel(stackTracesTree);
		this.lockTableModel = new LockTableModel();
		this.stackTracesTree = new StackTracesTree();
		this.notifyWaitTableModel = new NotifyWaitTableModel(stackTracesTree);
		this.timeTableModel = new TimeTableModel();
		this.messageHistory = new ConcurrentHashMap<Integer, List<AgentMessage>>();
		initializeThreadStatePieDataset();
		initializeThreadStateOverTimeBarDataSet();
		initializeJVMs();
	}

	public Map<Integer, JVM> getIDsToJVMs() {
		return IDsToJVMs;
	}

	private void initializeThreadStateOverTimeBarDataSet() {
		threadOverTimeDataSet = new DefaultCategoryDataset();
	}

	private void initializeThreadStatePieDataset() {
		threadPieDataSet = new DefaultPieDataset();
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

	public TableModel getThreadStatsTableModel() {
		return threadStatsTableModel;
	}

	public PieDataset getThreadPieDataset() {
		return threadPieDataSet;
	}

	public void addThreadInfo(int pid, ThreadInfo threadInfo) {
		IDsToJVMs.get(pid).addThread(threadInfo);
	}

	public void setThreadInfoState(int pid, ThreadInfo threadInfo, String state) {
		IDsToJVMs.get(pid).getThread(threadInfo.getId()).setState(state);
	}

	public void setThreadInfoMonitorStatus(int pid, ThreadInfo threadInfo,
			long timestamp, boolean isContendedEvent,
			NotifyWaitLogEntry notifyWaitLogEntry) {

		if (isContendedEvent) {
			IDsToJVMs.get(pid).synchronizedLog.put(timestamp, notifyWaitLogEntry);
		} else {
			IDsToJVMs.get(pid).notifyWaitLog.put(timestamp, notifyWaitLogEntry);
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

		if (agentMessage == null) {
			return;
		}

		hasChanged = true;

		int jvm_id = agentMessage.getJvmId();
		JVM jvm = null;

		synchronized (IDsToJVMs) {
			jvm = IDsToJVMs.get(jvm_id);
			if (jvm == null) {
				jvm = new JVM(jvm_id, "default");
				IDsToJVMs.put(jvm_id, jvm);

				((ThreadTableModel) getTableModel()).setCurrentJVM(jvm);

				((ThreadStatsTableModel) threadStatsTableModel)
						.setCurrentJVM(jvm);
				((NotifyWaitTableModel) notifyWaitTableModel)
						.setCurrentJVM(jvm);
				lockTableModel.setCurrentJVM(jvm);
				synchronizedTableModel.setCurrentJVM(jvm);

				setCurrentJVM(jvm);

				timeTableModel.setCurrentJVM(jvm);
			}
		}

		if (isLogging) {
			addAgentMessage(jvm_id, agentMessage);
		}

		if (agentMessage.hasThreadEvent()) {
			for (de.fu.profiler.model.AgentMessageProtos.AgentMessage.Thread thread : agentMessage
					.getThreadEvent().getThreadList()) {

				updateThreadInfo(jvm, agentMessage, thread);
			}
		}

		if (agentMessage.hasMethodEvent()) {

			MethodEvent methodEvent = agentMessage.getMethodEvent();

			String methodIdentifier = methodEvent.getClassName() + "."
					+ methodEvent.getMethodName();

			MethodInfo method = jvm.methods.get(methodIdentifier);

			if (method == null) {
				method = new MethodInfo(methodEvent.getClassName(),
						methodEvent.getMethodName(),
						methodEvent.getClockCycles(),
						methodEvent.getTimeTaken(), jvm.getThread(methodEvent
								.getThread().getId()));
				jvm.methods.put(methodIdentifier, method);
			} else {
				method.wasInvoked(methodEvent.getClockCycles(),
						methodEvent.getTimeTaken(),
						jvm.getThread(methodEvent.getThread().getId()));
			}

			MethodInfo.updateRelativeTime(jvm);
		}

		if (agentMessage.hasMonitorEvent()) {

			de.fu.profiler.model.AgentMessageProtos.AgentMessage.Thread thread = agentMessage
					.getMonitorEvent().getThread();

			ThreadInfo threadInfo = jvm.getThread(thread.getId());

			if (threadInfo == null) {
				threadInfo = new ThreadInfo(thread.getId(), thread.getName(),
						thread.getPriority(), thread.getState().toString(),
						thread.getIsContextClassLoaderSet(),
						agentMessage.getSystemTime());
				addThreadInfo(jvm_id, threadInfo);
			}

			boolean hasThreadStateChaned = false;
			String oldState = null;
			String newState = null;

			if (!threadInfo.getState().equals(
					agentMessage.getMonitorEvent().getThread().getState()
							.toString())) {

				oldState = threadInfo.getState();
				newState = agentMessage.getMonitorEvent().getThread()
						.getState().toString();
				hasThreadStateChaned = true;
			}

			setThreadInfoState(jvm_id, threadInfo, agentMessage
					.getMonitorEvent().getThread().getState().toString());

			updateThreadInfo(jvm, agentMessage, thread);

			NotifyWaitLogEntry notifyWaitLogEntry = null;
			Monitor monitor = null;

			List<StackTrace> stackTraces = new ArrayList<StackTrace>();

			for (AgentMessage.StackTrace st : agentMessage.getMonitorEvent()
					.getStackTracesList()) {

				ThreadInfo t = new ThreadInfo(st.getThread().getId(), st
						.getThread().getName(), st.getThread().getPriority(),
						st.getThread().getState().toString(), st.getThread()
								.getIsContextClassLoaderSet(),
						agentMessage.getTimestamp());

				List<java.lang.StackTraceElement> stes = new ArrayList<java.lang.StackTraceElement>();
				for (StackTraceElement ste : st.getStackTraceList()) {

					stes.add(new java.lang.StackTraceElement(
							ste.getClassName(), ste.getMethodName(), ste
									.getFileName(), -1));
				}

				stackTraces.add(new StackTrace(t, stes));
			}

			if (agentMessage.getMonitorEvent().hasMonitor()) {

				Map<ThreadInfo, StackTrace> waiter = new TreeMap<ThreadInfo, StackTrace>();
				for (AgentMessage.Thread waiterThread : agentMessage
						.getMonitorEvent().getMonitor().getWaiterThreadsList()) {
					
					StackTrace relatedStackTrace = null;
					ThreadInfo waiterThreadInfo = new ThreadInfo(waiterThread, agentMessage
							.getTimestamp());
					
					for (AgentMessage.StackTrace st : agentMessage.getMonitorEvent().getStackTracesList()) {
						
						if (waiterThread.getId() == st.getThread().getId()) {
							relatedStackTrace = new StackTrace(threadInfo, st);
						}
					}
					
					waiter.put(waiterThreadInfo, relatedStackTrace);
				}

				Map<ThreadInfo, StackTrace> notifyWaiter = new TreeMap<ThreadInfo, StackTrace>();
				for (AgentMessage.Thread notifyWaiterThread : agentMessage
						.getMonitorEvent().getMonitor().getNotifyWaiterThreadsList()) {
					
					
					
					StackTrace relatedStackTrace = null;
					ThreadInfo notifyWaiterThreadInfo = new ThreadInfo(notifyWaiterThread, agentMessage
							.getTimestamp());
					
					for (AgentMessage.StackTrace st : agentMessage.getMonitorEvent().getStackTracesList()) {
						
						if (notifyWaiterThread.getId() == st.getThread().getId()) {
							relatedStackTrace = new StackTrace(threadInfo, st);							
						}
					}
					
					notifyWaiter.put(notifyWaiterThreadInfo, relatedStackTrace);
				}

				monitor = new Monitor(agentMessage.getMonitorEvent()
						.getMonitor().getId(), agentMessage.getMonitorEvent()
						.getClassName(), agentMessage.getMonitorEvent()
						.getMonitor().getEntryCount(), agentMessage
						.getMonitorEvent().getMonitor().getWaiterCount(),
						agentMessage.getMonitorEvent().getMonitor()
								.getNotifyWaiterCount(), waiter, notifyWaiter);
			
				updateMonitorInfo(currentJVM, agentMessage, monitor);
			}

			switch (agentMessage.getMonitorEvent().getEventType()) {
			case WAIT:

				if (hasThreadStateChaned) {
					notifyWaitLogEntry = new NotifyWaitLogEntry(threadInfo,
							oldState, newState,
							NotifyWaitLogEntry.Type.INVOKED_WAIT, agentMessage
									.getMonitorEvent().getMethodName(),
							agentMessage.getMonitorEvent().getClassName(),
							agentMessage.getSystemTime(), stackTraces);
				} else {
					notifyWaitLogEntry = new NotifyWaitLogEntry(threadInfo,
							null, null, NotifyWaitLogEntry.Type.INVOKED_WAIT,
							agentMessage.getMonitorEvent().getMethodName(),
							agentMessage.getMonitorEvent().getClassName(),
							agentMessage.getSystemTime(), stackTraces);
				}

				setThreadInfoMonitorStatus(jvm_id, threadInfo,
						agentMessage.getTimestamp(), false,
						notifyWaitLogEntry);

				++threadInfo.waitCount;
				break;
			case WAITED:

				if (hasThreadStateChaned) {
					notifyWaitLogEntry = new NotifyWaitLogEntry(threadInfo,
							oldState, newState,
							NotifyWaitLogEntry.Type.LEFT_WAIT, agentMessage
									.getMonitorEvent().getMethodName(),
							agentMessage.getMonitorEvent().getClassName(),
							agentMessage.getSystemTime(), stackTraces);
				} else {
					notifyWaitLogEntry = new NotifyWaitLogEntry(threadInfo,
							null, null, NotifyWaitLogEntry.Type.LEFT_WAIT,
							agentMessage.getMonitorEvent().getMethodName(),
							agentMessage.getMonitorEvent().getClassName(),
							agentMessage.getSystemTime(), stackTraces);
				}

				setThreadInfoMonitorStatus(jvm_id, threadInfo,
						agentMessage.getTimestamp(), false,
						notifyWaitLogEntry);
				break;
			case NOTIFY_ALL:


				if (hasThreadStateChaned) {
					notifyWaitLogEntry = new NotifyWaitLogEntry(threadInfo,
							oldState, newState,
							NotifyWaitLogEntry.Type.INVOKED_NOTIFY_ALL,
							agentMessage.getMonitorEvent().getMethodName(),
							agentMessage.getMonitorEvent().getClassName(),
							agentMessage.getSystemTime(), stackTraces);
				} else {
					notifyWaitLogEntry = new NotifyWaitLogEntry(threadInfo,
							null, null,
							NotifyWaitLogEntry.Type.INVOKED_NOTIFY_ALL,
							agentMessage.getMonitorEvent().getMethodName(),
							agentMessage.getMonitorEvent().getClassName(),
							agentMessage.getSystemTime(), stackTraces);
				}

				setThreadInfoMonitorStatus(jvm_id, threadInfo,
						agentMessage.getTimestamp(), false,
						notifyWaitLogEntry);

				++threadInfo.notifyAllCount;
				break;

			case NOTIFY:

				if (hasThreadStateChaned) {
					notifyWaitLogEntry = new NotifyWaitLogEntry(threadInfo,
							oldState, newState,
							NotifyWaitLogEntry.Type.INVOKED_NOTIFY,
							agentMessage.getMonitorEvent().getMethodName(),
							agentMessage.getMonitorEvent().getClassName(),
							agentMessage.getSystemTime(), stackTraces);
				} else {
					notifyWaitLogEntry = new NotifyWaitLogEntry(threadInfo,
							null, null, NotifyWaitLogEntry.Type.INVOKED_NOTIFY,
							agentMessage.getMonitorEvent().getMethodName(),
							agentMessage.getMonitorEvent().getClassName(),
							agentMessage.getSystemTime(), stackTraces);
				}

				setThreadInfoMonitorStatus(jvm_id, threadInfo,
						agentMessage.getTimestamp(), false,
						notifyWaitLogEntry);
				++threadInfo.notifyCount;
				break;
			case CONTENDED:

				int owningThreadId = agentMessage.getMonitorEvent()
						.getMonitor().getOwningThread();
				ThreadInfo owningThread = jvm.getThread(owningThreadId);
				monitor.allocatedToThread = owningThread;

				if (hasThreadStateChaned) {
					notifyWaitLogEntry = new NotifyWaitLogEntry(threadInfo,
							oldState, newState,
							NotifyWaitLogEntry.Type.CONTENDED, agentMessage
									.getMonitorEvent().getMethodName(),
							agentMessage.getMonitorEvent().getClassName(),
							agentMessage.getSystemTime(), stackTraces);
				} else {
					notifyWaitLogEntry = new NotifyWaitLogEntry(threadInfo,
							null, null, NotifyWaitLogEntry.Type.CONTENDED,
							agentMessage.getMonitorEvent().getMethodName(),
							agentMessage.getMonitorEvent().getClassName(),
							agentMessage.getSystemTime(), stackTraces);
				}
				
				notifyWaitLogEntry.owningThread = owningThread;

				setThreadInfoMonitorStatus(jvm_id, threadInfo,
						agentMessage.getTimestamp(),true, notifyWaitLogEntry);

				threadInfo.requestedResource = monitor;
				++threadInfo.monitorContendedCount;
				break;
			case ENTERED:
				
				if (hasThreadStateChaned) {
					notifyWaitLogEntry = new NotifyWaitLogEntry(threadInfo,
							oldState, newState,
							NotifyWaitLogEntry.Type.ENTERED, agentMessage
									.getMonitorEvent().getMethodName(),
							agentMessage.getMonitorEvent().getClassName(),
							agentMessage.getSystemTime(), stackTraces);
				} else {
					notifyWaitLogEntry = new NotifyWaitLogEntry(threadInfo,
							null, null, NotifyWaitLogEntry.Type.ENTERED,
							agentMessage.getMonitorEvent().getMethodName(),
							agentMessage.getMonitorEvent().getClassName(),
							agentMessage.getSystemTime(), stackTraces);
				}
				
				setThreadInfoMonitorStatus(jvm_id, threadInfo,
						agentMessage.getTimestamp(), true, notifyWaitLogEntry);

				jvm.getMonitor(monitor.getId()).allocatedToThread = threadInfo;
				threadInfo.requestedResource = null;
				++threadInfo.monitorEnteredCount;
				break;
			}
		}
	}

	private void updateThreadInfo(JVM jvm, AgentMessage agentMessage,
			Thread thread) {
		ThreadInfo threadInfo = jvm.getThread(thread.getId());

		if (threadInfo == null) {
			threadInfo = new ThreadInfo(thread.getId(), thread.getName(),
					thread.getPriority(), thread.getState().toString(),
					thread.getIsContextClassLoaderSet(),
					agentMessage.getSystemTime());
			addThreadInfo(jvm.getId(), threadInfo);
		} else {

			jvm.getThread(thread.getId()).compareAndSet(
					agentMessage.getSystemTime(), thread.getId(),
					thread.getName(), thread.getPriority(),
					thread.getState().toString(),
					thread.getIsContextClassLoaderSet());

			if (thread.hasCpuTime()) {
				threadInfo.setCpuTime(thread.getCpuTime());
			}
		}
	}

	private void updateMonitorInfo(JVM jvm, AgentMessage agentMessage,
			Monitor monitor) {

		Monitor currentMonitor = jvm.getMonitor(monitor.getId());
		if (currentMonitor == null) {
			addMonitor(jvm.getId(), monitor);
		} else {
			currentMonitor.compareAndSet(monitor);
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

	public DefaultCategoryDataset getCategoryDataset() {
		return threadOverTimeDataSet;
	}

	public TimeTableModel getTimeTableModel() {
		return timeTableModel;
	}

	public NotifyWaitTableModel getNotifyWaitTableModel() {
		return notifyWaitTableModel;
	}

	public DefaultMutableTreeNode getTreeNode() {
		return stackTracesTree.root;
	}

	public StackTracesTree getStackTracesTree() {
		return stackTracesTree;
	}

	public LockTableModel getLockTableModel() {
		return lockTableModel;
	}

	public SynchronizedTableModel getSynchronizedTableModel() {
		return synchronizedTableModel;
	}

}
