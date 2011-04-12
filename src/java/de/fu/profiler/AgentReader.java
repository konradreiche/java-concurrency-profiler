package de.fu.profiler;

import java.io.StringReader;
import java.util.Observer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import de.fu.profiler.controller.ThreadTableModel;
import de.fu.profiler.model.JVM;
import de.fu.profiler.model.ThreadInfo;
import de.fu.profiler.view.MainFrame;

public class AgentReader {

	MainFrame mainFrame;

	DocumentBuilderFactory dbf;
	DocumentBuilder db;
	XPath xpath;

	public AgentReader(MainFrame mainFrame) throws ParserConfigurationException {
		super();
		this.mainFrame = mainFrame;
		this.dbf = DocumentBuilderFactory.newInstance();
		this.db = dbf.newDocumentBuilder();
		this.xpath = XPathFactory.newInstance().newXPath();
	}

	public void readAgentData(String xml) throws XPathExpressionException {

		InputSource source = new InputSource(new StringReader(xml));
		Node root = (Node) xpath.evaluate("/", source, XPathConstants.NODE);

		int JVM_ID = Integer.parseInt(xpath.evaluate("/message/@jvm", root));
		JVM jvm = null;
		
		synchronized (mainFrame) {
			jvm = mainFrame.getProfiler().getIDsToJVMs().get(JVM_ID);
			if (jvm == null) {
				jvm = new JVM(JVM_ID, "default");
				System.out.println(jvm);
				mainFrame.getProfiler().getIDsToJVMs().put(JVM_ID, jvm);
				((ThreadTableModel) mainFrame.getTableModel())
						.setCurrentJVM(jvm);
				jvm.addObserver((Observer) mainFrame.getTableModel());
			}
		}

		String lifeCycle = xpath.evaluate("/message/threads/@lifeCycle", root);
		boolean isStart = (lifeCycle.equals("start")) ? true : false;

		NodeList threadNodes = (NodeList) xpath.evaluate(
				"/message/threads/thread", root, XPathConstants.NODESET);

		for (int i = 0; i < threadNodes.getLength(); ++i) {
			String name = xpath.evaluate("name", threadNodes.item(i));
			int priority = Integer.parseInt(xpath.evaluate("priority",
					threadNodes.item(i)));
			String state = xpath.evaluate("state", threadNodes.item(i));
			boolean ccl = Boolean.parseBoolean(xpath.evaluate(
					"isContextClassLoaderSet", threadNodes.item(i)));

			if (isStart) {
				jvm.addThread(new ThreadInfo(name, priority, state, ccl));
			}
		}
	}
}
