package de.fu.profiler.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

/**
 * Models the state of the profiler itself.
 * 
 * @author Konrad Johannes Reiche
 * 
 */
public class Profiler {

	/**
	 * All available JVMs.
	 */
	final Map<Integer, JVM> IDsToJVMs;

	/**
	 * At the start of the profiler all available JVMs are read and listed.
	 */
	public Profiler() {
		super();
		this.IDsToJVMs = new ConcurrentHashMap<Integer, JVM>();

		for (VirtualMachineDescriptor vmd : VirtualMachine.list()) {
			IDsToJVMs.put(Integer.parseInt(vmd.id()),
					new JVM(Integer.parseInt(vmd.id()), vmd.displayName()));
		}
	}

	public Map<Integer, JVM> getIDsToJVMs() {
		return IDsToJVMs;
	}
}
