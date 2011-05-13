package de.fu.profiler.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
	String name;

	/**
	 * The threads priority.
	 */
	int priority;

	/**
	 * The threads state.
	 */
	String state;

	/**
	 * Whether the context class loader is set.
	 */
	boolean isContextClassLoaderSet;

	/**
	 * The number how much the thread has called wait.
	 */
	int waitCount;

	/**
	 * After thread termination this value represents the time the CPU has spent
	 * for this thread.
	 */
	long cpuTime;

	/**
	 * The time which was spent inside each state for the life time of the
	 * thread.
	 */
	Map<String, Long> stateToDuration;

	/**
	 * The system time since the last state update was done.
	 */
	long timeSinceLastUpdate;

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
	 * @param notiyWaitController
	 */
	public ThreadInfo(int id, String name, int priority, String state,
			boolean ccl) {
		super();
		this.id = id;
		this.name = name;
		this.priority = priority;
		this.state = state;
		this.isContextClassLoaderSet = ccl;
		this.cpuTime = -1;
		this.stateToDuration = new ConcurrentHashMap<String, Long>();

		String possibleStates[] = new String[] { "NEW", "RUNNABLE", "BLOCKED",
				"WAITING", "TIMED_WAITING", "TERMINATED" };

		for (String possibleState : possibleStates) {
			stateToDuration.put(possibleState, 0l);
		}

		stateToDuration.put(state, 1l);
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
	public void increaseWaitCounter() {
		++waitCount;
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

	public void setState(String state) {
		this.state = state;
	}

	public void setCpuTime(long cpuTime) {
		this.cpuTime = cpuTime;
	}

	public void compareAndSet(long timestamp, int id, String name,
			int priority, String state, boolean isContextClassLoaderSet) {

		if (this.id != id) {
			throw new IllegalStateException(
					"The threads id does not match the threads id to be updated.");
		}

		if (!this.name.equals(name)) {
			this.name = name;
		}

		if (this.priority != priority) {
			this.priority = priority;
		}

		if (!stateToDuration.containsKey(state)) {
			stateToDuration.put(state, 1l);
		}

		long timeSpentInState = timestamp - timeSinceLastUpdate;
		long oldTimeSpentInState = stateToDuration.get(this.state);
		stateToDuration.put(this.state, oldTimeSpentInState + timeSpentInState);
		timeSinceLastUpdate = timestamp;

		if (!this.state.equals(state)) {
			this.state = state;
		}

		if (this.isContextClassLoaderSet != isContextClassLoaderSet) {
			this.isContextClassLoaderSet = isContextClassLoaderSet;
		}
	}

	public Map<String, Long> getStateToDuration() {
		return stateToDuration;
	}
}
