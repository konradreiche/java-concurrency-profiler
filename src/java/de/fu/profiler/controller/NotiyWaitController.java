package de.fu.profiler.controller;

import java.util.Observable;
import java.util.Observer;

import javax.swing.JTextArea;

import de.fu.profiler.model.JVM;
import de.fu.profiler.model.ThreadInfo;

/**
 * Interacts with the model and view components when events regarding the notify
 * and wait mechanism occur.
 * 
 * @author Konrad Johannes Reiche
 * 
 */
public class NotiyWaitController implements Observer {

	/**
	 * mock-up
	 */
	JTextArea textArea;
	JVM currentJvm;

	public NotiyWaitController(JTextArea textArea) {
		super();
		this.textArea = textArea;
	}
	
	@Override
	public void update(Observable o, Object arg) {
		for (ThreadInfo thread : currentJvm.getThreads()) {
			if (o.equals(thread)) {
				textArea.append(thread.getMonitorStatus());
				textArea.repaint();
				break;
			}
		}
	}

	public void setCurrentJvm(JVM currentJvm) {
		this.currentJvm = currentJvm;
	}
	
	
}
