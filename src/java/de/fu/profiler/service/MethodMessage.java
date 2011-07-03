package de.fu.profiler.service;

import de.fu.profiler.model.JVM;
import de.fu.profiler.model.MethodInfo;
import de.fu.profiler.model.ProfilerModel;
import de.fu.profiler.model.ThreadInfo;
import de.fu.profiler.service.AgentMessageProtos.AgentMessage;
import de.fu.profiler.service.AgentMessageProtos.AgentMessage.MethodEvent;

public class MethodMessage extends Message {

	boolean isNewMethod;

	public MethodMessage(ProfilerModel profiler, JVM jvm,
			AgentMessage agentMessage) {
		super(profiler, jvm, agentMessage);
	}

	@Override
	public void execute() {

		MethodEvent event = agentMessage.getMethodEvent();
		String className = event.getClassName();
		String methodName = event.getMethodName();
		String identifier = className + "." + methodName;
		long clockCycles = event.getClockCycles();
		long timeTaken = event.getTimeTaken();
		ThreadInfo caller = jvm.getThread(event.getThread().getId());

		MethodInfo methodInfo = jvm.getMethods().get(identifier);
		if (methodInfo == null) {
			isNewMethod = true;
			methodInfo = new MethodInfo(className, methodName, clockCycles,
					timeTaken, caller);
			jvm.getMethods().put(identifier, methodInfo);
		} else {
			methodInfo.update(clockCycles, timeTaken, caller);
		}

		MethodInfo.updateRelativeTime(jvm);
	}

	@Override
	public void undo() {

		MethodEvent event = agentMessage.getMethodEvent();
		String className = event.getClassName();
		String methodName = event.getMethodName();
		String identifier = className + "." + methodName;
		long clockCycles = event.getClockCycles();
		long timeTaken = event.getTimeTaken();
		ThreadInfo caller = jvm.getThread(event.getThread().getId());

		if (isNewMethod) {
			jvm.getMethods().remove(identifier);
		} else {
			MethodInfo methodInfo = jvm.getMethods().get(identifier);
			methodInfo.revert(clockCycles, timeTaken, caller);
		}
	}

}
