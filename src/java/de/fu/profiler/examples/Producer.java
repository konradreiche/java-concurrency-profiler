package de.fu.profiler.examples;

import java.util.Random;


public class Producer implements Runnable {

	private BoundedBuffer<String> boundedBuffer;
	private int consumerCount;

	public Producer(BoundedBuffer<String> boundedBuffer, int consumerCount) {
		this.boundedBuffer = boundedBuffer;
		this.consumerCount = consumerCount;
	}

	public void run() {
		String importantInfo[] = { "Mares eat oats", "Does eat oats",
				"Little lambs eat ivy", "A kid will eat ivy too" };
		
		Random random = new Random();
		
		try {
			for (int i = 0; i < importantInfo.length; i++) {
				boundedBuffer.put(importantInfo[i]);
				Thread.sleep(random.nextInt(3000) + 2000);
			}
	
			for (int i = 0; i < consumerCount; ++i) {
				boundedBuffer.put("DONE");
			}
			
		} catch (InterruptedException e) {
		}
	}
}
