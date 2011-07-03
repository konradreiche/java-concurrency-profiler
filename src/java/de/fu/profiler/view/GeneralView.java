package de.fu.profiler.view;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;

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

import de.fu.profiler.model.JVM;
import de.fu.profiler.model.ProfilerModel;
import de.fu.profiler.model.ThreadTableModel;

public class GeneralView extends JPanel {

	/**
	 * generated serial version ID
	 */
	private static final long serialVersionUID = 4870196876809965787L;

	public GeneralView(ProfilerModel model, JVM jvm) {

		super(new GridLayout(1, 1));
		ThreadTableModel tableModel = model.getThreadTableModels().get(jvm);
		JTable threadOverviewTable = new JTable(tableModel);
		JScrollPane threadTableScrollPane = new JScrollPane(threadOverviewTable);
		threadTableScrollPane.setMinimumSize(new Dimension(300, 300));

		JPanel diagramPanel = new JPanel(new GridLayout(1, 2));
		JPanel generalThreadStatePanel = new JPanel(new GridLayout(1, 1));
		JPanel threadStateOverTimePanel = new JPanel(new GridLayout(1, 1));

		generalThreadStatePanel.setBorder(BorderFactory
				.createTitledBorder("General Thread State"));
		generalThreadStatePanel
				.add(setUpOverallThreadStatePieChart(model, jvm));

		threadStateOverTimePanel.setBorder(BorderFactory
				.createTitledBorder("Thread State OverTime"));
		threadStateOverTimePanel.add(new JScrollPane(setUpThreadStateOverTimeBarChart(
				model, jvm)));

		diagramPanel.add(generalThreadStatePanel);
		diagramPanel.add(threadStateOverTimePanel);

		super.add(new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				threadTableScrollPane, diagramPanel));
	}

	private ChartPanel setUpOverallThreadStatePieChart(ProfilerModel model,
			JVM jvm) {

		JFreeChart chart = ChartFactory.createPieChart3D(null, model
				.getThreadStatePieDataset().get(jvm), true, true, false);

		PiePlot3D plot = (PiePlot3D) chart.getPlot();
		plot.setStartAngle(290);
		plot.setDirection(Rotation.CLOCKWISE);
		plot.setForegroundAlpha(0.5f);

		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new Dimension(300, 200));
		return chartPanel;
	}

	private ChartPanel setUpThreadStateOverTimeBarChart(ProfilerModel model,
			JVM jvm) {

		JFreeChart chart = ChartFactory.createStackedBarChart3D(null,
				"Threads", "Time",
				model.getThreadStateOverTimeDataset().get(jvm),
				PlotOrientation.HORIZONTAL, true, true, false);

		StackedBarRenderer3D renderer = (StackedBarRenderer3D) chart
				.getCategoryPlot().getRenderer();
		renderer.setRenderAsPercentages(true);
		renderer.setDrawBarOutline(false);

		for (int i = 0; i < 6; ++i) {
			renderer.setSeriesItemLabelGenerator(
					i,
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
