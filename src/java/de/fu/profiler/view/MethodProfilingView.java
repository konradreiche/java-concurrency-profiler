package de.fu.profiler.view;

import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import de.fu.profiler.model.JVM;
import de.fu.profiler.model.ProfilerModel;

public class MethodProfilingView extends JPanel {

	/**
	 * generated serial version ID
	 */
	private static final long serialVersionUID = -7086350340657083243L;

	public MethodProfilingView(ProfilerModel model, JVM jvm) {
		super(new GridLayout(1, 1));
		JTable timeTable = new JTable(model.getTimeTableModels().get(jvm));
		JScrollPane timePanel = new JScrollPane(timeTable);
		timeTable.getTableHeader().setReorderingAllowed(false);
		timeTable.setAutoCreateRowSorter(true);
		JScrollPane pane = new JScrollPane(timePanel);
		super.add(pane);
	}

}
