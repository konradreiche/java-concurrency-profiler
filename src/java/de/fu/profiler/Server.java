package de.fu.profiler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.xml.parsers.ParserConfigurationException;

import de.fu.profiler.view.MainFrame;


public class Server extends ServerSocket implements Runnable {

	MainFrame mainFrame;
	
	public Server(int port, MainFrame mainFrame) throws IOException {
		super(port);
		this.mainFrame = mainFrame;
	}

	@Override
	public void run() {
		try {
			while (true) {
				Socket socket = this.accept();
				Thread service = new Thread(new ConnectionHandler(socket,mainFrame));
				service.start();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
