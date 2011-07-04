package de.fu.profiler.controller;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.RowFilter;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultTreeModel;

import de.fu.profiler.model.JVM;
import de.fu.profiler.model.MonitorLogEntry;
import de.fu.profiler.model.MonitorLogTableModel;
import de.fu.profiler.model.ProfilerModel;
import de.fu.profiler.model.StackTrace;

public class MonitorLogTableListener implements ListSelectionListener,
		ItemListener {

	JTable table;
	JTree tree;
	JVM jvm;
	ProfilerModel model;

	JCheckBox cbContendedEnter;
	JCheckBox cbContendedEntered;
	JCheckBox cbWaiting;
	JCheckBox cbSignaling;

	MonitorLogTableModel tableModel;
	TableRowSorter<MonitorLogTableModel> sorter;

	RowFilter<Object, Object> filter1 = RowFilter.regexFilter("contended", 2);
	RowFilter<Object, Object> filter2 = RowFilter.regexFilter("entered", 2);
	RowFilter<Object, Object> filter3 = RowFilter.regexFilter("wait", 2);
	RowFilter<Object, Object> filter4 = RowFilter.regexFilter(".*notify.*", 2);
	List<RowFilter<Object, Object>> filterList = new ArrayList<RowFilter<Object, Object>>();

	public MonitorLogTableListener(JTable table, ProfilerModel model, JVM jvm,
			JTree tree, JCheckBox cbContendedEnter,
			JCheckBox cbContendedEntered, JCheckBox cbWaiting,
			JCheckBox cbSignaling) {

		super();
		this.table = table;
		this.model = model;
		this.jvm = jvm;
		this.tree = tree;
		this.cbContendedEnter = cbContendedEnter;
		this.cbContendedEntered = cbContendedEntered;
		this.cbWaiting = cbWaiting;
		this.cbSignaling = cbSignaling;
		tableModel = model.getMonitorLogTables().get(jvm);
		sorter = new TableRowSorter<MonitorLogTableModel>(tableModel);
		table.setRowSorter(sorter);
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {

		if (e.getSource() == table.getSelectionModel()) {

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
			model.getNotifyWaitStackTracesTrees().get(jvm).createTree(
					stackTraces);
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

	@Override
	public void itemStateChanged(ItemEvent e) {

		boolean b = e.getStateChange() == ItemEvent.DESELECTED;
		Object source = e.getSource();
		if (source == cbContendedEnter) {
			removerOrAdd(b, filter1);
		} else if (source == cbContendedEntered) {
			removerOrAdd(b, filter2);
		} else if (source == cbWaiting) {
			removerOrAdd(b, filter3);
		} else if (source == cbSignaling) {
			removerOrAdd(b, filter4);
		}

		synchronized (tableModel) {
			RowFilter<Object, Object> filter = RowFilter.notFilter(RowFilter
					.orFilter(filterList));
			sorter.setRowFilter(filter);
		}
	}

	private void removerOrAdd(boolean b, RowFilter<Object, Object> filter) {

		if (b) {
			filterList.add(filter);
		} else {
			filterList.remove(filter);
		}
	}

}
