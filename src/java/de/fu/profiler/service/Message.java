package de.fu.profiler.service;

import de.fu.profiler.model.JVM;
import de.fu.profiler.model.ProfilerModel;
import de.fu.profiler.service.AgentMessageProtos.AgentMessage;

public abstract class Message {

	final ProfilerModel profiler;
	final AgentMessage agentMessage;
	final JVM jvm;

	public Message(ProfilerModel profiler, JVM jvm, AgentMessage agentMessage) {
		super();
		this.profiler = profiler;
		this.jvm = jvm;
		this.agentMessage = agentMessage;
	}

	public abstract void execute();

	public abstract void undo();
}
