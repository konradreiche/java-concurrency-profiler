package de.fu.profiler.examples;

public class Consumer implements Runnable {

	private BoundedBuffer<String> boundedBuffer;
	

	public Consumer(BoundedBuffer<String> boundedBuffer) {
		this.boundedBuffer = boundedBuffer;
	}

	public void run() {

		try {
			for (String message = boundedBuffer.take(); !message.equals("DONE"); message = boundedBuffer
					.take()) {
				System.out.println("Consumer "
						+ Thread.currentThread().getName()
						+ " MESSAGE RECEIVED: " + message);
			}
		} catch (InterruptedException e) {
		}
	}
}