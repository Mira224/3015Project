import java.io.IOException;


public class Controller {

    public static void main(String[] args) throws InterruptedException {

        int TCPport = 9999;
        int UDPport = 9998;

        Thread server = new Thread(() -> {
            try {
                new v3Server(TCPport, UDPport);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
		server.start();

        new v3Client();


    }

}