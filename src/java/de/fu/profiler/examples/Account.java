package de.fu.profiler.examples;

public class Account {

	int balance;

	public void debit(int amount) {
		balance -= amount;
	}
	
	public void credit(int amount) {
		balance += amount;
	}
	
	public Integer getBalance() {
		return balance;
	}
}
