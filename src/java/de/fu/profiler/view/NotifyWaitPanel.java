package de.fu.profiler.view;

import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;

import de.fu.profiler.controller.NotifyWaitTableListener;
import de.fu.profiler.model.ProfilerModel;

/**
 * This panel shows information about the notify and wait methods used by the
 * profiled JVM.
 * 
 * @author Konrad Johannes Reiche
 * 
 */
public class NotifyWaitPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1308837298229481853L;

	JTree stackTraces;

	public NotifyWaitPanel(ProfilerModel model) {

		super(new GridLayout(2, 1));
		JTable notifyWaitTable = new JTable(model.getNotifyWaitTableModel());
		JScrollPane notifyWaitLogScrollPane = new JScrollPane(notifyWaitTable);
		stackTraces = new JTree(model.getTreeNode());
		stackTraces.setExpandsSelectedPaths(true);

		notifyWaitTable.getSelectionModel()
				.addListSelectionListener(
						new NotifyWaitTableListener(notifyWaitTable, model,
								stackTraces));

		JTable threadStatsTable = new JTable(model.getThreadStatsTableModel());

		JTabbedPane tabbedNotifyWaitPane = new JTabbedPane();
		tabbedNotifyWaitPane.add("Stack Traces", new JScrollPane(stackTraces));
		tabbedNotifyWaitPane
				.add("Statistic", new JScrollPane(threadStatsTable));

		notifyWaitTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		notifyWaitTable.setRowSelectionAllowed(true);
		notifyWaitLogScrollPane = new JScrollPane(notifyWaitTable);
		super.add(notifyWaitLogScrollPane);
		super.add(tabbedNotifyWaitPane);

	}
}
