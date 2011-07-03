package de.fu.profiler.view;

import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import de.fu.profiler.model.JVM;
import de.fu.profiler.model.ProfilerModel;

public class SynchronizedView extends JPanel {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4755669375223024154L;

	public SynchronizedView(ProfilerModel model, JVM jvm) {
		super(new GridLayout(1, 1));
		JTable synchronizedTable = new JTable(model.getSynchronizationTableModels().get(jvm));
		JScrollPane synchronizedLogScrollPane = new JScrollPane(synchronizedTable);
		super.add(synchronizedLogScrollPane);
	}
}
