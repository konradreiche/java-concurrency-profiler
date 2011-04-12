package de.fu.profiler.view;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.IOException;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.TableModel;

import de.fu.profiler.Server;
import de.fu.profiler.controller.ThreadTableModel;
import de.fu.profiler.model.JVM;
import de.fu.profiler.model.Profiler;

public class MainFrame extends JFrame {

	/**
	 * generated serial UID
	 */
	private static final long serialVersionUID = -6342475722971843766L;

	Profiler profiler;
	JTable table;
	TableModel tableModel;
	DefaultListModel listModel;
	JList list;

	public MainFrame(Profiler profiler) throws ClassNotFoundException,
			InstantiationException, IllegalAccessException,
			UnsupportedLookAndFeelException {

		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.profiler = profiler;
		this.setPreferredSize(new Dimension(500, 400));
		this.setLayout(new GridLayout(1, 1));
		
		tableModel = new ThreadTableModel();
		listModel = new DefaultListModel();
		this.table = new JTable(tableModel);
		this.list = new JList(listModel);
		
		
		// creates the thread table in a scrollable Panel
		JScrollPane scrollPane = new JScrollPane(table);
		
		// creates a tabbed panel
		JTabbedPane tabbedPane = new JTabbedPane();
		
		// adds the scrollable panel to the tabbed panel
		tabbedPane.add("Overview", scrollPane);
		

		for (JVM jvm : profiler.getIDsToJVMs().values()) {
			listModel.addElement("JVM " + "(pid: " + jvm.getId() + ")");
		}

		table.setFillsViewportHeight(true);
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, list,tabbedPane);
		this.add(splitPane);

		this.setSize(500, 400);
		this.setVisible(true);
	}

	public Profiler getProfiler() {
		return profiler;
	}

	public TableModel getTableModel() {
		return tableModel;
	}

	public static void main(String args[]) throws IOException,
			ClassNotFoundException, InstantiationException,
			IllegalAccessException, UnsupportedLookAndFeelException {

		Profiler profiler = new Profiler();
		MainFrame mainFrame = new MainFrame(profiler);
		new Thread(new Server(50000, mainFrame)).start();

	}
}
