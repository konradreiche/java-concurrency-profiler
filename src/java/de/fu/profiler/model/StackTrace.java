package de.fu.profiler.model;

import java.util.List;

public class StackTrace {
	
	ThreadInfo threadInfo;
	List<StackTraceElement> stackTrace;
	
	public StackTrace(ThreadInfo threadInfo, List<StackTraceElement> stackTrace) {
		super();
		this.threadInfo = threadInfo;
		this.stackTrace = stackTrace;
	}
}
