package de.fu.profiler.model;

public class ThreadInfo implements Comparable<ThreadInfo> {

	final String name;
	final int priority;
	final String state;
	final boolean isContextClassLoaderSet;

	public ThreadInfo(String name, int priority, String state, boolean ccl) {
		super();
		this.name = name;
		this.priority = priority;
		this.state = state;
		this.isContextClassLoaderSet = ccl;
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

	@Override
	public int compareTo(ThreadInfo threadInfo) {
		return this.name.compareTo(threadInfo.name);
	}

}
