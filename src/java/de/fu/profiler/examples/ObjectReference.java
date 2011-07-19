package de.fu.profiler.examples;

class ObjectReference {

	private volatile Object obj = null;

	public void set(Object obj) {
		if (obj == null) {
			throw new IllegalArgumentException();
		}
		this.obj = obj;
		synchronized (this) {
			notifyAll();
		}
	}

	/**
	 * This method never returns null
	 * 
	 * @throws InterruptedException
	 */
	public Object waitAndGet() throws InterruptedException {
		if (obj != null) {
			return obj;
		}
		synchronized (this) {
			System.out.println("waiting");
			wait();
			return obj;
		}
	}

	public static void main(String... args) throws InterruptedException {

		final ObjectReference ref = new ObjectReference();
		Runnable getter = new Runnable() {

			@Override
			public void run() {
				try {
					
					Object mine = ref.waitAndGet();
					System.out.println(mine);
				} catch (InterruptedException e) {
				}
			}
		};

		new Thread(getter).start();

		final Notificator not = new Notificator();
		
		Runnable spuriousWakeUp = new Runnable() {

			@Override
			public void run() {

				not.doIt();
			}
		};
		
		Runnable waiter = new Runnable() {
			
			@Override
			public void run() {
				try {
					not.waitAndJo();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		};
		
		new Thread(waiter).start();
		Thread.sleep(1000);
		
		new Thread(spuriousWakeUp).start();
	}
}

class Notificator {

	public synchronized void doIt() {
		notifyAll();
		System.out.println("Notifying");
	}

	public synchronized void waitAndJo() throws InterruptedException {
		wait();
		System.out.println("Awesome");

	}

}
