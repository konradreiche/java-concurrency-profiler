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
	 * The number how often the thread has invoked notify.
	 */
	int notifyCount;

	/**
	 * The number how often thread has invoked notify all.
	 */
	int notifyAllCount;

	/**
	 * The number of times a thread switched to blocked state
	 */
	int blockedCount;

	/**
	 * The number of times a thread switched to the waiting state
	 */
	int waitingCout;

	/**
	 * The number of times a thread entered a synchronized block.
	 */
	int monitorEnteredCount;

	/**
	 * The number of times a thread contended when trying to acquire a lock.
	 */
	int monitorContendedCount;

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
	 * The monitor which is requested, i.e. trying to be acquired by this thread.
	 * Null if the thread does not currently try to acquire a monitor.
	 */
	Monitor requestedResource;

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
			boolean ccl, long timestamp) {
		super();
		this.id = id;
		this.name = name;
		this.priority = priority;
		this.state = state;
		this.isContextClassLoaderSet = ccl;
		this.cpuTime = -1;
		this.timeSinceLastUpdate = timestamp;
		this.stateToDuration = new ConcurrentHashMap<String, Long>();

		String possibleStates[] = new String[] { "NEW", "RUNNABLE", "BLOCKED",
				"WAITING", "TIMED_WAITING", "TERMINATED" };

		for (String possibleState : possibleStates) {
			stateToDuration.put(possibleState, 0l);
		}

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

		if (timeSinceLastUpdate > timestamp) {

		}

		if (timestamp < timeSinceLastUpdate) {
			System.out.println(timestamp);
			System.out.println(timeSinceLastUpdate);
			System.out.println("Error, old timestamp arrived at state upadte");
		} else {
			long timeSpentInState = timestamp - timeSinceLastUpdate;
			long oldTimeSpentInState = stateToDuration.get(this.state);
			stateToDuration.put(this.state, oldTimeSpentInState
					+ timeSpentInState);
			timeSinceLastUpdate = timestamp;
		}

		if (!this.state.equals(state)) {

			if (state.equals("BLOCKED")) {
				++blockedCount;
			} else if (state.equals("WAITING")) {
				++waitCount;
			}

			this.state = state;
		}

		if (this.isContextClassLoaderSet != isContextClassLoaderSet) {
			this.isContextClassLoaderSet = isContextClassLoaderSet;
		}
	}

	public Map<String, Long> getStateToDuration() {
		return stateToDuration;
	}

	public int getWaitCount() {
		return waitCount;
	}

	public int getNotifyCount() {
		return notifyCount;
	}

	public int getNotifyAllCount() {
		return notifyAllCount;
	}

	public int getBlockedCount() {
		return blockedCount;
	}

	public int getWaitingCout() {
		return waitingCout;
	}

	public int getMonitorEnteredCount() {
		return monitorEnteredCount;
	}

	public int getMonitorContendedCount() {
		return monitorContendedCount;
	}

	public Monitor getRequestedResource() {
		return requestedResource;
	}	
}
