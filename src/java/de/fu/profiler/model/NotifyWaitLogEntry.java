package de.fu.profiler.model;

/**
 * Models an event entry of a notify and wait event.
 * 
 * @author Konrad Johannes Reiche
 * 
 */
public class NotifyWaitLogEntry {

	final ThreadInfo threadInfo;
	final String state;
	final Type type;
	final String methodContext;
	final String monitorClass;

	public enum Type {
		INVOKED_WAIT, LEFT_WAIT, INVOKED_NOTIFY_ALL, INVOKED_NOTIFY,
	}
	
	
	public NotifyWaitLogEntry(ThreadInfo threadInfo, String state, Type type, String methodContext, String monitorClass) {
		super();
		this.threadInfo = threadInfo;
		this.state = state;
		this.type = type;
		this.methodContext = methodContext;
		this.monitorClass = monitorClass;
	}

	public ThreadInfo getThreadInfo() {
		return threadInfo;
	}

	public String getState() {
		return state;
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
	
	
}
