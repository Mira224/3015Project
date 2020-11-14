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
		System.out.print("Please input the IP address of Echo Server: ");
		String server = scanner.nextLine();

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

			String str = "0 " + username + " " + password;
			out.writeInt(str.length());
			out.write(str.getBytes(), 0, str.length());

			int len = in.readInt();
			in.read(buffer, 0, len);
			String serMsg = new String(buffer, 0, len);
			System.out.println(serMsg);
			if (serMsg.equals("Login success")) {
				login = true;
				do {
					System.out.println("Please input your option:");
					System.out.println("1. Read file list.");
					System.out.println("2. Create subdirectories");
					System.out.println("3. Upload files.");
					System.out.println("4. Download files.");
					System.out.println("5. Delete files.");
					System.out.println("6. Delete subdirectories.");
					System.out.println("7. Change file/target name.");
					System.out.println("8. Read the file¡¯s detail information");
					System.out.println("9. logout");
					System.out.println("10. Exit");
					String optionNo = scanner.nextLine();
					switch (optionNo) {
					case "1":
						System.out.println("Please input a directory:");
						String msg = scanner.nextLine();
						String req = optionNo + " " + msg;
						out.writeInt(req.length());
						out.write(req.getBytes(), 0, req.length());

						int length = in.readInt();
						in.read(buffer, 0, length);
						String ser = new String(buffer, 0, length);
						System.out.println(ser);
						break;
					case "9":
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

	/*
	 * System.out.println("Please input your name: "); Scanner scanName = new
	 * Scanner(System.in); String str1 = scanName.nextLine();
	 * 
	 * System.out.println("Please input messages:");
	 * 
	 * 
	 * while (true) { Scanner scanMsg = new Scanner(System.in); String str2 =
	 * scanMsg.nextLine(); String str = str1 + ": " + str2;
	 * out.writeInt(str.length()); out.write(str.getBytes(), 0, str.length()); }
	 */

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
