import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends ServerSocket implements Runnable {

	public Server(int port) throws IOException {
		super(port);
	}

	@Override
	public void run() {
		try {
			while (true) {
				Socket socket = this.accept();
				Thread service = new Thread(new ConnectionHandler(socket));
				service.start();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String args[]) throws IOException {

		new Thread(new Server(50000)).start();
	}

}
