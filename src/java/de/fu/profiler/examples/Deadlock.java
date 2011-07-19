package de.fu.profiler.examples;

public class Deadlock {

	public static void transferMoney(Account fromAccount, Account toAccount, int amount) throws InsufficientFundsException, InterruptedException {
		
		synchronized (fromAccount) {
			synchronized (toAccount) {
				if (fromAccount.getBalance().compareTo(amount) < 0) {
					throw new InsufficientFundsException();
				} else {
					fromAccount.debit(amount);
					toAccount.credit(amount);
				}
			}
		}
	}
	
	public static void main(String... args) {
		
		final Account x = new Account();
		final Account y = new Account();
		
		x.credit(1000);
		y.credit(1000);
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					transferMoney(x, y, 250);
				} catch (InsufficientFundsException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		
			}
		}).start();
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					transferMoney(y, x, 250);
				} catch (InsufficientFundsException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		
			}
		}).start();
		
	}
}
