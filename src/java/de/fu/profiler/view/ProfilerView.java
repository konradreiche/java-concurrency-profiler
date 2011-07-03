package de.fu.profiler.view;

import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import de.fu.profiler.model.JVM;
import de.fu.profiler.model.ProfilerModel;

/**
 * The graphical user interface of the profiler. Further elements of the
 * graphical user interface are initialized during the constructor of this
 * {@link JFrame}.
 * 
 * @author Konrad Johannes Reiche
 * 
 */
public class ProfilerView extends JFrame implements Observer {

	/**
	 * generated serial version ID
	 */
	private static final long serialVersionUID = -6276797391162392835L;
	
	private static final int WIDTH = 1400;

	private static final int HEIGHT = 800;

	// BOGUS
	List<JVM> jvms = new ArrayList<JVM>();

	/**
	 * The profiler model
	 */
	ProfilerModel model;

	JTabbedPane mainSelection;

	Map<JVM, MonitorLogView> notifyWaitViews;
	Map<JVM, ResourceAllocationGraph> resourceAllocationGraphs;

	public ProfilerView(ProfilerModel model) {

		this.model = model;
		notifyWaitViews = new ConcurrentSkipListMap<JVM, MonitorLogView>();
		resourceAllocationGraphs = new ConcurrentSkipListMap<JVM, ResourceAllocationGraph>();
		model.addObserver(this);
		setUpLookAndFeel();

		setTitle("Java Concurrency Profiler");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new GridLayout(1, 2));

		mainSelection = new JTabbedPane(JTabbedPane.LEFT);
		mainSelection.add("Welcome", new WelcomePanel());
		this.add(mainSelection);
		this.setSize(WIDTH, HEIGHT);
	}

	private void createView(JVM jvm) {
		GeneralView generalView = new GeneralView(model, jvm);
		MonitorLogView notifyWaitPanel = new MonitorLogView(model, jvm);
		MonitorView locks = new MonitorView(model, jvm);
		ResourceAllocationGraph resourceAllocationGraph = new ResourceAllocationGraph(
				model);
		MethodProfilingView methodProfilingView = new MethodProfilingView(model,jvm);

		notifyWaitViews.put(jvm, notifyWaitPanel);
		resourceAllocationGraphs.put(jvm, resourceAllocationGraph);

		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.add("General", generalView);
		tabbedPane.add("Monitor Log", notifyWaitPanel);
		tabbedPane.add("Monitor", locks);
		tabbedPane.add("Resource Allocation Graph", resourceAllocationGraph);
		tabbedPane.add("Method Profiling", methodProfilingView);

		mainSelection.add(jvm.getHost() + " (PID " + jvm.getPid() + ")",
				tabbedPane);
		mainSelection.setSelectedComponent(tabbedPane);
	}

	private void setUpLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void update(Observable o, Object arg) {

		// TODO: different Observable for adding jvm and chaning its state
		if (arg != null && o instanceof ProfilerModel && !jvms.contains(arg)) {
			JVM jvm = (JVM) arg;
			jvms.add(jvm);
			createView(jvm);
		}
	}
}
