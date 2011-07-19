/**
 * 
 */
package de.fu.profiler.examples;

import java.util.Random;

class Writer implements Runnable {
	private static final int NR_WRITES_PER_WRITER = 20;
	
	Writer(IMap<Integer,Integer> rwm){
		this.rwm = rwm;
	}

	public void run(){ 
		doWrites();
	}
	private void doWrites(){
		for (int i=0; i < NR_WRITES_PER_WRITER; i++){
			for (int j=0; j < 50; j++){
				Random r = new Random(i);
				int val = r.nextInt();
				if (rwm.containsKey(val)){
					rwm.put(val, (Integer)rwm.get(val)+1);
				} else {
					rwm.put(val, r.nextInt());
				}
			}
		}
	}
	private final IMap<Integer, Integer> rwm;
}