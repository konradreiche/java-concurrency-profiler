package de.fu.profiler.examples;

import java.util.Random;
import java.util.TreeMap;
import java.util.Map;

public class Test {
	private static final int INITIAL_SIZE = 50;

	private static final int NR_READER_THREADS = 2;
	private static final int NR_WRITER_THREADS = 1;

	public static void main(String[] args) throws InterruptedException {
		// performTest("ReadWriteMap: ", new ReadWriteMap(createMap()));
		performTest("SyncMap: ", new SyncMap<Integer, Integer>(createMap()));
		// performTest("ReentrantSyncMap: ", new ReentrantSyncMap(createMap()));
	}

	private static Map<Integer, Integer> createMap() {
		TreeMap<Integer, Integer> treemap = new TreeMap<Integer, Integer>();
		Random r = new Random(17);
		for (int i = 0; i < INITIAL_SIZE; i++) {
			treemap.put(i, r.nextInt() % 10);
		}
		return treemap;
	}

	private static void performTest(String s1, IMap<Integer, Integer> m)
			throws InterruptedException {

		Thread[] readerThreads = new Thread[NR_READER_THREADS];
		for (int i = 0; i < NR_READER_THREADS; i++) {
			readerThreads[i] = new Thread(new Reader(m));
		}
		Thread[] writerThreads = new Thread[NR_WRITER_THREADS];
		for (int i = 0; i < NR_WRITER_THREADS; i++) {
			writerThreads[i] = new Thread(new Writer(m));
		}

		for (int i = 0; i < NR_READER_THREADS; i++) {
			readerThreads[i].start();
		}
		for (int i = 0; i < NR_WRITER_THREADS; i++) {
			writerThreads[i].start();
		}

		for (int i = 0; i < NR_READER_THREADS; i++) {
			readerThreads[i].join();
		}
		for (int i = 0; i < NR_WRITER_THREADS; i++) {
			writerThreads[i].join();
		}

	}
}
