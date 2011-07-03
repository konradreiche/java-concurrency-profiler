package de.fu.profiler.model;

import java.util.ArrayList;
import java.util.List;

import de.fu.profiler.service.AgentMessageProtos.AgentMessage;

public class StackTrace {

	ThreadInfo threadInfo;
	List<StackTraceElement> stackTrace;

	public StackTrace(ThreadInfo threadInfo, List<StackTraceElement> stackTrace) {
		super();
		this.threadInfo = threadInfo;
		this.stackTrace = stackTrace;
	}

	public StackTrace(ThreadInfo threadInfo, AgentMessage.StackTrace stackTrace) {

		this.threadInfo = threadInfo;
		this.stackTrace = new ArrayList<StackTraceElement>();
		for (AgentMessage.StackTrace.StackTraceElement stackTraceElement : stackTrace
				.getStackTraceList()) {

			String declaringClass = stackTraceElement.getClassName();
			String methodName = stackTraceElement.getMethodName();
			String fileName = stackTraceElement.getFileName();
			int lineNumber = -1;

			this.stackTrace.add(new StackTraceElement(declaringClass,
					methodName, fileName, lineNumber));
		}

	}

	public List<StackTraceElement> getStackTrace() {
		return stackTrace;
	}

}
