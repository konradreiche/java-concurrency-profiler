public class ThreadPlayground {

	public static class SimpleTask implements Runnable {

		Object lock;
		
		public SimpleTask(Object lock) {
			this.lock = lock;
		}
		
		@Override
		public void run() {
			
			synchronized (lock) {
				lock.notifyAll();				
			}
				
						
		}
	}
	
	public static class WaitingThread implements Runnable {

		Object lock;
		
		public WaitingThread(Object lock) {
			this.lock = lock;
		}
		
		@Override
		public void run() {
			
			synchronized (lock) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			System.out.println(Thread.currentThread().getName() + "succeed!");
			
		}
		
	}
	
	public static void main(String args[]) {
		
		Object lock = new Object();
		new Thread(new WaitingThread(lock)).start();
		new Thread(new SimpleTask(lock)).start();

		
		
	}
	
}
