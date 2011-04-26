package de.fu.profiler;

import java.io.IOException;
import java.net.Socket;
import java.util.Observer;

import de.fu.profiler.controller.ThreadTableModel;
import de.fu.profiler.model.JVM;
import de.fu.profiler.model.ThreadInfo;
import de.fu.profiler.view.MainFrame;

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
	 * The main frame of the graphical interface.
	 */
	MainFrame mainFrame;

	/**
	 * Standard constructor.
	 * 
	 * @param socket
	 *            the client socket
	 * @param mainFrame
	 *            the main frame of the graphical interface
	 */
	public ConnectionHandler(Socket socket, MainFrame mainFrame) {
		super();
		this.socket = socket;
		this.mainFrame = mainFrame;
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

			synchronized (mainFrame) {
				jvm = mainFrame.getProfiler().getIDsToJVMs().get(jvm_id);
				if (jvm == null) {
					jvm = new JVM(jvm_id, "default");
					mainFrame.getProfiler().getIDsToJVMs().put(jvm_id, jvm);
					((ThreadTableModel) mainFrame.getTableModel())
							.setCurrentJVM(jvm);
					mainFrame.getNotifyWaitController().setCurrentJvm(jvm);
					jvm.addObserver((Observer) mainFrame.getTableModel());
				}
			}

			if (agentMessage.hasThreadEvent()) {
				for (de.fu.profiler.AgentMessageProtos.AgentMessage.Thread thread : agentMessage
						.getThreadEvent().getThreadList()) {

					ThreadInfo threadInfo = new ThreadInfo(thread.getId(),
							thread.getName(), thread.getPriority(), thread
									.getState().toString(),
							thread.getIsContextClassLoaderSet(),
							mainFrame.getNotifyWaitController());

					if (jvm.getThreads().contains(threadInfo)) {
						jvm.getThreads().remove(threadInfo);
					}

					jvm.addThread(threadInfo);
				}
			}

			if (agentMessage.hasMonitorEvent()) {
				ThreadInfo thread = jvm.getThread(agentMessage
						.getMonitorEvent().getThread().getId());

				thread.setState(agentMessage.getMonitorEvent().getThread()
						.getState().toString());
				
				if (jvm.getThreads().contains(thread)) {
					jvm.getThreads().remove(thread);
				}

				jvm.addThread(thread);

				switch (agentMessage.getMonitorEvent().getEventType()) {
				case WAIT:
					thread.changeMonitorStatus("Thread " + thread.getName()
							+ " wait()\n");
					break;
				case WAITED:
					thread.changeMonitorStatus("Thread " + thread.getName()
							+ " wait() exit\n");
					break;
				case NOTIFY_ALL:
					thread.changeMonitorStatus("Thread " + thread.getName()
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
