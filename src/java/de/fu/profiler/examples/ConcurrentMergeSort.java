package de.fu.profiler.examples;

import java.util.*;

public class ConcurrentMergeSort {

	public static void main(String[] args) {

		int maxThreads = Integer.parseInt(args[0]);

		long startTime, finishTime;
		

		Sorts s = new Sorts(maxThreads);
		
		int size = 40000;
		
//		for (int i = 1; i <= 14; ++i) {
		
			int[] list = generateIntegerList(size);

			startTime = System.nanoTime();

			try {
				
//				if (i % 2 == 1) {
//					s.mergeSort(list,false);
//					finishTime = System.nanoTime();
//					System.out.printf("%8d" + ": %12f ms [1+0 Thread(s)] %n",
//							size,(finishTime-startTime) / 1000000.0);
//				}
//				
//				else {				
					s.mergeSort(list,true);
					finishTime = System.nanoTime();
					System.out.printf("%8d" + ": %12f ms [1+" + maxThreads + " Thread(s)] %n%n",
							size,(finishTime-startTime) / 1000000.0);

//					size *= 10;
//				}
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
//		}
	}
	
	public static int[] generateIntegerList(int size) {
		
		int[] result = new int[size];
		
		Random randomGenerator = new Random();
		
		for (int i = 0; i < size; ++i) {
			result[i] = randomGenerator.nextInt();
		}
		
		return result;
	}
	
}
