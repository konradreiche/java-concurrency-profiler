package de.fu.profiler.examples;

import java.util.*;

public class Sorts implements Runnable {

	static Queue<Thread> threadPool = new LinkedList<Thread>();
	static int maxThreads;
	
	
	private int low;
	private int high;
	
	private static int[] list;
	private static int[] temp;	
	
	
	/*
	 * maxThreads sets the maximum Threads next to the
	 * thread controller being active on sorting
	 */
	public Sorts(int maxThreads) {
		Sorts.maxThreads = maxThreads;
	}
	
	/*
	 * The current indices are needed
	 * when starting a new thread 
	 */
	public Sorts(int low, int high) {
		this.low = low;
		this.high = high;
	}
	
	/*
	 * pre-function to decide whether the list
	 * is sorted with a single-thread or
	 * multithreads, the list and helper list
	 * is initialized  
	 */
	public void mergeSort(int[] list, boolean concurrent) throws InterruptedException {

		int n = list.length;
		
		Sorts.list = list;
		Sorts.temp = new int[n];
		
		if (concurrent) {
			mergeSort(0,n-1,concurrent);
		}
		else {
			mergeSort(0,n-1);
		}
	}
	
	/*
	 * Standard Mergesort for single-threaded
	 * compuation
	 */
	private void mergeSort(int low, int high) {
		
		if (low < high) {
			int m = (low+high)/2;
			mergeSort(low,m);
			mergeSort(m+1,high);
			merge(low,m,high);
		}
	}
	
	/*
	 * The Thread Pool Pattern is used to manage the running threads
	 * 
	 * If there are atleast 2 free spots in the pool, the recursion
	 * is run by 2 new threads
	 * 
	 * If there is only 1 free spot in the pool, the first recursion
	 * call is run by 1 new thread and the second recursion by
	 * the thread controller
	 * 
	 * If there is no free spot in the pool, both recursion calls
	 * are run by the thread controller
	 * 
	 * IMPORTANT:
	 * Everytime a new thread is created the thread controller
	 * enters a infinite loop, until the new thread is finished
	 * It guarantees independent recursive calls
	 */
	private void mergeSort(int low, int high, boolean concurrent) throws InterruptedException {
		
		if (low < high) {
			
	        int m = (low+high)/2;

	        if (maxThreads - threadPool.size() >= 2) {
	            	
            		Thread t1 = new Thread(new Sorts(low,m));
            		Thread t2 = new Thread(new Sorts(m+1,high));
            		
            		threadPool.add(t1);
            		threadPool.add(t2);
            		
            		t1.start();
            		t2.start();

            		while (true) {
            			while (true) {
            				t1.join();
            				threadPool.remove(t1);
            				break;
            			}
						t2.join();
            			threadPool.remove(t2);
            			break;            			
            		}
   
     
            		merge(low,m,high);
	        }
	        
	        else if (maxThreads - threadPool.size() == 1) {
	        	
	        	Thread t = new Thread(new Sorts(low,m));
	        	
	        	threadPool.add(t);
	        	
	        	t.start();
	        	
	        	mergeSort(m+1,high);

	        	while (true) {
	        		t.join();
	        		threadPool.remove(t);
	        		break;
	        	}
	        	
	        	merge(low,m,high);
	        	
	        }
	        
	        else {            	
                mergeSort(low,m);
                mergeSort(m+1,high);
                merge(low,m,high);
	        }
		}
	}
	
	
	/*
	 * Standard Mergesort for both single-threaded
	 * and multi-threaded compuation
	 */
	private void merge(int low, int m, int high)
	{
		int i, j, k;
		
		i = k = low;
		j = m + 1;
		
		while((i <= m) && (j <= high)) {
		    if(list[j] < list[i]) {
		    	temp[k++] = list[j++];
		    }
		    else {
		    	temp[k++] = list[i++];
		    }
		}
		
		if(i <= m) {
		    while(i <= m) {
		    	temp[k++] = list[i++];
		    }
		}
		else {
		    while(j <= high) {
		    	temp[k++] = list[j++];
		    }
		}
		
		for(k = low; k <= high; ++k) {
			list[k] = temp[k];
		}
	}

	/*
	 * Standard Mergesort for multi-threaded
	 * compuation
	 */
	@Override
	public void run() {
		
		if (low < high) {
            int m = (low+high)/2;
 
            try {
            	mergeSort(low,m,true);
            	mergeSort(m+1,high,true);            	
            } catch (InterruptedException e) {
            	e.printStackTrace();
            }
            
        	merge(low,m,high);
        }
	}
	
}
