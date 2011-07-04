package de.fu.profiler.model;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.table.AbstractTableModel;

public class MonitorLogTableModel extends AbstractTableModel {

	/**
	 * generated serial version ID
	 */
	private static final long serialVersionUID = -5087099635014715160L;

	/**
	 * Column names.
	 */
	String[] columnNames = { "Time (ms)", "Thread", "Action", "Monitor",
			"Context", "Old State", "New State" };

	/**
	 * The current actively profiled JVM.
	 */
	JVM jvm;

	/**
	 * Standard constructor.
	 * 
	 * @param stackTracesTree
	 * 
	 */
	public MonitorLogTableModel(JVM jvm) {
		super();
		this.jvm = jvm;
	}

	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {

		if (jvm == null) {
			return 0;
		} else {
			return jvm.getMonitorLog().size();
		}
	}

	/**
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	/**
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}

	/**
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {

		MonitorLogEntry notifyWaitLogEntry = null;
		SortedSet<Long> sortedTimestamp = new TreeSet<Long>(
				jvm.monitorLog.keySet());

		long timestamp = -1;
		Iterator<Long> it = sortedTimestamp.iterator();
		for (int i = 0; i <= rowIndex && it.hasNext(); ++i) {
			timestamp = it.next();
		}

		if (timestamp == -1) {
			return null;
		}

		notifyWaitLogEntry = jvm.monitorLog.get(timestamp);

		switch (columnIndex) {
		case 0:
			long time = notifyWaitLogEntry.systemTime - jvm.deltaSystemTime;
			return time / 1000000;
		case 1:
			return notifyWaitLogEntry.threadInfo.name;
		case 2:

			switch (notifyWaitLogEntry.type) {
			case INVOKED_WAIT:
				return "invoked wait";
			case INVOKED_NOTIFY:
				return "invoked notify";
			case INVOKED_NOTIFY_ALL:
				return "invoked notifyAll";
			case LEFT_WAIT:
				return "left wait";
			case CONTENDED_WITH_THREAD:
				String result = (notifyWaitLogEntry.owningThread == null) ? "contended with N/A"
						: "contended with "
								+ notifyWaitLogEntry.owningThread.name;
				return result;

			case ENTERED_AFTER_CONTENTION_WITH_THREAD:
				return "entered after contention";
			}

		case 3:
			return notifyWaitLogEntry.monitorClass;
		case 4:
			return notifyWaitLogEntry.methodContext;
		case 5:

			if (notifyWaitLogEntry.oldState != null) {
				return notifyWaitLogEntry.oldState;
			} else {
				return "-";
			}

		case 6:

			if (notifyWaitLogEntry.newState != null) {
				return notifyWaitLogEntry.newState;
			} else {
				return "-";
			}

		default:
			throw new RuntimeException("ThreadInfo has no"
					+ " attribute mapped to this value");
		}
	}

	public void setCurrentJVM(JVM jvm) {
		this.jvm = jvm;
	}

	public JVM getJvm() {
		return jvm;
	}

}
