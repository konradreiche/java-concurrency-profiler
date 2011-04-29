package de.fu.profiler.view;

import java.util.Observable;
import java.util.Observer;

import javax.swing.DefaultListModel;
import javax.swing.table.AbstractTableModel;

import org.jfree.data.general.DefaultPieDataset;

import de.fu.profiler.model.JVM;
import de.fu.profiler.model.ThreadInfo;

public class ProfilerObserver implements Observer {

	ProfilerView view;

	public ProfilerObserver(ProfilerView view) {
		super();
		this.view = view;
	}

	@Override
	public void update(Observable o, Object arg) {

		synchronized (view.list) {
			((DefaultListModel) view.list.getModel()).clear();
			((DefaultListModel) view.list.getModel()).setSize(0);
			for (JVM jvm : view.model.getIDsToJVMs().values()) {
				((DefaultListModel) view.list.getModel()).addElement("pid "
						+ jvm.getId());
			}			
		}

		if (view.model.getCurrentJVM() != null) {
			for (ThreadInfo thread : view.model.getCurrentJVM().getThreads()) {
				if (o.equals(thread)) {
					view.notifyWaitLog.append(thread.getMonitorStatus());
					view.notifyWaitLog.repaint();
					break;
				}
			}

			int newCounter = 0;
			int terminatedCounter = 0;
			int runnableCounter = 0;
			int blockedCounter = 0;
			int waitingCounter = 0;
			int timedWaitingCounter = 0;

			for (ThreadInfo thread : view.model.getCurrentJVM().getThreads()) {
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
		}
	}
}
