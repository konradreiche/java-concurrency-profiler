package de.fu.profiler.model;

import java.io.IOException;
import java.net.Socket;

/**
 * Handles incoming connects and parses the incoming data. Based on the incoming
 * data the model is updated.
 * 
 * @author Konrad Johannes Reiche
 * 
 */
public class ConnectionHandler implements Runnable {

	/**
	 * The client socket.
	 */
	Socket socket;

	/**
	 * The profiler itself.
	 */
	private ProfilerModel profilerModel;

	/**
	 * Standard constructor.
	 * 
	 * @param profilerModel
	 * 
	 * @param socket
	 *            the client socket
	 */
	public ConnectionHandler(ProfilerModel profilerModel, Socket socket) {
		super();
		this.profilerModel = profilerModel;
		this.socket = socket;
	}

	/**
	 * Parses one protobuf message and updates the model due to the stored
	 * information. The message fields are checked for being set. If the message
	 * fields are set their information can be used to update the model.
	 * <p>
	 * The model is chosen based on the generated id of the profiled JVM. Access
	 * to the list of JVMs has to be synchronized as access to it might be done
	 * by many {@link ConnectionHandler}.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {

		try {
			AgentMessageProtos.AgentMessage agentMessage = AgentMessageProtos.AgentMessage
					.parseDelimitedFrom(socket.getInputStream());

			int jvm_id = agentMessage.getJvmId();
			JVM jvm = null;

			synchronized (profilerModel.IDsToJVMs) {
				jvm = profilerModel.IDsToJVMs.get(jvm_id);
				if (jvm == null) {
					jvm = new JVM(jvm_id, "default");
					profilerModel.IDsToJVMs.put(jvm_id, jvm);
					((ThreadTableModel) profilerModel.getTableModel())
							.setCurrentJVM(jvm);
					profilerModel.setCurrentJVM(jvm);
				}
			}

			if (agentMessage.hasThreadEvent()) {
				for (de.fu.profiler.model.AgentMessageProtos.AgentMessage.Thread thread : agentMessage
						.getThreadEvent().getThreadList()) {

					ThreadInfo threadInfo = new ThreadInfo(thread.getId(),
							thread.getName(), thread.getPriority(), thread
									.getState().toString(),
							thread.getIsContextClassLoaderSet());

					if (thread.hasCpuTime()) {
						threadInfo.setCpuTime(thread.getCpuTime());
					}
					
					profilerModel.addThreadInfo(jvm_id, threadInfo);					
				}
			}

			if (agentMessage.hasMonitorEvent()) {

				de.fu.profiler.model.AgentMessageProtos.AgentMessage.Thread t = agentMessage
						.getMonitorEvent().getThread();

				ThreadInfo thread = jvm.getThread(agentMessage
						.getMonitorEvent().getThread().getId());

				if (thread == null) {
					thread = new ThreadInfo(t.getId(), t.getName(),
							t.getPriority(), t.getState().toString(),
							t.getIsContextClassLoaderSet());
					profilerModel.addThreadInfo(jvm_id, thread);
				}

				profilerModel.setThreadInfoState(jvm_id, thread, agentMessage
						.getMonitorEvent().getThread().getState().toString());

				profilerModel.addThreadInfo(jvm_id, thread);

				switch (agentMessage.getMonitorEvent().getEventType()) {
				case WAIT:
					profilerModel.setThreadInfoMonitorStatus(jvm_id, thread, thread.getName() + " invoked"
							+ " wait()\n");
					thread.increaseWaitCounter();
					break;
				case WAITED:
					profilerModel.setThreadInfoMonitorStatus(jvm_id, thread, thread.getName() + " left"
							+ " wait()\n");
					break;
				case NOTIFY_ALL:
					profilerModel.setThreadInfoMonitorStatus(jvm_id, thread, thread.getName() + " invoked"
							+ " notifyAll()\n");
					break;
				}
			}

		} catch (IOException e) {
			System.err.println("IOException: " + e.getMessage());
			e.printStackTrace();
		}

	}
}
