package de.fu.profiler.controller;

import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import javax.swing.table.AbstractTableModel;

import de.fu.profiler.model.JVM;
import de.fu.profiler.model.ThreadInfo;

public class ThreadTableModel extends AbstractTableModel implements Observer {

	private static final long serialVersionUID = -737908470678111339L;

	String[] columnNames = { "Name", "Priority", "State", "Context Class Loader" };
	
	JVM jvm;

	public ThreadTableModel() {
		super();
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
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}

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
			return threadInfo.getName();
		case 1:
			return threadInfo.getPriority();
		case 2:
			return threadInfo.getState();
		case 3:
			return threadInfo.isContextClassLoaderSet();
		default:
			return null;
		}
	}
	
	public void setCurrentJVM(JVM jvm) {
		this.jvm = jvm;
	}

	@Override
	public void update(Observable o, Object arg) {
		fireTableDataChanged();
	}

}
