package de.fu.profiler.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import de.fu.profiler.model.JVM;
import de.fu.profiler.model.ProfilerModel;
import de.fu.profiler.model.ThreadInfo;
import de.fu.profiler.service.AgentMessageProtos.AgentMessage;
import de.fu.profiler.service.AgentMessageProtos.AgentMessage.ThreadEvent;

public class ThreadMessage extends Message {

	Map<AgentMessage.Thread, Boolean> isNewThread;
	Map<AgentMessage.Thread, ThreadInfo> previousThreadInfos;

	public ThreadMessage(ProfilerModel profiler, JVM jvm,
			AgentMessage agentMessage) {

		super(profiler, jvm, agentMessage);
		isNewThread = new HashMap<AgentMessageProtos.AgentMessage.Thread, Boolean>();
		previousThreadInfos = new HashMap<AgentMessageProtos.AgentMessage.Thread, ThreadInfo>();
	}

	@Override
	public void execute() {

		long timestamp = agentMessage.getTimestamp();
		ThreadEvent event = agentMessage.getThreadEvent();
		for (AgentMessage.Thread thread : event.getThreadList()) {

			ThreadInfo threadInfo = jvm.getThread(thread.getId());
			if (threadInfo == null) {
				threadInfo = new ThreadInfo(thread, timestamp);
				jvm.addThread(threadInfo);
				isNewThread.put(thread, true);
			} else {
				isNewThread.put(thread, false);
				previousThreadInfos.put(thread, threadInfo.copy());

				long systemTime = agentMessage.getSystemTime();
				int id = thread.getId();
				String name = thread.getName();
				int priority = thread.getPriority();
				String state = ThreadInfo.formatThreadState(thread.getState());
				boolean isContextClassLoaderSet = thread
						.getIsContextClassLoaderSet();

				threadInfo.update(systemTime, id, name, priority, state,
						isContextClassLoaderSet);

				if (thread.hasCpuTime()) {
					threadInfo.setCpuTime(thread.getCpuTime());
				}

			}
		}

	}

	@Override
	public void undo() {

		for (Entry<AgentMessage.Thread, Boolean> entry : isNewThread.entrySet()) {
			if (entry.getValue()) {
				jvm.removeThread(entry.getKey().getId());
			} else {
				jvm.addThread(previousThreadInfos.get(entry.getKey()));
			}
		}

	}

}
