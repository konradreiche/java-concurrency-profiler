package de.fu.profiler.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Models a Java Virtual Machine.
 * 
 * @author Konrad Johannes Reiche
 * 
 */
public class JVM implements Comparable<JVM> {

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
	final Map<Integer, ThreadInfo> threads;

	/**
	 * A list of events where its timestamp is mapped to a certain notify and
	 * wait log entry with a textual representation.
	 */
	final Map<Long, String> notifyWaitTextualLog;

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
		this.threads = new ConcurrentSkipListMap<Integer, ThreadInfo>();
		this.notifyWaitTextualLog = new ConcurrentHashMap<Long, String>();
		this.monitorLog = new ConcurrentHashMap<Long, MonitorLogEntry>();
		this.monitors = new ConcurrentHashMap<Long, MonitorInfo>();
		this.methods = new ConcurrentHashMap<String, MethodInfo>();
	}

	public String getName() {
		return name;
	}

	public Map<Integer, ThreadInfo> getThreads() {
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
		threads.put(threadInfo.getId(), threadInfo);
	}

	/**
	 * Removes all threads and notifies the graphical elements to display the
	 * changes.
	 */
	public void clearThreads() {
		threads.clear();
	}

	/**
	 * Returns the thread based on its id.
	 * 
	 * @param id
	 *            Thread id.
	 */
	public ThreadInfo getThread(int id) {
		return threads.get(id);
	}

	public Map<Long, String> getNotifyWaitTextualLog() {
		return notifyWaitTextualLog;
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

	public void removeThread(int id) {
		threads.remove(id);
	}

	public void addMonitor(MonitorInfo monitorInfo) {
		monitors.put(monitorInfo.getId(), monitorInfo);
	}

	public void removeMonitor(long id) {
		monitors.remove(id);
	}
}
