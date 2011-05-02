package de.fu.profiler.view;

import java.util.Map.Entry;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;

import org.jfree.data.general.DefaultPieDataset;

import de.fu.profiler.model.AgentMessageProtos.AgentMessage;
import de.fu.profiler.model.JVM;
import de.fu.profiler.model.Monitor;
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

				((DefaultListModel) view.list.getModel()).clear();
				for (JVM jvm : view.model.getIDsToJVMs().values()) {
					((DefaultListModel) view.list.getModel()).addElement("pid "
							+ jvm.getId());
				}

				view.notifyWaitLog.setText(null);
				if (view.model.getCurrentJVM() != null) {

					for (Entry<Long, String> entry : view.model.getCurrentJVM()
							.getNotifyWaitLog().entrySet()) {
						view.notifyWaitLog.append(entry.getKey() + ": "
								+ entry.getValue());
						view.notifyWaitLog.repaint();
					}

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
					}

					((AbstractTableModel) view.model.getTableModel())
							.fireTableDataChanged();

					view.monitorSelection.removeAllItems();
					for (Monitor monitor : view.model.getCurrentJVM()
							.getMonitors().values()) {
						view.monitorSelection.addItem(monitor.getId());
					}

					int jvmId = view.model.getCurrentJVM().getId();
					List<AgentMessage> eventHistory = view.model
							.getMessageHistory().get(jvmId);
					AgentMessage currentEvent = view.model.getCurrentEvent();

					
					view.previousEvent.setEnabled(false);
					view.nextEvent.setEnabled(false);
					if (!currentEvent.equals(eventHistory.get(0))) {
						view.previousEvent.setEnabled(true);
					}

					if (!currentEvent.equals(eventHistory.get(eventHistory
							.size() - 1))) {
						view.nextEvent.setEnabled(true);
					}

					view.eventLabel.setText("Event #"
							+ eventHistory.indexOf(currentEvent));

				}
			}
		});
	}
}
