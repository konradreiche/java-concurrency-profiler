package de.fu.profiler;

import java.io.IOException;
import java.net.Socket;
import java.util.Observer;

import javax.xml.parsers.ParserConfigurationException;

import de.fu.profiler.controller.ThreadTableModel;
import de.fu.profiler.model.JVM;
import de.fu.profiler.model.ThreadInfo;
import de.fu.profiler.protobuf.AgentMessageProtos;
import de.fu.profiler.view.MainFrame;

public class ConnectionHandler implements Runnable {

	Socket socket;
	MainFrame mainFrame;

	public ConnectionHandler(Socket socket, MainFrame mainFrame)
			throws ParserConfigurationException {
		super();
		this.socket = socket;
		this.mainFrame = mainFrame;
	}

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
					jvm.addObserver((Observer) mainFrame.getTableModel());
				}
			}

			boolean isStart = agentMessage.getThreads().getLifeCycle()
					.equals("start") ? true : false;

			for (de.fu.profiler.protobuf.AgentMessageProtos.AgentMessage.Threads.Thread thread : agentMessage
					.getThreads().getThreadList()) {

				if (isStart) {

					ThreadInfo threadInfo = new ThreadInfo(thread.getId(),
							thread.getName(), thread.getPriority(),
							thread.getState(),
							thread.getIsContextClassLoaderSet());

					if (jvm.getThreads().contains(threadInfo)) {
						jvm.getThreads().remove(threadInfo);
						System.out.println("Updating Thread "
								+ threadInfo.getName());
					}

					jvm.addThread(threadInfo);
				}

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
