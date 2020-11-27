import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Controller {

	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		int TCPport = 9999;
		int UDPport = 9998;

		Thread t1 = new Thread(() -> {
//			new v3Client();

		});
		

		Thread t2 = new Thread(() -> {
			try {
//				while (true)
				new v3Server(TCPport, UDPport);
			} catch (IOException e) {
				e.printStackTrace();
			}

		});
//		t2.start();
//		t1.start();
		try {
//			while (true)
			new v3Server(TCPport, UDPport);
		} catch (IOException e) {
			e.printStackTrace();
		}

		new v3Client();
	}

}
