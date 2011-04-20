package de.fu.profiler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import de.fu.profiler.view.MainFrame;

/**
 * Provides the service to start new Threads handling the incoming connections.
 * 
 * @author Konrad Johannes Reiche
 * 
 */
public class Server extends ServerSocket implements Runnable {

	/**
	 * The main frame of the graphical interface.
	 */
	MainFrame mainFrame;

	/**
	 * Standard constructor.
	 * 
	 * @param port
	 *            the port on which the server socket will listen.
	 * @param mainFrame
	 *            the main frame of the graphical interface.
	 * @throws IOException
	 *             if an I/O error occurs when opening the socket.
	 */
	public Server(int port, MainFrame mainFrame) throws IOException {
		super(port);
		this.mainFrame = mainFrame;
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
				Thread service = new Thread(new ConnectionHandler(socket,
						mainFrame));
				service.start();
			}

		} catch (IOException e) {
			System.err.println("IOException: " + e.getMessage());
		}
	}
}
