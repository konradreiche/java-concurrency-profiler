package de.fu.profiler.model;

import java.util.Map;
import java.util.Observable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;

/**
 * Models a Java Virtual Machine.
 * 
 * @author Konrad Johannes Reiche
 * 
 */
public class JVM extends Observable implements Comparable<JVM> {

	/**
	 * pid
	 */
	final int pid;

	/**
	 * The host address of the Java Virtual Machine.
	 */
	final String host;

	/**
	 * The name of the executed java program.
	 */
	final String name;

	/**
	 * Identifies the JVM by constructing an identifier with the form: pid@host
	 */
	final String identifier;

	/**
	 * The first system time received of the host machine in order to calculate
	 * the passed time.
	 */
	final long deltaSystemTime;

	/**
	 * A list of threads of the JVM.
	 */
	final Map<Long, ThreadInfo> threads;

	/**
	 * A list of events where its timestamp is mapped to a certain notify and
	 * wait log entry.
	 */
	final Map<Long, MonitorLogEntry> monitorLog;

	/**
	 * A list of monitors mapped by their ids.
	 */
	final Map<Long, MonitorInfo> monitors;

	/**
	 * A list of invoked methods mapped by their class name + method name.
	 */
	final Map<String, MethodInfo> methods;

	/**
	 * The graph presenting the resource allocation graph constructed due to
	 * thread contention on monitor acquisition.
	 */
	final Graph<Node<?>, Long> resourceAllocationGraph;

	/**
	 * Whether the JVM is deadlocked.
	 */
	boolean isDeadlocked;

	/**
	 * Standard constructor.
	 * 
	 * @param id
	 *            generated id by the profiling agent.
	 * @param name
	 *            the name of the executed java program.
	 * @param host
	 */
	public JVM(int id, String name, String host, long deltaSystemTime) {
		super();
		this.pid = id;
		this.host = host;
		this.name = name;
		this.identifier = pid + "@" + host;
		this.deltaSystemTime = deltaSystemTime;
		this.threads = new ConcurrentSkipListMap<Long, ThreadInfo>();
		this.monitorLog = new ConcurrentHashMap<Long, MonitorLogEntry>();
		this.monitors = new ConcurrentHashMap<Long, MonitorInfo>();
		this.methods = new ConcurrentHashMap<String, MethodInfo>();
		this.resourceAllocationGraph = new DirectedSparseMultigraph<Node<?>, Long>();
	}

	public String getName() {
		return name;
	}

	public Map<Long, ThreadInfo> getThreads() {
		return threads;
	}

	/**
	 * Adds a new thread and notifies the graphical elements to display the
	 * changes.
	 * 
	 * @param threadInfo
	 *            a thread.
	 */
	public void addThread(ThreadInfo threadInfo) {
		threads.put(threadInfo.id, threadInfo);
		resourceAllocationGraph.addVertex(threadInfo);
	}

	public void removeThread(long id) {
		ThreadInfo thread = threads.get(id);
		threads.remove(id);
		resourceAllocationGraph.removeVertex(thread);
	}

	public void assignResource(long timestamp, ThreadInfo thread,
			MonitorInfo monitor) {

		monitor.ownedByThread = thread;
		if (resourceAllocationGraph.findEdge(thread, monitor) == null) {
			resourceAllocationGraph.addEdge(timestamp, thread, monitor,
					EdgeType.DIRECTED);
		}
	}

	public void unassignResource(long timestamp, ThreadInfo thread,
			MonitorInfo monitor) {

		monitor.ownedByThread = null;
		Long edge = resourceAllocationGraph.findEdge(thread, monitor);
		resourceAllocationGraph.removeEdge(edge);
	}

	public void requestResource(long timestamp, ThreadInfo thread,
			MonitorInfo monitor) {

		thread.requestedResource = monitor;
		if (resourceAllocationGraph.findEdge(monitor, thread) == null) {
			resourceAllocationGraph.addEdge(timestamp, monitor, thread,
					EdgeType.DIRECTED);
		}
		
		if (!isDeadlocked && isDeadlocked(thread)) {
			isDeadlocked = true;
			setChanged();
			notifyObservers(this);
		} else if (isDeadlocked) {
			
			boolean isStillDeadlocked = false;
			for (ThreadInfo t : threads.values()) {
				isStillDeadlocked |= isDeadlocked(t);
			}
			
			if (!isStillDeadlocked) {
				isDeadlocked = false;
				setChanged();
				notifyObservers(this);
			}
		}
	}

	public boolean isDeadlocked(ThreadInfo thread) {

		if (thread.getRequestedResource() == null) {
			return false;
		}
		
		MonitorInfo originalMonitor = thread.getRequestedResource();
		MonitorInfo requestedMonitor = thread.getRequestedResource();		
		ThreadInfo owningThread = requestedMonitor.ownedByThread;
		
		while (true) {
			if (owningThread != null) {
				requestedMonitor = owningThread.requestedResource;
				if (requestedMonitor != null) {
					owningThread = requestedMonitor.ownedByThread;
					if (requestedMonitor.equals(originalMonitor)) {
						return true;
					}
				} else {
					return false;
				}
			} else {
				return false;
			}
		}

	}

	public void stopRequestResource(long timestamp, ThreadInfo thread,
			MonitorInfo monitor) {

		thread.requestedResource = null;
		Long edge = resourceAllocationGraph.findEdge(monitor, thread);
		resourceAllocationGraph.removeEdge(edge);
	}

	/**
	 * Removes all threads and notifies the graphical elements to display the
	 * changes.
	 */
	public void clearThreads() {
		threads.clear();
	}

	public void addMonitorLogEntry(long timestamp,
			MonitorLogEntry monitorLogEntry) {

		monitorLog.put(timestamp, monitorLogEntry);
		setChanged();
		notifyObservers(monitorLogEntry.getThreadInfo());
	}

	/**
	 * Returns the thread based on its id.
	 * 
	 * @param id
	 *            Thread id.
	 */
	public ThreadInfo getThread(long id) {
		return threads.get(id);
	}

	public Map<Long, MonitorLogEntry> getMonitorLog() {
		return monitorLog;
	}

	public Map<Long, MonitorInfo> getMonitors() {
		return monitors;
	}

	public MonitorInfo getMonitor(long id) {
		return monitors.get(id);
	}

	public Map<String, MethodInfo> getMethods() {
		return methods;
	}

	public int getPid() {
		return pid;
	}

	public String getHost() {
		return host;
	}

	@Override
	public int compareTo(JVM o) {
		return identifier.compareTo(o.identifier);
	}

	public void addMonitor(MonitorInfo monitorInfo) {
		long id = monitorInfo.id;
		monitors.put(id, monitorInfo);
		resourceAllocationGraph.addVertex(monitorInfo);
	}

	public void removeMonitor(long id) {
		MonitorInfo monitor = monitors.get(id);
		monitors.remove(id);
		resourceAllocationGraph.removeVertex(monitor);
	}

	public Graph<Node<?>, Long> getResourceAllocationGraph() {
		return resourceAllocationGraph;
	}

	public boolean isDeadlocked() {
		return isDeadlocked;
	}
	
	
}
