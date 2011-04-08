package de.fu.profiler.view;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.IOException;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
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

		this.setLayout(new GridLayout(2, 1));
		
		JPanel jvmNavigationPanel = new JPanel();
		jvmNavigationPanel.add(new JLabel("VMs"));
		
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		this.profiler = profiler;
		this.setPreferredSize(new Dimension(500, 400));
		
		JTabbedPane tabbedPane = new JTabbedPane();
		JPanel panel = new JPanel(false);
		panel.setLayout(new GridLayout(1, 1));

		tableModel = new ThreadTableModel();
		listModel = new DefaultListModel();
		this.table = new JTable(tableModel);
		this.list = new JList(listModel);
		
		for (JVM jvm : profiler.getIDsToJVMs().values()) {
			listModel.addElement("JVM " + "(pid: " + jvm.getId() + ")");
		}
			
		table.setFillsViewportHeight(true);

		JComponent pane = panel;
		tabbedPane.addTab("Overview", pane);

		JScrollPane scrollPane = new JScrollPane(table);
		jvmNavigationPanel.add(list);
		this.add(jvmNavigationPanel);
		pane.add(scrollPane);
		this.add(tabbedPane);

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
