package de.fu.profiler.model;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/*
 * The thread table for dispaying the thread statistics
 * 
 * @author Konrad Johannes Reiche
 *
 */
public class ThreadStatsTableModel extends AbstractTableModel {

	/**
	 * generated serial version ID
	 */
	private static final long serialVersionUID = -5713880756271160854L;

	JVM jvm;

	/**
	 * Column names.
	 */
	String[] columnNames = { "Thread", "#wait", "#notify", "#notifyAll",
			"#contention", "#entered" };

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {

		if (jvm == null) {
			return 0;
		} else {
			return jvm.getThreads().size();
		}
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {

		List<ThreadInfo> threadList = new ArrayList<ThreadInfo>(jvm
				.getThreads());

		ThreadInfo threadInfo = threadList.get(rowIndex);
		
		switch (columnIndex) {
		case 0:
			return threadInfo.name;
		case 1:
			return threadInfo.getWaitCount();
		case 2:
			return threadInfo.getNotifyCount();
		case 3:
			return threadInfo.getNotifyAllCount();
		case 4:
			return threadInfo.getMonitorContendedCount();
		case 5:
			return threadInfo.getMonitorEnteredCount();
		}

		return null;
	}

	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}

	public void setCurrentJVM(JVM jvm) {
		this.jvm = jvm;
	}
}
