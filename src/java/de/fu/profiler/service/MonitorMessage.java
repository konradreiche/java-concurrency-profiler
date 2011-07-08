package de.fu.profiler.service;

import java.util.Map;
import java.util.TreeMap;

import de.fu.profiler.model.JVM;
import de.fu.profiler.model.MonitorInfo;
import de.fu.profiler.model.MonitorLogEntry;
import de.fu.profiler.model.MonitorLogEntry.Type;
import de.fu.profiler.model.ProfilerModel;
import de.fu.profiler.model.StackTrace;
import de.fu.profiler.model.ThreadInfo;
import de.fu.profiler.service.AgentMessageProtos.AgentMessage;
import de.fu.profiler.service.AgentMessageProtos.AgentMessage.Monitor;
import de.fu.profiler.service.AgentMessageProtos.AgentMessage.MonitorEvent;

public class MonitorMessage extends Message {

	boolean isNewMonitor;
	Map<ThreadInfo, StackTrace> allStackTraces;
	MonitorInfo previousMonitorInfo;
	MonitorLogEntry logEntry;

	public MonitorMessage(ProfilerModel profiler, JVM jvm,
			AgentMessage agentMessage) {
		super(profiler, jvm, agentMessage);
	}

	@Override
	public void execute() {

		MonitorEvent event = agentMessage.getMonitorEvent();
		AgentMessage.Thread thread = event.getThread();
		long systemTime = agentMessage.getSystemTime();
		ThreadInfo threadInfo = jvm.getThread(thread.getId());
		if (threadInfo == null) {
			threadInfo = new ThreadInfo(thread, systemTime);
			jvm.addThread(threadInfo);
		}

		String currentState = threadInfo.getState();
		String nextState = ThreadInfo.formatThreadState(event.getThread()
				.getState());

		if (currentState.equals(nextState)) {
			currentState = null;
			nextState = null;
		}
		MonitorInfo monitorInfo = null;
		updateThread(threadInfo);
		allStackTraces = createStackTraces();
		if (event.hasMonitor()) {
			monitorInfo = processMonitorInfo(event);
		}
		processEventType(event, monitorInfo, threadInfo, currentState,
				nextState);

	}

	private void processEventType(MonitorEvent event, MonitorInfo monitorInfo,
			ThreadInfo threadInfo, String currentState, String nextState) {

		long timestamp = agentMessage.getTimestamp();
		Monitor monitor = event.getMonitor();
		long systemTime = agentMessage.getSystemTime();
		String className = event.getClassName();
		String methodName = event.getMethodName();
		MonitorEvent.EventType eventType = event.getEventType();

		switch (eventType) {
		case CONTENDED:
			int id = monitor.getOwningThread();
			ThreadInfo owningThread = jvm.getThread(id);

			logEntry = new MonitorLogEntry(threadInfo, currentState, nextState,
					Type.CONTENDED_WITH_THREAD, methodName, className,
					systemTime, allStackTraces);

			if (owningThread != null) {
				jvm.assignResource(timestamp, owningThread, monitorInfo);
			}
			
			jvm.requestResource(timestamp + 1, threadInfo, monitorInfo);
			logEntry.setOwningThread(owningThread);
			threadInfo.increaseMonitorContendedCount();
			break;
		case ENTERED:

			logEntry = new MonitorLogEntry(threadInfo, currentState, nextState,
					Type.ENTERED_AFTER_CONTENTION_WITH_THREAD, methodName,
					className, systemTime, allStackTraces);

			owningThread = monitorInfo.getOwningThread();
			
			if (owningThread != null) {
				jvm.unassignResource(timestamp, owningThread, monitorInfo);				
			}

			jvm.stopRequestResource(timestamp, threadInfo, monitorInfo);
			jvm.assignResource(timestamp, threadInfo, monitorInfo);

			threadInfo.increaseMonitorEnteredCount();
			break;
		case NONE:
			throw new IllegalStateException("The event type NONE is illegal.");
		case NOTIFY:

			logEntry = new MonitorLogEntry(threadInfo, currentState, nextState,
					Type.INVOKED_NOTIFY, methodName, className, systemTime,
					allStackTraces);

			threadInfo.increaseNotifyCount();
			break;
		case NOTIFY_ALL:

			logEntry = new MonitorLogEntry(threadInfo, currentState, nextState,
					Type.INVOKED_NOTIFY_ALL, methodName, className, systemTime,
					allStackTraces);

			threadInfo.increaseNotifyAllCount();
			break;
		case WAIT:

			logEntry = new MonitorLogEntry(threadInfo, currentState, nextState,
					Type.INVOKED_WAIT, methodName, className, systemTime,
					allStackTraces);

			threadInfo.increaseWaitCount();
			break;
		case WAITED:

			logEntry = new MonitorLogEntry(threadInfo, currentState, nextState,
					Type.LEFT_WAIT, methodName, className, systemTime,
					allStackTraces);

			threadInfo.increaseWaitCount();
			break;
		}

		jvm.addMonitorLogEntry(timestamp, logEntry);
	}

	private MonitorInfo processMonitorInfo(MonitorEvent event) {
		AgentMessage.Monitor monitor = agentMessage.getMonitorEvent()
				.getMonitor();

		int id = (int) monitor.getId();
		String className = event.getClassName();
		int entryCount = monitor.getEntryCount();
		int waiterCount = monitor.getWaiterCount();
		int notifyWaiterCount = monitor.getNotifyWaiterCount();

		Map<ThreadInfo, StackTrace> waiter = new TreeMap<ThreadInfo, StackTrace>();
		Map<ThreadInfo, StackTrace> notifyWaiter = new TreeMap<ThreadInfo, StackTrace>();

		probeMonitorRelatedThreads(monitor, allStackTraces, waiter,
				notifyWaiter);

		MonitorInfo monitorInfo = jvm.getMonitor(id);
		if (monitorInfo == null) {
			isNewMonitor = true;
			monitorInfo = new MonitorInfo(id, className, entryCount,
					waiterCount, notifyWaiterCount, waiter, notifyWaiter);
			jvm.addMonitor(monitorInfo);
		} else {
			previousMonitorInfo = monitorInfo.copy();
			monitorInfo.update(id, className, entryCount, waiterCount,
					notifyWaiterCount, waiter, notifyWaiter);
		}

		return monitorInfo;
	}

	private void updateThread(ThreadInfo threadInfo) {

		AgentMessage.Thread thread = agentMessage.getMonitorEvent().getThread();
		long systemTime = agentMessage.getSystemTime();
		int id = thread.getId();
		String name = thread.getName();
		int priority = thread.getPriority();
		String state = ThreadInfo.formatThreadState(thread.getState());
		boolean isContextClassLoaderSet = thread.getIsContextClassLoaderSet();

		threadInfo.update(systemTime, id, name, priority, state,
				isContextClassLoaderSet);

		if (thread.hasCpuTime()) {
			threadInfo.setCpuTime(thread.getCpuTime());
		}

	}

	private void probeMonitorRelatedThreads(AgentMessage.Monitor monitor,
			Map<ThreadInfo, StackTrace> allStackTraces,
			Map<ThreadInfo, StackTrace> waiterStackTraces,
			Map<ThreadInfo, StackTrace> notifyWaiterStackTraces) {

		for (AgentMessage.Thread notifyWaiterThread : monitor
				.getWaiterThreadsList()) {

			ThreadInfo relatedThread = jvm
					.getThread(notifyWaiterThread.getId());
			StackTrace relatedStackTrace = allStackTraces.get(relatedThread);
			waiterStackTraces.put(relatedThread, relatedStackTrace);
		}

		for (AgentMessage.Thread notifyWaiterThread : monitor
				.getNotifyWaiterThreadsList()) {

			ThreadInfo relatedThread = jvm
					.getThread(notifyWaiterThread.getId());
			StackTrace relatedStackTrace = allStackTraces.get(relatedThread);
			notifyWaiterStackTraces.put(relatedThread, relatedStackTrace);
		}

	}

	private Map<ThreadInfo, StackTrace> createStackTraces() {
		Map<ThreadInfo, StackTrace> stackTraces = new TreeMap<ThreadInfo, StackTrace>();
		MonitorEvent event = agentMessage.getMonitorEvent();
		for (AgentMessage.StackTrace stackTrace : event.getStackTracesList()) {

			AgentMessage.Thread thread = stackTrace.getThread();
			long systemTime = agentMessage.getSystemTime();

			ThreadInfo threadInfo = jvm.getThread(thread.getId());

			if (threadInfo == null) {
				threadInfo = new ThreadInfo(thread, systemTime);
				jvm.addThread(threadInfo);
			}

			StackTrace newStackTrace = new StackTrace(threadInfo, stackTrace);
			stackTraces.put(threadInfo, newStackTrace);
		}

		return stackTraces;
	}

	@Override
	public void undo() {

		MonitorEvent event = agentMessage.getMonitorEvent();
		if (isNewMonitor) {
			long id = event.getMonitor().getId();
			jvm.removeMonitor(id);
		} else {
			jvm.addMonitor(previousMonitorInfo);
		}

		jvm.getMonitorLog().remove(logEntry.getSystemTime());
	}

}
