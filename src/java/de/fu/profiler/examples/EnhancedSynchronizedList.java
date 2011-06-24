package de.fu.profiler.examples;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class EnhancedSynchronizedList<T> {

	public List<T> list = Collections.synchronizedList(new ArrayList<T>());

	public synchronized boolean putIfAbsent(T x) {
		boolean absent = !list.contains(x);
		if (absent) {
			list.add(x);
		}
		return absent;
	}

	public static void main(String... args) {

		final EnhancedSynchronizedList<Integer> list = new EnhancedSynchronizedList<Integer>();
		
		for (int i = 0; i < 50; ++i) {
			
			new Thread(new Runnable() {
				
				@Override
				public void run() {
					
					while (true) {
						Random r = new Random();
						list.list.add(r.nextInt());
						Thread.yield();
						list.putIfAbsent(r.nextInt());
						System.out.println(list.list.size());
					}
					
				}
			}).start();
			
		}	
				

	}
}
