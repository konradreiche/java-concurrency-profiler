package de.fu.profiler.model;

import java.util.ArrayList;
import java.util.List;

import de.fu.profiler.model.AgentMessageProtos.AgentMessage;

public class StackTrace {

	ThreadInfo threadInfo;
	List<StackTraceElement> stackTrace;

	public StackTrace(ThreadInfo threadInfo, List<StackTraceElement> stackTrace) {
		super();
		this.threadInfo = threadInfo;
		this.stackTrace = stackTrace;
	}

	public StackTrace(ThreadInfo threadInfo, AgentMessage.StackTrace st) {

		this.threadInfo = threadInfo;
		this.stackTrace = new ArrayList<StackTraceElement>();
		for (de.fu.profiler.model.AgentMessageProtos.AgentMessage.StackTrace.StackTraceElement ste : st
				.getStackTraceList()) {
			
			this.stackTrace.add(new StackTraceElement(ste.getClassName(), ste
					.getMethodName(), ste.getFileName(), -1));
		}
		
		
	}

	public List<StackTraceElement> getStackTrace() {
		return stackTrace;
	}

}
