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
	 * The name of the class which is used as a monitor.
	 */
	final String className;
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

	public Monitor(long id, String className, int entryCount, int waiterCount,
			int notifyWaiterCount) {
		super();
		this.id = id;
		this.className = className;
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

	public String getClassName() {
		return className;
	}
}
