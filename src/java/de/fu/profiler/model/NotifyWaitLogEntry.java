package de.fu.profiler.model;

import java.util.List;

/**
 * Models an event entry of a notify and wait event.
 * 
 * @author Konrad Johannes Reiche
 * 
 */
public class NotifyWaitLogEntry {

	final ThreadInfo threadInfo;
	final String oldState;
	final String newState;
	final Type type;
	final String methodContext;
	final String monitorClass;
	final long systemTime;
	final List<StackTrace> stackTraces;
	
	ThreadInfo owningThread;

	public enum Type {
		INVOKED_WAIT, LEFT_WAIT, INVOKED_NOTIFY_ALL, INVOKED_NOTIFY, CONTENDED, ENTERED,
	}

	public NotifyWaitLogEntry(ThreadInfo threadInfo, String oldState,
			String newState, Type type, String methodContext,
			String monitorClass, long systemTime, List<StackTrace> stackTraces) {
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

	public List<StackTrace> getStackTraces() {
		return stackTraces;
	}
	
	

}
