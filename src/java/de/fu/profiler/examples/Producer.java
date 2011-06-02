package de.fu.profiler.examples;



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
		
		try {
			for (int i = 0; i < importantInfo.length; i++) {
				boundedBuffer.put(importantInfo[i]);
				Thread.sleep(5000);
			}
	
			for (int i = 0; i < consumerCount; ++i) {
				boundedBuffer.put("DONE");
			}
			
		} catch (InterruptedException e) {
		}
	}
}
