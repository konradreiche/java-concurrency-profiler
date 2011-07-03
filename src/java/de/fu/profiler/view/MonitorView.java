package de.fu.profiler.view;

import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import de.fu.profiler.controller.LockTableListener;
import de.fu.profiler.model.JVM;
import de.fu.profiler.model.ProfilerModel;

public class MonitorView extends JPanel {

	/**
	 * generated serial version ID
	 */
	private static final long serialVersionUID = 6969151448433873534L;
	
	public MonitorView(ProfilerModel model, JVM jvm) {
		super(new GridLayout(1, 1));
		JTable lockTable = new JTable(model.getLockTableModels().get(jvm));
		JTree monitorRelatedThreads = new JTree(new DefaultMutableTreeNode("Threads"));
		lockTable.getSelectionModel().addListSelectionListener(
				new LockTableListener(lockTable, model, monitorRelatedThreads));
		
		JScrollPane lockScrollPane = new JScrollPane(lockTable);
		lockScrollPane.setMinimumSize(new Dimension(700, 250));
		JSplitPane locksPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				lockScrollPane, new JScrollPane(monitorRelatedThreads));
		lockTable.setMinimumSize(new Dimension(700, 400));
		super.add(locksPanel);
	}

}
