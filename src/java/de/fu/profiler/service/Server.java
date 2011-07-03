package de.fu.profiler.service;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import de.fu.profiler.model.ProfilerModel;

/**
 * Provides the service to start new Threads handling the incoming connections.
 * 
 * @author Konrad Johannes Reiche
 * 
 */
public class Server extends ServerSocket implements Runnable {

	/**
	 * The profiler itself.
	 */
	private ProfilerModel profilerModel;

	/**
	 * Standard constructor.
	 * @param profilerModel 
	 * 
	 * @param port
	 *            the port on which the server socket will listen.
	 * @throws IOException
	 *             if an I/O error occurs when opening the socket.
	 */
	public Server(ProfilerModel profilerModel, int port) throws IOException {
		super(port);
		this.profilerModel = profilerModel;
	}

	/**
	 * In order to handle the incoming connections new Thread with the
	 * {@link ConnectionHandler} runnable are launched.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			while (true) {
				Socket socket = this.accept();
				Thread service = new Thread(new ConnectionHandler(profilerModel,socket));
				service.start();
			}

		} catch (IOException e) {
			System.err.println("IOException: " + e.getMessage());
		}
	}
}
