package de.fu.profiler.service;

import java.io.IOException;
import java.net.Socket;

import de.fu.profiler.model.JVM;
import de.fu.profiler.model.ProfilerModel;
import de.fu.profiler.service.AgentMessageProtos.AgentMessage;

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
	private ProfilerModel profiler;

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
		this.profiler = profilerModel;
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

		while (true) {

			try {
				AgentMessage agentMessage = AgentMessageProtos.AgentMessage
						.parseDelimitedFrom(socket.getInputStream());

				if (agentMessage == null) {
					break;
				}

				String host = socket.getInetAddress().getCanonicalHostName();
				int pid = agentMessage.getJvmId();
				long timestamp = agentMessage.getTimestamp();
				JVM jvm = null;

				synchronized (profiler.getIDsToJVMs()) {
					long deltaSystemTime = agentMessage.getSystemTime();
					jvm = profiler.newJvmInstance(pid, "default", host,
							deltaSystemTime);
				}

				if (agentMessage.hasThreadEvent()) {
					ThreadMessage message = new ThreadMessage(profiler, jvm,
							agentMessage);
					message.execute();
					profiler.addMessage(jvm, timestamp, message);
				}

				if (agentMessage.hasMonitorEvent()) {
					MonitorMessage message = new MonitorMessage(profiler, jvm,
							agentMessage);
					message.execute();
					profiler.addMessage(jvm, timestamp, message);
				}

				if (agentMessage.hasMethodEvent()) {
					MethodMessage message = new MethodMessage(profiler, jvm,
							agentMessage);
					message.execute();
					profiler.addMessage(jvm, timestamp, message);
				}

				profiler.notifyGUI(jvm);
			} catch (IOException e) {
				// System.err.println(e.getMessage());
			}
		}

	}

}
