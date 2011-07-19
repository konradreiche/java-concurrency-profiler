package de.fu.profiler.examples;

public class Canteen {

	private int n_chickens = 0;

	public static void main(String argv[]) {

		int n_philosophers = 5;
		Canteen canteen = new Canteen();
		Phil[] phil = new Phil[n_philosophers];
		for (int i = 0; i < n_philosophers; i++) {
			phil[i] = new Phil(i, canteen);
		}
	}

	public synchronized int get(int id) {

		while (n_chickens == 0) {

			System.out.println("(" + System.currentTimeMillis() + ") "
					+ "Phil " + id + ": wot, no chickens?  I'll WAIT ... ");

			try {
				wait();
			} catch (InterruptedException e) {
			}
		}
		
		if (id == 0) {
			System.out.println("Finally, awesome!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		}
		
		System.out.println("(" + System.currentTimeMillis() + ") " + "Phil "
				+ id + ": those chickens look good ... one please ...");

		n_chickens--;
		return 1;
	}

	public synchronized void put(int value) {

		System.out.println("(" + System.currentTimeMillis() + ") "
				+ "Chef  : ouch ... make room ... this dish is very hot ... ");

		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
		}

		n_chickens += value;

		System.out.println("(" + System.currentTimeMillis() + ") "
				+ "Chef  : more chickens ... " + n_chickens
				+ " now available ... NOTIFYING ...");
		notifyAll();
	}
}

class Chef extends Thread {

	private Canteen canteen;

	public Chef(Canteen canteen) {
		this.canteen = canteen;
		start();
	}

	public void run() {

		int n_chickens;

		System.out.println("(" + System.currentTimeMillis() + ")"
				+ " Chef  : starting ... ");

		while (true) {

			System.out.println("(" + System.currentTimeMillis() + ")"
					+ " Chef  : cooking ... ");
			try {
				sleep(2000);
			} catch (InterruptedException e) {
			}

			n_chickens = 4;
			System.out.println("(" + System.currentTimeMillis() + ")"
					+ " Chef  : " + n_chickens + " chickens, ready-to-go ... ");

			canteen.put(n_chickens);
		}
	}
}

class Phil extends Thread {

	private int id;
	private Canteen canteen;

	public Phil(int id, Canteen canteen) {
		this.id = id;
		this.canteen = canteen;
		start();
	}

	public void run() {

		System.out.println("(" + System.currentTimeMillis() + ")" + " Phil "
				+ id + ": starting ... ");

		while (true) {
			if (id > 0) {

				try {
					sleep(3000);
				} catch (InterruptedException e) {
				}
			}

			System.out.println("(" + System.currentTimeMillis() + ")"
					+ " Phil " + id + ": gotta eat ... ");

			canteen.get(id);

			System.out.println("(" + System.currentTimeMillis() + ")"
					+ " Phil " + id + ": mmm ... that's good ... ");
		}
	}
}
