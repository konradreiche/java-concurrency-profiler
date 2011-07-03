package de.fu.profiler.model;

import java.util.Map;
import java.util.TreeMap;

import de.fu.profiler.Cloneable;

/**
 * Represents a Java monitor.
 * 
 * @author Konrad Johannes Reiche
 * 
 */
public class MonitorInfo implements Cloneable<MonitorInfo> {

	/**
	 * Generated id by the profiling agent.
	 */
	final long id;

	/**
	 * The name of the class which is used as a monitor.
	 */
	String className;
	/**
	 * The number of times the owning thread has entered the monitor.
	 */
	int entryCount;

	/**
	 * The number of threads waiting to own this monitor.
	 */
	int waiterCount;

	/**
	 * The number of threads waiting to be notified by this monitor.
	 */
	int notifyWaiterCount;

	/**
	 * The monitor is allocated, i.e. held to this thread. Null if the monitor
	 * is not allocated.
	 */
	ThreadInfo ownedByThread;

	Map<ThreadInfo, StackTrace> waiter;

	Map<ThreadInfo, StackTrace> notifyWaiter;

	public MonitorInfo(long id, String className, int entryCount,
			int waiterCount, int notifyWaiterCount,
			Map<ThreadInfo, StackTrace> waiter,
			Map<ThreadInfo, StackTrace> notifyWaiter) {
		super();
		this.id = id;
		this.className = className;
		this.entryCount = entryCount;
		this.waiterCount = waiterCount;
		this.notifyWaiterCount = notifyWaiterCount;
		this.waiter = waiter;
		this.notifyWaiter = notifyWaiter;
	}

	@Override
	public MonitorInfo copy() {

		MonitorInfo monitorInfo = new MonitorInfo(id, className, entryCount,
				waiterCount, notifyWaiterCount, waiter, notifyWaiter);
		monitorInfo.ownedByThread = ownedByThread;
		monitorInfo.waiter = new TreeMap<ThreadInfo, StackTrace>(waiter);
		monitorInfo.notifyWaiter =  new TreeMap<ThreadInfo, StackTrace>(notifyWaiter);

		return monitorInfo;
	}

	public long getId() {
		return id;
	}

	public int getEntryCount() {
		return entryCount;
	}

	public int getWaiterCount() {
		return waiterCount;
	}

	public int getNotifyWaiterCount() {
		return notifyWaiterCount;
	}

	public String getClassName() {
		return className;
	}

	public void update(int id, String className, int entryCount,
			int waiterCount, int notifyWaiterCount,
			Map<ThreadInfo, StackTrace> waiter,
			Map<ThreadInfo, StackTrace> notifyWaiter) {

		if (this.id != id) {
			throw new IllegalStateException(
					"The monitors id does not match the monitors id to be updated.");
		}

		if (this.className.equals("N/A") && !className.equals("N/A")) {
			this.className = className;
		}

		if (this.entryCount != entryCount) {
			this.entryCount = entryCount;
		}

		if (this.waiterCount != waiterCount) {
			this.waiterCount = waiterCount;
			this.waiter = waiter;
		}

		if (this.notifyWaiterCount != notifyWaiterCount) {
			this.notifyWaiterCount = notifyWaiterCount;
			this.notifyWaiter = notifyWaiter;
		}
	}

	public ThreadInfo getOwningThread() {
		return ownedByThread;
	}

	public Map<ThreadInfo, StackTrace> getWaiter() {
		return waiter;
	}

	public Map<ThreadInfo, StackTrace> getNotifyWaiter() {
		return notifyWaiter;
	}

	public void setOwningThread(ThreadInfo owningThread) {
		ownedByThread = owningThread;
	}
}
