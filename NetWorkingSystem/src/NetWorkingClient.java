import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class NetWorkingClient {
	static int TCPport = 9999;
	Socket clientSocket;
	String com = "";// which command the user require

	public NetWorkingClient(String serverIP, int port) throws IOException {
		Scanner scanner = new Scanner(System.in);

		clientSocket = new Socket(serverIP, port);
		DataInputStream in = new DataInputStream(clientSocket.getInputStream());
		DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

		byte[] buffer = new byte[1024];
		boolean login = false;
		boolean exit = false;

		do {
			System.out.print("Please input your username: ");
			String username = scanner.nextLine();
			System.out.print("Please input password: ");
			String password = scanner.nextLine();

			String str = "login " + username + " " + password;
			out.writeInt(str.length());
			out.write(str.getBytes(), 0, str.length());

			int len = in.readInt();
			in.read(buffer, 0, len);
			String serMsg = new String(buffer, 0, len);
			System.out.println(serMsg);
			if (serMsg.equals("Login success")) {
				login = true;
				do {
					System.out.println("Please input your command>");

					String cmd = scanner.nextLine();
					out.writeInt(cmd.length());
					out.write(cmd.getBytes(), 0, cmd.length());

					int length = in.readInt();
					in.read(buffer, 0, length);
					String ser = new String(buffer, 0, length);
					System.out.println(ser);
					if (cmd.equalsIgnoreCase("logout")) {
						login = false;
						System.out.println("See you~");
						break;
					}

				} while (login);
			}

		} while (!login);

		clientSocket.close();
		scanner.close();

	}

	public void login() {

	}



	public void readFileList() {
		Scanner scanner = new Scanner(System.in);
		System.out.print("File: ");
		String filename = scanner.nextLine().trim();

	}

	public static void main(String[] args) throws IOException {
		// NetWorkingClient client = new NetWorkingClient("", 9999);
		// client.setup();
		new NetWorkingClient("192.168.31.238", TCPport);
	}

}
