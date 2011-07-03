package de.fu.profiler.view;

import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * A {@link JPanel} which constructs the welcome tab in the profiler.
 * 
 * @author Konrad Johannes Reiche
 * 
 */
public class WelcomePanel extends JPanel {

	/**
	 * generated serial version ID
	 */
	private static final long serialVersionUID = -6274578324488247345L;

	public WelcomePanel() {
		super();
		super.setBorder(BorderFactory
				.createTitledBorder("Monitor New Applications"));

		JLabel manual = new JLabel();
		manual.setPreferredSize(new Dimension(600, 300));

		manual.setText(readTextFile("doc/help.html"));
		super.add(manual);
	}

	public static String readTextFile(String path) {

		File file = new File(path);
		StringBuilder content = new StringBuilder();
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(file));
			String text = null;
			while ((text = reader.readLine()) != null) {
				content.append(text).append(
						System.getProperty("line.separator"));
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return content.toString();
	}

}
