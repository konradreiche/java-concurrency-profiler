package de.fu.profiler.view;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.StackedBarRenderer3D;
import org.jfree.ui.TextAnchor;
import org.jfree.util.Rotation;

import de.fu.profiler.model.ProfilerModel;

public class OverviewPanel extends JPanel {

	/**
	 * generated serial version ID
	 */
	private static final long serialVersionUID = 4870196876809965787L;

	public OverviewPanel(ProfilerModel model, TableModel threadOverviewTableModel) {
		
		super(new GridLayout(1, 1));
		JTable threadOverviewTable = new JTable(threadOverviewTableModel);
		JScrollPane threadTableScrollPane = new JScrollPane(threadOverviewTable);
		threadTableScrollPane.setMinimumSize(new Dimension(300, 300));
		
		JTabbedPane tabbedDiagramPane = new JTabbedPane();
		tabbedDiagramPane.add("Overall Thread State",setUpOverallThreadStatePieChart(model));
		tabbedDiagramPane.add("Thread State Over Time", new JScrollPane(
				setUpThreadStateOverTimeBarChart(model)));
		
		super.add(new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				threadTableScrollPane, tabbedDiagramPane));
	}
	
	private ChartPanel setUpOverallThreadStatePieChart(ProfilerModel model) {

		JFreeChart chart = ChartFactory.createPieChart3D(
				"Overall Threads State", model.getThreadPieDataset(), true,
				true, false);

		PiePlot3D plot = (PiePlot3D) chart.getPlot();
		plot.setStartAngle(290);
		plot.setDirection(Rotation.CLOCKWISE);
		plot.setForegroundAlpha(0.5f);

		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new Dimension(300, 200));
		return chartPanel;
	}
	
	private ChartPanel setUpThreadStateOverTimeBarChart(ProfilerModel model) {

		JFreeChart chart = ChartFactory.createStackedBarChart3D(
				"Thread State Over Time", "Threads", "Time", model
						.getCategoryDataset(), PlotOrientation.HORIZONTAL,
				true, true, false);

		StackedBarRenderer3D renderer = (StackedBarRenderer3D) chart
				.getCategoryPlot().getRenderer();
		renderer.setRenderAsPercentages(true);
		renderer.setDrawBarOutline(false);

		for (int i = 0; i < 6; ++i) {
			renderer.setSeriesItemLabelGenerator(i,
					new StandardCategoryItemLabelGenerator("{3}", NumberFormat
							.getIntegerInstance(), new DecimalFormat("0.0%")));
			renderer.setSeriesItemLabelsVisible(i, true);
			renderer.setSeriesPositiveItemLabelPosition(i,
					new ItemLabelPosition(ItemLabelAnchor.CENTER,
							TextAnchor.CENTER));
			renderer.setSeriesNegativeItemLabelPosition(i,
					new ItemLabelPosition(ItemLabelAnchor.CENTER,
							TextAnchor.CENTER));
		}

		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new Dimension(300, 200));
		return chartPanel;
	}
}
