package de.fu.profiler.controller;

import java.util.ArrayList;
import java.util.Map.Entry;

import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import de.fu.profiler.model.JVM;
import de.fu.profiler.model.LockTableModel;
import de.fu.profiler.model.Monitor;
import de.fu.profiler.model.ProfilerModel;
import de.fu.profiler.model.StackTrace;
import de.fu.profiler.model.ThreadInfo;

public class LockTableListener implements ListSelectionListener {

	JTable table;
	JTree tree;
	JVM jvm;
	ProfilerModel model;

	public LockTableListener(JTable table, ProfilerModel model, JTree tree) {
		super();
		this.table = table;
		this.model = model;
		this.tree = tree;
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {

		if (e.getSource() == table.getSelectionModel()) {

			JVM jvm = ((LockTableModel) table.getModel()).getJvm();
			int row = e.getFirstIndex();

			if (row == -1) {
				return;
			}
			
			Monitor monitor = new ArrayList<Monitor>(jvm.getMonitors().values())
					.get(row);
			

			DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree
					.getModel().getRoot();
			root.removeAllChildren();
			
			DefaultMutableTreeNode waiter = new DefaultMutableTreeNode("Waiter");
			DefaultMutableTreeNode notifyWaiter = new DefaultMutableTreeNode(
					"Notify Waiter");

			root.add(waiter);
			root.add(notifyWaiter);

			
			for (Entry<ThreadInfo, StackTrace> thread : monitor.getWaiter()
					.entrySet()) {
				
				
				
				DefaultMutableTreeNode newWaiterThread = new DefaultMutableTreeNode(
						thread.getKey().getName() + " ["
								+ thread.getKey().getState() + "]");
				waiter.add(newWaiterThread);

				StackTrace threadsStackTrace = thread.getValue();
				for (StackTraceElement stackTraceElement : threadsStackTrace
						.getStackTrace()) {
					newWaiterThread.add(new DefaultMutableTreeNode(
							stackTraceElement));
				}
			}

			for (Entry<ThreadInfo, StackTrace> thread : monitor.getNotifyWaiter()
					.entrySet()) {
				DefaultMutableTreeNode newNotifyWaiterThread = new DefaultMutableTreeNode(
						thread.getKey().getName() + " ["
								+ thread.getKey().getState() + "]");
				notifyWaiter.add(newNotifyWaiterThread);

				StackTrace threadsStackTrace = thread.getValue();
				for (StackTraceElement stackTraceElement : threadsStackTrace
						.getStackTrace()) {
					newNotifyWaiterThread.add(new DefaultMutableTreeNode(
							stackTraceElement));
				}
			}

			((DefaultTreeModel) tree.getModel()).reload();
			tree.repaint();

		}
	}
}
