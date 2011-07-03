package de.fu.profiler.model;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.table.AbstractTableModel;

public class SynchronizedTableModel extends AbstractTableModel {

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
	public SynchronizedTableModel(JVM jvm) {
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
			return jvm.synchronizedLog.size();
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

		NotifyWaitLogEntry notifyWaitLogEntry = null;
		SortedSet<Long> sortedTimestamp = new TreeSet<Long>(
				jvm.synchronizedLog.keySet());

		long timestamp = -1;
		Iterator<Long> it = sortedTimestamp.iterator();
		for (int i = 0; i <= rowIndex && it.hasNext(); ++i) {
			timestamp = it.next();
		}

		if (timestamp == -1) {
			return null;
		}

		notifyWaitLogEntry = jvm.synchronizedLog.get(timestamp);

		switch (columnIndex) {
		case 0:
			return notifyWaitLogEntry.systemTime / 1000000;
		case 1:
			return notifyWaitLogEntry.threadInfo.name;
		case 2:

			switch (notifyWaitLogEntry.type) {
			case CONTENDED:
				String result = (notifyWaitLogEntry.owningThread == null) ? "did not contend"
						: "contended with "
								+ notifyWaitLogEntry.owningThread.name;
				return result;

			case ENTERED:
				return "entered";
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

}
