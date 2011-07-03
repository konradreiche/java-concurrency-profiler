package de.fu.profiler.view;

import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import de.fu.profiler.model.ProfilerModel;

public class ResourceAllocationGraph extends JPanel {

	GraphBuilder graphBuilder;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ResourceAllocationGraph(ProfilerModel model) {
		super(new GridLayout(1, 1));
		graphBuilder = new GraphBuilder();
		super.add(new JScrollPane(graphBuilder.getWaitForGraph()));
	}

	
}
