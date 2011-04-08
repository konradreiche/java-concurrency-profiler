package de.fu.profiler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import de.fu.profiler.view.MainFrame;

public class ConnectionHandler implements Runnable {

	Socket socket;
	MainFrame mainFrame;
	AgentReader agentReader;

	public ConnectionHandler(Socket socket, MainFrame mainFrame) throws ParserConfigurationException {
		super();
		this.socket = socket;
		this.mainFrame = mainFrame;
		this.agentReader = new AgentReader(mainFrame);
	}

	@Override
	public void run() {

		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			StringBuilder xmlMessage = new StringBuilder();

			while (true) {

				String message = in.readLine();
				if (message == null) {
					break;
				}

				xmlMessage.append(message);
				xmlMessage.append("\n");
				if (message.equals("</message>")) {
					System.out.println(xmlMessage);
					agentReader.readAgentData(xmlMessage.toString());
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
