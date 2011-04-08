package de.fu.profiler.model;

public class ThreadInfo implements Comparable<ThreadInfo> {

	final String name;
	final int priority;
	final boolean isContextClassLoaderSet;
	
	public ThreadInfo(String name, int priority, boolean ccl) {
		super();
		this.name = name;
		this.priority = priority;
		this.isContextClassLoaderSet = ccl;
	}

	public String getName() {
		return name;
	}

	public int getPriority() {
		return priority;
	}

	public boolean isContextClassLoaderSet() {
		return isContextClassLoaderSet;
	}

	@Override
	public int compareTo(ThreadInfo threadInfo) {
		return this.name.compareTo(threadInfo.name);
	}
	
	

}
