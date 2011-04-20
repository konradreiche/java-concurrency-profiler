package de.fu.profiler.model;

/**
 * Models a java thread and describes selected and analysed information of a
 * thread.
 * 
 * @author Konrad Johannes Reiche
 * 
 */
public class ThreadInfo implements Comparable<ThreadInfo> {

	/**
	 * A generated id by the profiler.
	 */
	final int id;

	/**
	 * The threads name.
	 */
	final String name;

	/**
	 * The threads priority.
	 */
	final int priority;

	/**
	 * The threads state.
	 */
	final String state;

	/**
	 * Whether the context class loader is set.
	 */
	final boolean isContextClassLoaderSet;

	/**
	 * The number how much the thread has entered a contended monitor.
	 */
	int contendedMonitorWaitCount;

	/**
	 * Standard constructor.
	 * 
	 * @param id
	 *            a generated id by the profiler.
	 * @param name
	 *            the threads name.
	 * @param priority
	 *            the threads priority.
	 * @param state
	 *            the threads state.
	 * @param ccl
	 *            whether the context class loader is set.
	 */
	public ThreadInfo(int id, String name, int priority, String state,
			boolean ccl) {
		super();
		this.id = id;
		this.name = name;
		this.priority = priority;
		this.state = state;
		this.isContextClassLoaderSet = ccl;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getPriority() {
		return priority;
	}

	public String getState() {
		return state;
	}

	public boolean isContextClassLoaderSet() {
		return isContextClassLoaderSet;
	}

	/**
	 * Increments the statistics about the contended monitor waiting.
	 */
	public void incContendedMonitorWait() {
		++contendedMonitorWaitCount;
	}

	/**
	 * Compares the threads by their id.
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ThreadInfo threadInfo) {
		return new Integer(id).compareTo(new Integer(threadInfo.id));
	}

	/**
	 * Compares the threads by their id.
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {
		return new Integer(id).equals(new Integer(((ThreadInfo) o).id));
	}
}
