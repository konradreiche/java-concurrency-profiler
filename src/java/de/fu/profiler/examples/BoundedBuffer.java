package de.fu.profiler.examples;

/**
 * A bounded buffer using Java condition queues.
 * 
 * @author Konrad Johannes Reiche
 *
 * @param <V>
 */
public class BoundedBuffer<V> extends BaseBoundedBuffer<V> {

	protected BoundedBuffer(int capacity) {
		super(capacity);
	}
	
	public synchronized void put(V v) throws InterruptedException {
		while (isFull()) {
			wait();
		}
		doPut(v);
		notifyAll();
	}
	
	public synchronized V take() throws InterruptedException {
		while (isEmpty()) {
			wait();
		}
		V v = doTake();
		notifyAll();
		return v;
	}
	
	public static void main(String args[]) {
		
		BoundedBuffer<String> boundedBuffer = new BoundedBuffer<String>(1);
		(new Thread(new Producer(boundedBuffer,3))).start();
		(new Thread(new Consumer(boundedBuffer))).start();
		(new Thread(new Consumer(boundedBuffer))).start();
		(new Thread(new Consumer(boundedBuffer))).start();
	}

}
