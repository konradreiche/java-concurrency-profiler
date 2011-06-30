package de.fu.profiler.examples;


public class Reindeer extends Thread {

	private final Santa santa;
	private final int name;
	private boolean santa_is_harnessing = false;

	public Reindeer(Santa santa, int name) {
		this.santa = santa;
		this.name = name;
	}

	public void run() {
		synchronized (this) {
			for (;;) {
				System.out.println("Reindeer " + name + " is on vacation");
				try {
					Thread.sleep(3000);
				} catch (InterruptedException ie) {
					// Ignore
				}
				System.out.println("Reindeer " + name + " returns from vacation");
				santa.deliverToys();
				while (!santa_is_harnessing) {
					try {
						wait();
					} catch (InterruptedException ie) {
						// Ignore
					}
				}
				System.out.println("Reindeer " + name + " is pulling sleigh");
				while (santa_is_harnessing) {
					try {
						wait();
					} catch (InterruptedException ie) {
						// Ignore
					}
				}
			}
		}
	}

	public synchronized void harness() {
		santa_is_harnessing = true;
		notify();
	}

	public synchronized void unHarness() {
		santa_is_harnessing = false;
		notify();
	}
}
