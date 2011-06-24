package de.fu.profiler.controller;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultTreeModel;

import de.fu.profiler.model.JVM;
import de.fu.profiler.model.NotifyWaitLogEntry;
import de.fu.profiler.model.NotifyWaitTableModel;
import de.fu.profiler.model.ProfilerModel;

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

			JVM jvm = ((NotifyWaitTableModel) table.getModel()).getJvm();
			int row = e.getFirstIndex();

			NotifyWaitLogEntry notifyWaitLogEntry = null;
			SortedSet<Long> sortedTimestamp = new TreeSet<Long>(jvm
					.getNotifyWaitLog().keySet());

			long timestamp = -1;
			Iterator<Long> it = sortedTimestamp.iterator();
			for (int i = 0; i <= row && it.hasNext(); ++i) {
				timestamp = it.next();
			}

			if (timestamp == -1) {
				return;
			}

			notifyWaitLogEntry = jvm.getNotifyWaitLog().get(timestamp);
			model.getStackTracesTree().createTree(
					notifyWaitLogEntry.getStackTraces());
			((DefaultTreeModel)tree.getModel()).reload();
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
