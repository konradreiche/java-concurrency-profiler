package de.fu.profiler.model;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Models a Java Virtual Machine.
 * 
 * @author Konrad Johannes Reiche
 * 
 */
public class JVM {

	/**
	 * Generated id by the profiling agent.
	 */
	final int id;

	/**
	 * The name of the executed java program.
	 */
	final String name;

	/**
	 * A list of threads of the JVM.
	 */
	final Set<ThreadInfo> threads;

	/**
	 * Standard constructor.
	 * 
	 * @param id
	 *            generated id by the profiling agent.
	 * @param name
	 *            the name of the executed java program.
	 */
	public JVM(int id, String name) {
		super();
		this.id = id;
		this.name = name;
		this.threads = new CopyOnWriteArraySet<ThreadInfo>();
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Set<ThreadInfo> getThreads() {
		return threads;
	}

	/**
	 * Adds a new thread and notifies the graphical elements to display the
	 * changes.
	 * 
	 * @param threadInfo
	 *            a thread.
	 */
	public void addThread(ThreadInfo threadInfo) {
		
		if (threads.contains(threadInfo)) {
			threads.remove(threadInfo);
		}
		threads.add(threadInfo);
	}

	/**
	 * Removes all threads and notifies the graphical elements to display the
	 * changes.
	 */
	public void clearThreads() {
		threads.clear();
	}
	
	/**
	 * Returns the thread based on its id.
	 * 
	 * @param id Thread id.
	 */
	public ThreadInfo getThread(int id) {
		for (ThreadInfo thread : threads) {
			if (thread.getId() == id) {
				return thread;
			}
		}
		
		return null;
	}

}
