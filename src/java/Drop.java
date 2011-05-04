public class Drop {
	// Message sent from producer to consumer.
	private String message;
	// True if consumer should wait for producer to send message, false
	// if producer should wait for consumer to retrieve message.
	private boolean empty = true;

	public synchronized String take() {
		// Wait until message is available.
		while (empty) {
			try {
				wait();
				System.out.println(Thread.currentThread().getName() + " invoked wait()");
			} catch (InterruptedException e) {
			}
		}
		// Toggle status.
		empty = true;
		// Notify producer that status has changed.
		notifyAll();
		System.out.println(Thread.currentThread().getName() + " invoked notifyAll()");
		return message;
	}

	public synchronized void put(String message) {
		// Wait until message has been retrieved.
		while (!empty) {
			try {
				wait();
				System.out.println(Thread.currentThread().getName() + " invoked wait()");
			} catch (InterruptedException e) {
			}
		}
		// Toggle status.
		empty = false;
		// Store message.
		this.message = message;
		// Notify consumer that status has changed.
		notifyAll();
		System.out.println(Thread.currentThread().getName() + " invoked notifyAll()");
	}
}