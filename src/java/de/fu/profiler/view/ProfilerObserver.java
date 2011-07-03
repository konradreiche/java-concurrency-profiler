package de.fu.profiler.view;

import java.util.Observable;
import java.util.Observer;

import javax.swing.table.AbstractTableModel;

import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import de.fu.profiler.model.JVM;
import de.fu.profiler.model.ThreadInfo;

/**
 * The observer of the profiling changing the state of the view in order to
 * display the changes made to the profiler mode.
 * 
 * @author Konrad Johannes Reiche
 * 
 */
public class ProfilerObserver implements Observer {

	ProfilerView view;

	public ProfilerObserver(ProfilerView view) {
		super();
		this.view = view;
	}

	@Override
	public void update(final Observable o, final Object arg) {

		if (arg != null && view.notifyWaitViews.containsKey(arg)
				&& view.resourceAllocationGraphs.containsKey(arg)) {
			JVM jvm = (JVM) arg;

			int newCounter = 0;
			int terminatedCounter = 0;
			int runnableCounter = 0;
			int blockedCounter = 0;
			int waitingCounter = 0;
			int timedWaitingCounter = 0;

			for (ThreadInfo thread : jvm.getThreads().values()) {
				String state = thread.getState();

				if (state.equals("New")) {
					newCounter++;
				} else if (state.equals("Terminated")) {
					terminatedCounter++;
				} else if (state.equals("Runnable")) {
					runnableCounter++;
				} else if (state.equals("Blocked")) {
					blockedCounter++;
				} else if (state.equals("Waiting")) {
					waitingCounter++;
				} else if (state.equals("Timed Waiting")) {
					timedWaitingCounter++;
				} else {
					assert (false);
				}

				DefaultPieDataset pieDataset = ((DefaultPieDataset) view.model
						.getThreadStatePieDataset().get(jvm));

				pieDataset.setValue("New", newCounter);
				pieDataset.setValue("Terminated", terminatedCounter);
				pieDataset.setValue("Runnable", runnableCounter);
				pieDataset.setValue("Blocked", blockedCounter);
				pieDataset.setValue("Waiting", waitingCounter);
				pieDataset.setValue("Timed Waiting", timedWaitingCounter);

				updateThreadStateOverTimeChart(thread, view.model
						.getThreadStateOverTimeDataset().get(jvm));
			}

			((AbstractTableModel) view.model.getThreadTableModels().get(jvm))
					.fireTableDataChanged();

			((AbstractTableModel) view.model.getThreadStatsTableModels().get(
					jvm)).fireTableDataChanged();

			((AbstractTableModel) view.model.getTimeTableModels().get(jvm))
					.fireTableDataChanged();

			((AbstractTableModel) view.model.getMonitorLogTables().get(jvm))
					.fireTableDataChanged();

			((AbstractTableModel) view.model.getLockTableModels().get(jvm))
					.fireTableDataChanged();

			view.notifyWaitViews.get(jvm).repaint();

			view.resourceAllocationGraphs.get(jvm).graphBuilder
					.createWaitForGraph(jvm);
		}
	}

	private void updateThreadStateOverTimeChart(ThreadInfo threadInfo,
			DefaultCategoryDataset threadOverTimeDataSet) {

		String possibleStates[] = new String[] { "New", "Terminated",
				"Runnable", "Blocked", "Waiting", "Timed Waiting" };

		for (String possibleState : possibleStates) {

			long duration = threadInfo.getStateToDuration().get(possibleState);

			threadOverTimeDataSet.setValue(duration, possibleState,
					threadInfo.getName());

		}
	}
}