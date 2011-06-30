package de.fu.profiler.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.tree.DefaultMutableTreeNode;

import de.fu.profiler.controller.LockTableListener;
import de.fu.profiler.model.ProfilerModel;

/**
 * The graphical user interface of the profiler. Further elements of the
 * graphical user interface are initialized during the constructor of this
 * {@link JFrame}.
 * 
 * @author Konrad Johannes Reiche
 * 
 */
public class ProfilerView extends JFrame {

	
	/**
	 * generated serial version ID 
	 */
	private static final long serialVersionUID = -6276797391162392835L;

	/**
	 * The profiler model
	 */
	ProfilerModel model;

	/**
	 * This panel shows information about the locks used by the profiled JVM.
	 */
	JPanel locksPanel;

	/**
	 * This panel shows information about the synchronized events which occur.
	 */
	JPanel synchronizedPanel;

	/**
	 * The tabbed pane enables to select different views in the profiler.
	 */
	JTabbedPane tabbedPane;

	/**
	 * The tabbed pane enables to select different diagrams.
	 */
	JTabbedPane tabbedDiagramPane;

	

	/**
	 * The split pane splits the whole frame into two sides. One side is for the
	 * JVM selection and one is for the information views.
	 */
	JSplitPane splitPane;

	/**
	 * The scroll pane for the notify wait log.
	 */
	JScrollPane notifyWaitLogScrollPane;

	/**
	 * The scroll pane for the synchronized log.
	 */
	JScrollPane synchronizedLogScrollPane;

	JTable lockTable;

	/**
	 * Displays the current event number.
	 */
	JLabel eventLabel;

	/**
	 * Helper class to create the graphs.
	 */
	GraphBuilder graphBuilder;

	/**
	 * The component displaying the notify wait graph.
	 */
	Component notifyWaitGraph;

	/**
	 * Table for displaying the statistical numbers about the threads concerned
	 * with notify and wait
	 */
	JTable threadStatsTable;

	/**
	 * The panel displaying the constructed wait-for graph
	 */
	JScrollPane waitForGraphPanel;

	/**
	 * The panel containing the table with all the information about the methods
	 * which were profiled.
	 */
	JScrollPane timePanel;

	JTree monitorRelatedThreads;

	NotifyWaitPanel notifyWaitPanel;

	public ProfilerView(ProfilerModel model) {

		this.model = model;
		super.setTitle("Java Concurrency Profiler");
		
		setUpLookAndFeel();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new GridLayout(1, 2));
		
		graphBuilder = new GraphBuilder();

		this.lockTable = new JTable(model.getLockTableModel());
		monitorRelatedThreads = new JTree(new DefaultMutableTreeNode("Threads"));
		this.lockTable.getSelectionModel().addListSelectionListener(
				new LockTableListener(lockTable, model, monitorRelatedThreads));

		this.synchronizedPanel = new JPanel(new GridLayout(1, 1));
		JTable synchronizedTable = new JTable(model.getSynchronizedTableModel());
		this.synchronizedLogScrollPane = new JScrollPane(synchronizedTable);
		this.synchronizedPanel.add(synchronizedLogScrollPane);

		this.waitForGraphPanel = new JScrollPane(graphBuilder.getWaitForGraph());

		JTable timeTable = new JTable(model.getTimeTableModel());
		timeTable.getTableHeader().setReorderingAllowed(false);
		timeTable.setAutoCreateRowSorter(true);

		this.timePanel = new JScrollPane(timeTable);

		JScrollPane lockScrollPane = new JScrollPane(lockTable);
		lockScrollPane.setMinimumSize(new Dimension(700, 250));
		JSplitPane locksPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				lockScrollPane, new JScrollPane(monitorRelatedThreads));

		this.lockTable.setMinimumSize(new Dimension(700, 400));

		this.tabbedPane = new JTabbedPane();
		this.tabbedPane.add("Overview", new OverviewPanel(model, model.getTableModel()));
		this.tabbedPane.add("Notify/Wait", notifyWaitPanel);
		this.tabbedPane.add("Locks", locksPanel);
		this.tabbedPane.add("Synchronized", synchronizedPanel);
		this.tabbedPane.add("Wait-For Graph", waitForGraphPanel);
		this.tabbedPane.add("Time", timePanel);

		this.splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		this.splitPane.add(tabbedPane);
		
		
		JTabbedPane jvmSelection = new JTabbedPane(JTabbedPane.LEFT);
		jvmSelection.add("Welcome", new WelcomePanel());
		jvmSelection.add("Main", splitPane);
		this.add(jvmSelection);

		this.setSize(700, 800);
	}



	

	private void setUpLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	}
}
