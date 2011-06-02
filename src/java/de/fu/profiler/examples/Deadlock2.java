package de.fu.profiler.examples;

public class Deadlock2 {
	
	static Object lockA = new Object();
	static Object lockB = new Object();
	
	static Runnable one = new Runnable() {
		
		@Override
		public void run() {
			
			synchronized (lockA) {
	
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				System.out.println("oi");
				
				synchronized (lockB) {
					System.out.println(Thread.currentThread().getName() +  " got both locks");
				}
			}
		}
	};
	
	static Runnable two = new Runnable() {
		
		@Override
		public void run() {
						
			synchronized (lockB) {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				System.out.println("oi");
				
				synchronized (lockA) {
					System.out.println(Thread.currentThread().getName() +  " got both locks");
				}
			}
		}
	};
	
	public static void main(String... args) {
		new Thread(one).start();
		new Thread(two).start();
	}

}
