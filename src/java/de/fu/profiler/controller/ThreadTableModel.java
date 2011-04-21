package de.fu.profiler.controller;

import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DefaultPieDataset;

import de.fu.profiler.model.JVM;
import de.fu.profiler.model.ThreadInfo;

/**
 * Provides a {@link TableModel} for displaying the profiled threads in a
 * {@link JTable}.
 * 
 * @author Konrad Johannes Reiche
 * 
 */
public class ThreadTableModel extends AbstractTableModel implements Observer {

	/**
	 * generated serial version ID
	 */
	private static final long serialVersionUID = 226417728835659052L;

	/**
	 * Column names.
	 */
	String[] columnNames = { "ID", "Name", "Priority", "State",
			"Context Class Loader" };

	/**
	 * The current actively profiled JVM.
	 */
	JVM jvm;

	DefaultPieDataset threadPieChartDataset;

	/**
	 * Standard constructor.
	 * 
	 * @param threadPieChartDataset
	 */
	public ThreadTableModel(DefaultPieDataset threadPieChartDataset) {
		super();
		this.threadPieChartDataset = threadPieChartDataset;
	}

	/**
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	@Override
	public int getRowCount() {

		if (jvm == null) {
			return 0;
		} else {
			return jvm.getThreads().size();
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

		ThreadInfo threadInfo = null;
		Iterator<ThreadInfo> it = jvm.getThreads().iterator();
		for (int i = 0; i <= rowIndex && it.hasNext(); ++i) {
			threadInfo = it.next();
		}

		if (threadInfo == null) {
			return null;
		}

		switch (columnIndex) {
		case 0:
			return threadInfo.getId();
		case 1:
			return threadInfo.getName();
		case 2:
			return threadInfo.getPriority();
		case 3:
			return threadInfo.getState();
		case 4:
			return threadInfo.isContextClassLoaderSet();
		default:
			return null;
		}
	}

	public void setCurrentJVM(JVM jvm) {
		this.jvm = jvm;
	}

	/**
	 * Updates the table and the pie chart diagram.
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable o, Object arg) {

		for (ThreadInfo thread : jvm.getThreads()) {
			int currentValue = threadPieChartDataset
					.getValue(thread.getState()).intValue();
			currentValue++;
			threadPieChartDataset.setValue(thread.getState(), currentValue);
		}

		fireTableDataChanged();
	}
}
