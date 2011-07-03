package de.fu.profiler.model;

import java.util.Map;

/**
 * Models an event entry of an monitor event.
 * 
 * @author Konrad Johannes Reiche
 * 
 */
public class MonitorLogEntry {

	final ThreadInfo threadInfo;
	final String oldState;
	final String newState;
	final Type type;
	final String methodContext;
	final String monitorClass;
	final long systemTime;
	final Map<ThreadInfo,StackTrace> stackTraces;

	ThreadInfo owningThread;

	public enum Type {
		INVOKED_WAIT, LEFT_WAIT, INVOKED_NOTIFY_ALL, INVOKED_NOTIFY, CONTENDED_WITH_THREAD, ENTERED_AFTER_CONTENTION_WITH_THREAD,
	}

	public MonitorLogEntry(ThreadInfo threadInfo, String oldState,
			String newState, Type type, String methodContext,
			String monitorClass, long systemTime, Map<ThreadInfo,StackTrace> stackTraces) {
		super();
		this.threadInfo = threadInfo;
		this.oldState = oldState;
		this.newState = newState;
		this.type = type;
		this.methodContext = methodContext;
		this.monitorClass = monitorClass;
		this.systemTime = systemTime;
		this.stackTraces = stackTraces;
	}

	public ThreadInfo getThreadInfo() {
		return threadInfo;
	}

	public String getState() {
		return oldState;
	}

	public Type getType() {
		return type;
	}

	public String getMethodContext() {
		return methodContext;
	}

	public String getMonitorClass() {
		return monitorClass;
	}

	public Map<ThreadInfo, StackTrace> getStackTraces() {
		return stackTraces;
	}

	public void setOwningThread(ThreadInfo owningThread) {
		this.owningThread = owningThread;
	}

	public long getSystemTime() {
		return systemTime;
	}
	
	

}
