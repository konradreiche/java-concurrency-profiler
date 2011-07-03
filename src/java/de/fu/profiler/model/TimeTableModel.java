package de.fu.profiler.model;

import java.util.Iterator;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * Provides a {@link TableModel} for displaying the method measuring in a
 * {@link JTable}.
 * 
 * @author Konrad Johannes Reiche
 * 
 */
public class TimeTableModel extends AbstractTableModel {

	/**
	 * generated serial version ID
	 */
	private static final long serialVersionUID = 226417728835659052L;

	/**
	 * Column names.
	 */
	String[] columnNames = { "Method", "Time %", "Time ms", "Clock Cycles",
			"Invocations" };

	/**
	 * The current actively profiled JVM.
	 */
	JVM jvm;

	/**
	 * Standard constructor.
	 * 
	 * @param threadPieChartDataset
	 */
	public TimeTableModel(JVM jvm) {
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
			return jvm.methods.size();
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

		MethodInfo methodInfo = null;
		Iterator<MethodInfo> it = jvm.methods.values().iterator();
		for (int i = 0; i <= rowIndex && it.hasNext(); ++i) {
			methodInfo = it.next();
		}

		if (methodInfo == null) {
			return null;
		}

		Double result;
		
		switch (columnIndex) {
		case 0:
			return methodInfo.className + "." + methodInfo.methodName;
		case 1:
			return methodInfo.timePercent * 100;
		case 2:
			result = new Double(
					(long) (methodInfo.time * 1000000 / (double) methodInfo.numberOfInvocations));
			return result;
		case 3:
			result = new Double(
					((long)((double) methodInfo.clockCycles / ((double) methodInfo.numberOfInvocations))));
			return result;
		case 4:
			return methodInfo.numberOfInvocations;
		case 5:
		default:
			throw new IllegalStateException("MethodInfo has no"
					+ " attribute mapped to this value");
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {

		switch (columnIndex) {
		case 0:
			return String.class;
		case 1:
			return Double.class;
		case 2:
			return Double.class;
		case 3:
			return Double.class;
		case 4:
			return Integer.class;
		case 5:
		default:
			throw new IllegalStateException("MethodInfo has no"
					+ " attribute mapped to this value");
		}
	}

	public void setCurrentJVM(JVM jvm) {
		this.jvm = jvm;
	}

}
