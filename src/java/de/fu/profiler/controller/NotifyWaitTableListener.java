package de.fu.profiler.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultTreeModel;

import de.fu.profiler.model.JVM;
import de.fu.profiler.model.MonitorLogEntry;
import de.fu.profiler.model.MonitorLogTableModel;
import de.fu.profiler.model.ProfilerModel;
import de.fu.profiler.model.StackTrace;

public class NotifyWaitTableListener implements ListSelectionListener {

	JTable table;
	JTree tree;
	JVM jvm;
	ProfilerModel model;

	public NotifyWaitTableListener(JTable table, ProfilerModel model, JTree tree) {
		super();
		this.table = table;
		this.model = model;
		this.tree = tree;
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {

		if (e.getSource() == table.getSelectionModel()) {

			JVM jvm = ((MonitorLogTableModel) table.getModel()).getJvm();
			int row = e.getFirstIndex();

			MonitorLogEntry notifyWaitLogEntry = null;
			SortedSet<Long> sortedTimestamp = new TreeSet<Long>(jvm
					.getMonitorLog().keySet());

			long timestamp = -1;
			Iterator<Long> it = sortedTimestamp.iterator();
			for (int i = 0; i <= row && it.hasNext(); ++i) {
				timestamp = it.next();
			}

			if (timestamp == -1) {
				return;
			}

			notifyWaitLogEntry = jvm.getMonitorLog().get(timestamp);
			List<StackTrace> stackTraces = new ArrayList<StackTrace>(
					notifyWaitLogEntry.getStackTraces().values());
			model.getNotifyWaitStackTracesTrees().get(jvm)
					.createTree(stackTraces);
			((DefaultTreeModel) tree.getModel()).reload();
			expandAll(tree);
			tree.repaint();

		}
	}

	public void expandAll(JTree tree) {
		int row = 0;
		while (row < tree.getRowCount()) {
			tree.expandRow(row);
			row++;
		}
	}
}
