package de.fu.profiler.model;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

public class LockTableModel extends AbstractTableModel {

	JVM jvm;

	/**
	 * generated serial version ID
	 */
	private static final long serialVersionUID = -3187623776815738029L;

	/**
	 * Column names.
	 */
	String[] columnNames = { "ID", "Class", "Entry Count", "Waiter Count",
			"Notify Waiter Count" };

	@Override
	public int getRowCount() {
		if (jvm == null) {
			return 0;
		} else {
			return jvm.monitors.size();
		}
	}

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

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {

		Monitor monitor = new ArrayList<Monitor>(jvm.monitors.values())
				.get(rowIndex);

		switch (columnIndex) {
		case 0:
			return monitor.id;
		case 1:
			return monitor.className;
		case 2:
			return monitor.entryCount;
		case 3:
			return monitor.waiterCount;
		case 4:
			return monitor.notifyWaiterCount;
		}

		return null;
	}

	public void setCurrentJVM(JVM jvm) {
		this.jvm = jvm;
	}

}
