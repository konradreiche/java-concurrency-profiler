package de.fu.profiler.examples;

import java.util.LinkedList;

public class Santa extends Thread {
	public static final int REINDEERS = 9;
	public static final int ELVES = 10;
	public static final int ELF_GROUP = 3;
	private Reindeer[] reindeer;
	private Elf[] elf;
	private LinkedList<Elf> elves = new LinkedList<Elf>();
	private int reindeers = 0;

	public Santa() {
		reindeer = new Reindeer[Santa.REINDEERS];
		elf = new Elf[Santa.ELVES];
		for (int i = 0; i < reindeer.length; ++i) {
			reindeer[i] = new Reindeer(this, i);
			reindeer[i].setName("Reindeer-" + i);
		}
		for (int i = 0; i < elf.length; ++i) {
			elf[i] = new Elf(this, i);
			elf[i].setName("Elf-" + i);
		}
		for (Thread t : reindeer)
			t.start();
		for (Thread t : elf)
			t.start();
	}

	public void run() {
		synchronized (this) {
			for (;;) {
				System.out.println("Santa is sleeping");
				while (reindeers < Santa.REINDEERS &&
				       elves.size() < Santa.ELF_GROUP) {
						try {
							wait();
						} catch (InterruptedException ie) {
							// Ignore
						}
					}
					if (reindeers >= Santa.REINDEERS) {
					System.out.println("Santa is delivering toys");
					for (Reindeer r : reindeer) {
						r.harness();
					}
					try {
						Thread.sleep(500);
					} catch (InterruptedException ie) {
						// Ignore
					}
					for (Reindeer r : reindeer) {
						r.unHarness();
						--reindeers;
					}
				} else if (elves.size() >= Santa.ELF_GROUP) {
					System.out.println("Santa is meeting in the study");
					for (int i = 0; i < Santa.ELF_GROUP; ++i) {
						elves.get(i).showIn();
					}
					try {
						Thread.sleep(500);
					} catch (InterruptedException ie) {
						// Ignore
					}
					for (int i = 0; i < Santa.ELF_GROUP; ++i) {
						elves.remove().showOut();
					}
				} else {
					System.out.println("This should not happen!");
					System.exit(1);
				}
			}
		}
	}
	public synchronized void deliverToys() {
		reindeers++;
		if (reindeers == Santa.REINDEERS) notifyAll();
	}

	public synchronized void ask(Elf elf) {
		elves.add(elf);
		if (elves.size() == Santa.ELF_GROUP) notifyAll();
	}

	public static void main(String[] args) {
		Santa santa = new Santa();
		santa.setName("Santa Clause");
		santa.start();
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			// Ignore
		}
		System.out.println("This is the end");
		System.exit(0);
	}
}
