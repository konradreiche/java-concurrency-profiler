package de.fu.profiler.model;

import java.util.ArrayList;
import java.util.List;

public class MethodInfo {

	String className;
	String methodName;
	long clockCycles;
	long time;
	int numberOfInvocations;
	List<ThreadInfo> callees;
	double timePercent;

	public MethodInfo(String className, String methodName, long clockCycles,
			long time, ThreadInfo firstCallee) {

		this.className = className;
		this.methodName = methodName;
		this.clockCycles = clockCycles;
		this.time = time;
		this.numberOfInvocations = 1;
		this.callees = new ArrayList<ThreadInfo>();
		this.callees.add(firstCallee);
	}
	
	public void wasInvoked(long clockCycles, long time, ThreadInfo callee) {
		this.clockCycles += clockCycles;
		this.time += time;
		this.numberOfInvocations += 1;
		this.callees.add(callee);
	}
	
	public void setTimeInPercent(double timePercent) {
		this.timePercent = timePercent;
	}

	public static void updateRelativeTime(JVM jvm) {
		
		long overallTime = 0;
		for (MethodInfo method : jvm.getMethods().values()) {
			overallTime += method.time;			
		}
		
		for (MethodInfo method : jvm.getMethods().values()) {
			method.setTimeInPercent((double)method.time/overallTime);
		}
		
	}
}
