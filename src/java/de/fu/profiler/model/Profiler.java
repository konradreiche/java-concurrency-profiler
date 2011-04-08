package de.fu.profiler.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

public class Profiler {

	final Map<Integer, JVM> IDsToJVMs;

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
