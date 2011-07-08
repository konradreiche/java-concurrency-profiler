package de.fu.profiler.model;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.swing.table.AbstractTableModel;

public class MonitorLogTableModel extends AbstractTableModel {

	/**
	 * generated serial version ID
	 */
	private static final long serialVersionUID = -5087099635014715160L;

	/**
	 * List of thread names which is collected in order to provide filter
	 * possibilities.
	 */
	SortedSet<String> threadNames;

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
		threadNames = new ConcurrentSkipListSet<String>();
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

		MonitorLogEntry monitorLogEntry = null;
		SortedSet<Long> sortedTimestamp = new TreeSet<Long>(jvm.monitorLog
				.keySet());

		long timestamp = -1;
		Iterator<Long> it = sortedTimestamp.iterator();
		for (int i = 0; i <= rowIndex && it.hasNext(); ++i) {
			timestamp = it.next();
		}

		if (timestamp == -1) {
			return null;
		}

		monitorLogEntry = jvm.monitorLog.get(timestamp);

		switch (columnIndex) {
		case 0:
			long time = monitorLogEntry.systemTime - jvm.deltaSystemTime;
			return time / 1000000;
		case 1:
			threadNames.add(monitorLogEntry.threadInfo.name);
			return monitorLogEntry.threadInfo.name;
		case 2:

			switch (monitorLogEntry.type) {
			case INVOKED_WAIT:
				return "invoked wait";
			case INVOKED_NOTIFY:
				return "invoked notify";
			case INVOKED_NOTIFY_ALL:
				return "invoked notifyAll";
			case LEFT_WAIT:
				return "left wait";
			case CONTENDED_WITH_THREAD:
				String result = (monitorLogEntry.owningThread == null) ? "contended with N/A"
						: "contended with " + monitorLogEntry.owningThread.name;
				return result;

			case ENTERED_AFTER_CONTENTION_WITH_THREAD:
				return "entered after contention";
			}

		case 3:
			return monitorLogEntry.monitorClass;
		case 4:
			return monitorLogEntry.methodContext;
		case 5:

			if (monitorLogEntry.oldState != null) {
				return monitorLogEntry.oldState;
			} else {
				return "-";
			}

		case 6:

			if (monitorLogEntry.newState != null) {
				return monitorLogEntry.newState;
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

	public SortedSet<String> getThreadNames() {
		return threadNames;
	}
}
