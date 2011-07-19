/**
 * 
 */
package de.fu.profiler.examples;

import java.util.Random;

class Reader implements Runnable {
	private static final int NR_READS_PER_READER = 20;
	
	Reader(IMap<Integer,Integer> rwm){
		this.rwm = rwm;
	}
	public void run(){
		doReads();
	}
	private void doReads(){	
		int result = 0;
		for (int i=0; i < NR_READS_PER_READER; i++){
			for (int j=0; j < rwm.size(); j++){
				Random r = new Random(j);
				int val = r.nextInt();
				if (rwm.containsKey(val)){
					result += ((Integer)rwm.get(val)).intValue(); 
				}
			}
		}
	}
	private final IMap<Integer,Integer> rwm;
}