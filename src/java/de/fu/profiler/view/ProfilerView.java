package de.fu.profiler.view;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.util.Rotation;

import de.fu.profiler.controller.ProfilerController.JVMSelectionListener;
import de.fu.profiler.model.ProfilerModel;

/**
 * The graphical user interface of the profiler.
 * 
 * @author Konrad Johannes Reiche
 * 
 */
public class ProfilerView extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * The profiler itself.
	 */
	ProfilerModel model;

	/**
	 * The table displays the threads of the JVM.
	 */
	JTable table;

	/**
	 * The list for displaying the available JVMs.
	 */
	JList list;

	/**
	 * The panel contains the selection for the different JVMs.
	 */
	JPanel jvmSelection;

	/**
	 * The scroll pane containing the JVM list.
	 */
	JScrollPane jvmSelectionScrollPane;

	/**
	 * The scroll pane containing the thread table.
	 */
	JScrollPane threadTableScrollPane;

	/**
	 * This panels shows a general overview on the profiled JVM.
	 */
	JPanel overviewPanel;

	/**
	 * This panel shows information about the notify and wait methods used by
	 * the profiled JVM.
	 */
	JPanel notifyWaitPanel;

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
	 * The split pane splits the whole frame into two sides. One side is for the
	 * JVM selection and one is for the information views.
	 */
	JSplitPane splitPane;

	/**
	 * A text area which displays the logged data of the notify and wait events.
	 */
	JTextArea notifyWaitLogTextArea;

	/**
	 * The scroll pane for the notify wait log.
	 */
	JScrollPane notifyWaitLogScrollPane;
	
	/**
	 * The scroll pane for the synchronized log.
	 */
	JScrollPane synchronizedLogScrollPane;
	
	/**
	 * A text area which displays the logged data of the synchronized events.
	 */
	JTextArea synchronizedLogTextArea;

	/**
	 * A box to select one of the available monitors to view their information.
	 */
	JComboBox monitorSelection;

	/**
	 * Label which displays the number of times the owning thread has entered
	 * the monitor.
	 */
	JLabel monitorEntryCount;

	/**
	 * Label which displays the number number of threads waiting to own this
	 * monitor.
	 */
	JLabel monitorWaiterCount;

	/**
	 * Label which displays the number of threads waiting to be notified by this
	 * monitor.
	 */
	JLabel monitorNotifyWaiterCount;

	/**
	 * Button to display the next event from the event history.
	 */
	JButton nextEvent;

	/**
	 * Button to display the previous event from the event history-
	 */
	JButton previousEvent;

	/**
	 * Packages all elements which help to navigate through the available
	 * events.
	 */
	JPanel eventNavigation;

	/**
	 * Displays the current event number.
	 */
	JLabel eventLabel;

	public ProfilerView(ProfilerModel model) {

		this.model = model;
		setUpLookAndFeel();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new GridLayout(1, 1));

		this.table = new JTable(model.getTableModel());
		this.list = new JList(new DefaultListModel());
		this.threadTableScrollPane = new JScrollPane(table);

		this.nextEvent = new JButton(">>");
		this.previousEvent = new JButton("<<");
		this.eventLabel = new JLabel("Event #?");
		this.eventNavigation = new JPanel();
		this.eventNavigation.add(previousEvent);
		this.eventNavigation.add(eventLabel);
		this.eventNavigation.add(nextEvent);

		this.jvmSelection = new JPanel(new GridLayout(3, 1));
		this.jvmSelection.add(new JLabel("Available JVMs"));
		this.jvmSelectionScrollPane = new JScrollPane(list);
		this.jvmSelection.add(jvmSelectionScrollPane);
		this.jvmSelection.add(eventNavigation);

		this.overviewPanel = new JPanel(new GridLayout(2, 1));
		this.overviewPanel.add(threadTableScrollPane);
		this.overviewPanel.add(setUpThreadPieChart());

		this.notifyWaitPanel = new JPanel(new GridLayout(1, 1));
		this.notifyWaitLogTextArea = new JTextArea();
		this.notifyWaitLogScrollPane = new JScrollPane(notifyWaitLogTextArea);
		this.notifyWaitPanel.add(notifyWaitLogScrollPane);

		this.monitorSelection = new JComboBox();
		this.monitorEntryCount = new JLabel("Entry Count: N/A");
		this.monitorWaiterCount = new JLabel("Waiter Count: N/A");
		this.monitorNotifyWaiterCount = new JLabel("Notify Waiter Count: N/A");

		this.locksPanel = new JPanel();
		this.locksPanel.add(monitorSelection);
		this.locksPanel.add(monitorEntryCount);
		this.locksPanel.add(monitorWaiterCount);
		this.locksPanel.add(monitorNotifyWaiterCount);
		
		this.synchronizedPanel = new JPanel(new GridLayout(1, 1));
		this.synchronizedLogTextArea = new JTextArea();
		this.synchronizedLogScrollPane = new JScrollPane(synchronizedLogTextArea);
		this.synchronizedPanel.add(synchronizedLogScrollPane);
		
		this.tabbedPane = new JTabbedPane();
		this.tabbedPane.add("Overview", overviewPanel);
		this.tabbedPane.add("Notify/Wait", notifyWaitPanel);
		this.tabbedPane.add("Locks", locksPanel);
		this.tabbedPane.add("Synchronized", synchronizedPanel);

		this.splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		this.splitPane.add(jvmSelection);
		this.splitPane.add(tabbedPane);

		this.add(splitPane);
		this.jvmSelectionScrollPane.setMinimumSize(new Dimension(700, 35));
		this.setSize(700, 700);
	}

	private ChartPanel setUpThreadPieChart() {

		JFreeChart chart = ChartFactory.createPieChart3D("Threads", model
				.getThreadPieDataset(), true, true, false);
		PiePlot3D plot = (PiePlot3D) chart.getPlot();
		plot.setStartAngle(290);
		plot.setDirection(Rotation.CLOCKWISE);
		plot.setForegroundAlpha(0.5f);

		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new Dimension(300, 200));
		return chartPanel;
	}

	private void setUpLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void addJVMSelectionListener(
			JVMSelectionListener jvmSelectionListener) {
		this.list.addListSelectionListener(jvmSelectionListener);
	}

	public void addMonitorSelectionListener(ActionListener actionListener) {
		this.monitorSelection.addActionListener(actionListener);
	}

	public void addNextEventListener(ActionListener actionListener) {
		this.nextEvent.addActionListener(actionListener);
	}

	public void addPreviousEventListener(ActionListener actionListener) {
		this.previousEvent.addActionListener(actionListener);
	}

	public void setMonitorLabels(int entryCount, int waiterCount,
			int notifyWaiterCount) {

		monitorEntryCount.setText("Entry Count: " + entryCount);
		monitorWaiterCount.setText("Waiter Count: " + waiterCount);
		monitorNotifyWaiterCount.setText("Notify Waiter Count: "
				+ notifyWaiterCount);
	}

	public void setEnabledPreviousEventButton(boolean isEnabled) {
		previousEvent.setEnabled(isEnabled);
	}
}
