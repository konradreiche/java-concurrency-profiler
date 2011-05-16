package de.fu.profiler.examples;


public class Elf extends Thread {

	private final Santa santa;
	private final int name;
	private boolean santa_is_listening = false;

	public Elf(Santa santa, int name) {
		this.santa = santa;
		this.name = name;
	}

	public void run() {
		synchronized (this) {
			for (;;) {
				System.out.println("Elf " + name + " is working");
				try {
					Thread.sleep(2000);
				} catch (InterruptedException ie) {
					// Ignore
				}
				System.out.println("Elf " + name + " wants to ask a question");
				santa.ask(this);
				while (!santa_is_listening) {
					try {
						wait();
					} catch (InterruptedException ie) {
						// Ignore
					}
				}
				System.out.println("Elf " + name + " is in the study");
				while (santa_is_listening) {
					try {
						wait();
					} catch (InterruptedException ie) {
						// Ignore
					}
				}
			}
		}
	}

	public synchronized void showIn() {
		santa_is_listening = true;
		notifyAll();
	}

	public synchronized void showOut() {
		santa_is_listening = false;
		notifyAll();
	}
}
