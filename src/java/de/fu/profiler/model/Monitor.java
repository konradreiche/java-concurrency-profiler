package de.fu.profiler.model;

import java.util.Map;

/**
 * Represents a Java monitor.
 * 
 * @author Konrad Johannes Reiche
 * 
 */
public class Monitor {

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
	ThreadInfo allocatedToThread;

	Map<ThreadInfo,StackTrace> waiter;

	Map<ThreadInfo,StackTrace> notifyWaiter;

	public Monitor(long id, String className, int entryCount, int waiterCount,
			int notifyWaiterCount, Map<ThreadInfo,StackTrace> waiter,
			Map<ThreadInfo,StackTrace> notifyWaiter) {
		super();
		this.id = id;
		this.className = className;
		this.entryCount = entryCount;
		this.waiterCount = waiterCount;
		this.notifyWaiterCount = notifyWaiterCount;
		this.waiter = waiter;
		this.notifyWaiter = notifyWaiter;
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

	public void compareAndSet(Monitor monitor) {

		if (monitor.getId() != id) {
			throw new IllegalStateException(
					"The monitors id does not match the monitors id to be updated.");
		}

		if (className.equals("N/A") && !monitor.getClassName().equals("N/A")) {
			className = monitor.getClassName();
		}

		if (entryCount != monitor.getEntryCount()) {
			entryCount = monitor.getEntryCount();
		}

		if (waiterCount != monitor.getWaiterCount()) {
			waiterCount = monitor.getWaiterCount();
			waiter = monitor.getWaiter();
		}

		if (notifyWaiterCount != monitor.getNotifyWaiterCount()) {
			notifyWaiterCount = monitor.getNotifyWaiterCount();
			notifyWaiter = monitor.getNotifyWaiter();
		}
	}

	public ThreadInfo getAllocatedToThread() {
		return allocatedToThread;
	}

	public Map<ThreadInfo,StackTrace> getWaiter() {
		return waiter;
	}

	public Map<ThreadInfo,StackTrace> getNotifyWaiter() {
		return notifyWaiter;
	}
	
	

}
