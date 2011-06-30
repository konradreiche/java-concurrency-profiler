package de.fu.profiler.view;

import java.util.Observable;
import java.util.Observer;

import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

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
	public void update(final Observable o, Object arg) {

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				if (view.model.getCurrentJVM() != null) {

					int newCounter = 0;
					int terminatedCounter = 0;
					int runnableCounter = 0;
					int blockedCounter = 0;
					int waitingCounter = 0;
					int timedWaitingCounter = 0;

					for (ThreadInfo thread : view.model.getCurrentJVM()
							.getThreads()) {
						String state = thread.getState();

						if (state.equals("NEW")) {
							newCounter++;
						} else if (state.equals("TERMINATED")) {
							terminatedCounter++;
						} else if (state.equals("RUNNABLE")) {
							runnableCounter++;
						} else if (state.equals("BLOCKED")) {
							blockedCounter++;
						} else if (state.equals("WAITING")) {
							waitingCounter++;
						} else if (state.equals("TIMED_WAITING")) {
							timedWaitingCounter++;
						} else {
							assert (false);
						}

						((DefaultPieDataset) view.model.getThreadPieDataset())
								.setValue("New", newCounter);
						((DefaultPieDataset) view.model.getThreadPieDataset())
								.setValue("Terminated", terminatedCounter);
						((DefaultPieDataset) view.model.getThreadPieDataset())
								.setValue("Runnable", runnableCounter);
						((DefaultPieDataset) view.model.getThreadPieDataset())
								.setValue("Blocked", blockedCounter);
						((DefaultPieDataset) view.model.getThreadPieDataset())
								.setValue("Waiting", waitingCounter);
						((DefaultPieDataset) view.model.getThreadPieDataset())
								.setValue("Timed Waiting", timedWaitingCounter);

						updateThreadStateOverTimeChart(thread,
								view.model.getCategoryDataset());
					}

					((AbstractTableModel) view.model.getTableModel())
							.fireTableDataChanged();
					
					((AbstractTableModel) view.model.getThreadStatsTableModel())
					.fireTableDataChanged();
					
					((AbstractTableModel) view.model.getTimeTableModel())
					.fireTableDataChanged();

					((AbstractTableModel) view.model.getNotifyWaitTableModel())
					.fireTableDataChanged();
					
					((AbstractTableModel) view.model.getLockTableModel())
					.fireTableDataChanged();
					
					((AbstractTableModel) view.model.getSynchronizedTableModel())
					.fireTableDataChanged();
					
					view.notifyWaitPanel.stackTraces.repaint();				

					view.graphBuilder.createWaitForGraph(view.model.getCurrentJVM());
				}
			}
		});
	}

	private void updateThreadStateOverTimeChart(ThreadInfo threadInfo,
			DefaultCategoryDataset threadOverTimeDataSet) {

		String possibleStates[] = new String[] { "NEW", "TERMINATED",
				"RUNNABLE", "BLOCKED", "WAITING", "TIMED_WAITING" };

		for (String possibleState : possibleStates) {

			long duration = threadInfo.getStateToDuration().get(possibleState);

			threadOverTimeDataSet.setValue(duration, possibleState,
					threadInfo.getName());

		}
	}
}