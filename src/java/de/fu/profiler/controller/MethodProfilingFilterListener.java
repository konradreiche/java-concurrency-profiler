package de.fu.profiler.controller;

import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;

import de.fu.profiler.model.MethodProfilingTableModel;

public class MethodProfilingFilterListener implements DocumentListener {

	final JTextField inputField;
	final JTable table;
	final MethodProfilingTableModel tableModel;
	private TableRowSorter<MethodProfilingTableModel> sorter;

	public MethodProfilingFilterListener(JTextField inputField, JTable table,
			MethodProfilingTableModel tableModel) {
		super();
		this.inputField = inputField;
		this.table = table;
		this.tableModel = tableModel;
		sorter = new TableRowSorter<MethodProfilingTableModel>(tableModel);
		table.setRowSorter(sorter);
	}
	
	private void updateFilter() {
        RowFilter<MethodProfilingTableModel, Object> filter = null;
        try {
            filter = RowFilter.regexFilter(inputField.getText(), 0);
        } catch (java.util.regex.PatternSyntaxException e) {
            return;
        }
        
        synchronized (tableModel) {
        	sorter.setRowFilter(filter);			
		}
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		updateFilter();

	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		updateFilter();
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		updateFilter();
	}

}
