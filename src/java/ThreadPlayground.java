public class ThreadPlayground {

	public static class SimpleTask implements Runnable {

		Object lock;
		
		public SimpleTask(Object lock) {
			this.lock = lock;
		}
		
		@Override
		public void run() {
			
			
				try {
					synchronized (lock) {
						System.out.println(Thread.currentThread().getName() + " is waiting ");
						Thread.sleep(5000);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
						
		}
	}
	
	public static void main(String args[]) {
		
		Object lock = new Object();
		new Thread(new SimpleTask(lock)).start();
		new Thread(new SimpleTask(lock)).start();
		new Thread(new SimpleTask(lock)).start();
		new Thread(new SimpleTask(lock)).start();
		new Thread(new SimpleTask(lock)).start();
		new Thread(new SimpleTask(lock)).start();
		
		
	}
	
}
