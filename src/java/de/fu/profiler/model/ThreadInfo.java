package de.fu.profiler.model;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import de.fu.profiler.Cloneable;
import de.fu.profiler.service.AgentMessageProtos.AgentMessage;

/**
 * Models a java thread and describes selected and analysed information of a
 * thread.
 * 
 * @author Konrad Johannes Reiche
 * 
 */
public class ThreadInfo implements Comparable<ThreadInfo>,
		Cloneable<ThreadInfo>, Node<ThreadInfo> {

	/**
	 * A generated id by the profiler.
	 */
	final long id;

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
	 * Whether the thread is a daemon thread.
	 */
	boolean isDaemon;
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
	 * The monitor which is requested, i.e. trying to be acquired by this
	 * thread. Null if the thread does not currently try to acquire a monitor.
	 */
	MonitorInfo requestedResource;

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
	public ThreadInfo(long id, String name, int priority, String state,
			boolean ccl, boolean isDaemon, long timestamp) {
		super();
		this.id = id;
		this.name = name;
		this.priority = priority;
		this.state = state;
		this.isContextClassLoaderSet = ccl;
		this.cpuTime = -1;
		this.timeSinceLastUpdate = timestamp;
		this.stateToDuration = new ConcurrentHashMap<String, Long>();

		String possibleStates[] = new String[] { "New", "Runnable", "Blocked",
				"Waiting", "Timed Waiting", "Terminated" };

		for (String possibleState : possibleStates) {
			stateToDuration.put(possibleState, 0l);
		}
	}

	public ThreadInfo(AgentMessage.Thread thread, long timestamp) {
		this(thread.getId(), thread.getName(), thread.getPriority(),
				formatThreadState(thread.getState()), thread
						.getIsContextClassLoaderSet(), thread.getIsDaemon(),
				timestamp);
	}

	public static String formatThreadState(AgentMessage.Thread.State state) {

		String result = state.toString();
		int index;
		while ((index = result.indexOf("_")) != -1) {
			String head = result.substring(0, index);
			String firstLetter = result.substring(index + 1, index + 2);
			String tail = result.substring(index + 2);
			result = head.toLowerCase() + " " + firstLetter.toUpperCase()
					+ tail.toLowerCase();
		}

		String firstLetter = result.substring(0, 1);
		String remainder = result.contains(" ") ? result.substring(1) : result
				.substring(1).toLowerCase();
		result = firstLetter.toUpperCase() + remainder;

		return result;
	}

	@Override
	public ThreadInfo copy() {
		ThreadInfo thread = new ThreadInfo(id, name, priority, state,
				isContextClassLoaderSet, isDaemon, timeSinceLastUpdate);
		thread.waitCount = waitCount;
		thread.notifyCount = notifyCount;
		thread.notifyAllCount = notifyAllCount;
		thread.blockedCount = blockedCount;
		thread.waitingCout = waitingCout;
		thread.monitorEnteredCount = monitorEnteredCount;
		thread.monitorContendedCount = monitorContendedCount;
		thread.cpuTime = thread.cpuTime;

		for (Entry<String, Long> entry : thread.stateToDuration.entrySet()) {
			thread.stateToDuration.put(entry.getKey(), entry.getValue());
		}

		thread.requestedResource = requestedResource;

		return thread;
	}

	public long getId() {
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
		return Long.valueOf(id).compareTo(Long.valueOf(threadInfo.id));
	}

	/**
	 * Compares the threads by their id.
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object o) {

		if (!(o instanceof ThreadInfo)) {
			return false;
		} else {
			return Long.valueOf(id).equals(Long.valueOf(((ThreadInfo) o).id));
		}
	}

	public void setState(String state) {
		this.state = state;
	}

	public void setCpuTime(long cpuTime) {
		this.cpuTime = cpuTime;
	}

	public void update(long systemTime, int id, String name, int priority,
			String state, boolean isContextClassLoaderSet) {

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

		if (timeSinceLastUpdate > systemTime) {

		}

		if (systemTime < timeSinceLastUpdate) {
			System.out.println(systemTime);
			System.out.println(timeSinceLastUpdate);
			System.out.println("Error, old timestamp arrived at state upadte");
		} else {
			long timeSpentInState = systemTime - timeSinceLastUpdate;
			long oldTimeSpentInState = stateToDuration.get(this.state);
			stateToDuration.put(this.state, oldTimeSpentInState
					+ timeSpentInState);
			timeSinceLastUpdate = systemTime;
		}

		if (!this.state.equals(state)) {

			if (state.equals("Blocked")) {
				++blockedCount;
			} else if (state.equals("Waiting")) {
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

	public MonitorInfo getRequestedResource() {
		return requestedResource;
	}

	public void increaseMonitorContendedCount() {
		++monitorContendedCount;
	}

	public void increaseMonitorEnteredCount() {
		++monitorEnteredCount;
	}

	public void increaseNotifyCount() {
		++notifyCount;
	}

	public void increaseNotifyAllCount() {
		++notifyAllCount;
	}

	public void increaseWaitCount() {
		++waitCount;
	}
}
