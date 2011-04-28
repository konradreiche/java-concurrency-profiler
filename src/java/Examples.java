
public class Examples {

	public static void main(String[] args) throws InterruptedException {

		Drop drop = new Drop();
		(new Thread(new Producer(drop))).start();
		(new Thread(new Consumer(drop))).start();

	}
}
