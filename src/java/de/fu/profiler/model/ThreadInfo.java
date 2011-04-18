package de.fu.profiler.model;

public class ThreadInfo implements Comparable<ThreadInfo> {

	final int id;
	final String name;
	final int priority;
	final String state;
	final boolean isContextClassLoaderSet;
	
	int contendedMonitorWaitCount;

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

	public void incContendedMonitorWait() {
		++contendedMonitorWaitCount;
		System.out.println("Hurray, now it is " + contendedMonitorWaitCount + " on " + name);
	}
	
	@Override
	public int compareTo(ThreadInfo threadInfo) {
		return new Integer(id).compareTo(new Integer(threadInfo.id));
	}

	@Override
	public boolean equals(Object o) {
		return new Integer(id).equals(new Integer(((ThreadInfo) o).id));
	}

}
