package de.fu.profiler.view;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;

import de.fu.profiler.controller.MethodProfilingFilterListener;
import de.fu.profiler.model.JVM;
import de.fu.profiler.model.ProfilerModel;

public class MethodProfilingView extends JPanel {

	/**
	 * generated serial version ID
	 */
	private static final long serialVersionUID = -7086350340657083243L;

	public MethodProfilingView(ProfilerModel model, JVM jvm) {
		super(new BorderLayout());
		JTable timeTable = new JTable(model.getTimeTableModels().get(jvm));
		JScrollPane timePanel = new JScrollPane(timeTable);
		timeTable.getTableHeader().setReorderingAllowed(false);
		timeTable.setAutoCreateRowSorter(true);
		JScrollPane pane = new JScrollPane(timePanel);

		JLabel filterLabel = new JLabel("Filter:");
		JTextField filterInput = new JTextField();
		JPanel filterPanel = new JPanel();
		filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.LINE_AXIS));
		filterPanel.add(filterLabel);
		filterPanel.add(filterInput);

		DocumentListener listener = new MethodProfilingFilterListener(
				filterInput, timeTable, model.getTimeTableModels().get(jvm));
		filterInput.getDocument().addDocumentListener(listener);

		super.add(pane, BorderLayout.CENTER);
		super.add(filterPanel, BorderLayout.SOUTH);
	}

}
