package de.fu.profiler.model;

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

	public Monitor(long id, int entryCount, int waiterCount,
			int notifyWaiterCount) {
		super();
		this.id = id;
		this.entryCount = entryCount;
		this.waiterCount = waiterCount;
		this.notifyWaiterCount = notifyWaiterCount;
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
}
