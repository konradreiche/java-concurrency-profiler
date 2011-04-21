package de.fu.profiler.view;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.IOException;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.TableModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.util.Rotation;

import de.fu.profiler.Server;
import de.fu.profiler.controller.ThreadTableModel;
import de.fu.profiler.model.JVM;
import de.fu.profiler.model.Profiler;

/**
 * The graphical user interface of the profiler.
 * 
 * @author Konrad Johannes Reiche
 * 
 */
public class MainFrame extends JFrame {

	/**
	 * Generated serial version ID
	 */
	private static final long serialVersionUID = -6342475722971843766L;

	/**
	 * The profiler itself.
	 */
	Profiler profiler;

	/**
	 * The table displays the threads of the JVM.
	 */
	JTable table;

	/**
	 * The table model for displaying the threads of the JVM.
	 */
	TableModel tableModel;

	/**
	 * The list model for displaying the available JVMs.
	 */
	DefaultListModel listModel;

	/**
	 * The list for displaying the available JVMs.
	 */
	JList list;

	/**
	 * The dataset on which the thread state pie chart is based on.
	 */
	DefaultPieDataset threadPieChartDataset;

	/**
	 * Initializes the elements for the graphical interface.
	 * 
	 * @param profiler
	 *            the profiler itself.
	 * @throws ClassNotFoundException
	 *             if the <code>LookAndFeel</code> class could not be found
	 * @throws InstantiationException
	 *             if a new instance of the class couldn't be created
	 * @throws IllegalAccessException
	 *             if the class or initializer isn't accessible
	 * @throws UnsupportedLookAndFeelException
	 *             if <code>lnf.isSupportedLookAndFeel()</code> is false
	 */
	public MainFrame(Profiler profiler) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException,
			UnsupportedLookAndFeelException {

		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		this.profiler = profiler;
		this.setLayout(new GridLayout(1, 1));
		
		Component pieChart = createPieChart();

		tableModel = new ThreadTableModel(threadPieChartDataset);
		listModel = new DefaultListModel();
		this.table = new JTable(tableModel);
		this.list = new JList(listModel);

		JPanel overview = new JPanel(new GridLayout(2, 1));
		JScrollPane scrollPane = new JScrollPane(table);
		JTabbedPane tabbedPane = new JTabbedPane();

		overview.add(scrollPane);
		overview.add(pieChart);

		tabbedPane.add("Overview", overview);

		for (JVM jvm : profiler.getIDsToJVMs().values()) {
			listModel.addElement("JVM " + "(pid: " + jvm.getId() + ")");
		}

		table.setFillsViewportHeight(true);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
				list, tabbedPane);
		this.add(splitPane);

		this.setSize(700, 700);
		this.setVisible(true);
	}

	/**
	 * Initializes the pie chart illustrating the threads state.
	 * 
	 * @return the pie chart component.
	 */
	private Component createPieChart() {

		threadPieChartDataset = new DefaultPieDataset();
		threadPieChartDataset.setValue("New", 0);
		threadPieChartDataset.setValue("Terminated", 0);
		threadPieChartDataset.setValue("Runnable", 0);
		threadPieChartDataset.setValue("Blocked", 0);
		threadPieChartDataset.setValue("Waiting", 0);
		threadPieChartDataset.setValue("Timed Waiting", 0);

		JFreeChart chart = ChartFactory.createPieChart3D("Threads", threadPieChartDataset,
				true, true, false);

		PiePlot3D plot = (PiePlot3D) chart.getPlot();
		plot.setStartAngle(290);
		plot.setDirection(Rotation.CLOCKWISE);
		plot.setForegroundAlpha(0.5f);

		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new Dimension(300, 200));
		return chartPanel;
	}

	public Profiler getProfiler() {
		return profiler;
	}

	public TableModel getTableModel() {
		return tableModel;
	}

	public static void main(String args[]) throws IOException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException, UnsupportedLookAndFeelException {

		Profiler profiler = new Profiler();
		MainFrame mainFrame = new MainFrame(profiler);
		new Thread(new Server(50000, mainFrame)).start();

	}
}
