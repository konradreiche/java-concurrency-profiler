package de.fu.profiler.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

public class JVM extends Observable {

	final int id;
	final String name;
	final List<ThreadInfo> threads;

	public JVM(int id, String name) {
		super();
		this.id = id;
		this.name = name;
		this.threads = new ArrayList<ThreadInfo>();
	}

	
	
	public int getId() {
		return id;
	}



	public String getName() {
		return name;
	}



	public List<ThreadInfo> getThreads() {
		return threads;
	}
	
	public void addThread(ThreadInfo threadInfo) {
		threads.add(threadInfo);
		setChanged();
		notifyObservers();
	}
	
	public void clearThreads() {
		threads.clear();
		notifyObservers();
	}

}
