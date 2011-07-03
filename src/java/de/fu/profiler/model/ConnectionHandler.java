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

			
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					
					while (true) {
						
						if (profilerModel.hasChanged) {
							profilerModel.hasChanged = false;
						}
						
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
				}
			}).start();
			
			while (true) {
				
				try {
					AgentMessageProtos.AgentMessage agentMessage = AgentMessageProtos.AgentMessage
					.parseDelimitedFrom(socket.getInputStream());
					
					String host = socket.getInetAddress().getCanonicalHostName();
					profilerModel.applyData(agentMessage,true,host);					
				} catch (IOException e) {
					// swallow
				}
				
			}

		
	}

	
}
