import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class NetWorkingClient {
	static int TCPport = 9999;

	public NetWorkingClient(String serverIP, int port) throws IOException {
		Scanner scanner = new Scanner(System.in);
		System.out.print("Please input the IP address of Echo Server: ");
		String server = scanner.nextLine();

		Socket clientSocket = new Socket(serverIP, port);
		DataInputStream in = new DataInputStream(clientSocket.getInputStream());
		DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

		byte[] buffer = new byte[1024];

		System.out.print("Please input your username: ");
		String username = scanner.nextLine();
		System.out.print("Please input password: ");
		String password = scanner.nextLine();

		String str = "0 "+username + " " + password;
		out.writeInt(str.length());
		out.write(str.getBytes(), 0, str.length());

		int len = in.readInt();
		in.read(buffer, 0, len);
		System.out.println(new String(buffer, 0, len));
		clientSocket.close();
		scanner.close();
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
//
//	public void setup() {
//		Scanner scanner = new Scanner(System.in);
//		System.out.println("Please input\nServer IP: ");
//		String ip = scanner.nextLine().trim();
//		System.out.print("Port no: ");
//		int port = Integer.parseInt(scanner.nextLine());
//		boolean run = true;
//		while (run) {
//			System.out.println("Please select the option: ");
//			System.out.println("1. Read file list.");
//			System.out.println("2. Create subdirectories");
//			System.out.println("3. Upload and download files.");
//			System.out.println("4. Delete files.");
//			System.out.println("5. Delete subdirectories.");
//			System.out.println("6. Change file/target name.");
//			System.out.println("7. Read the file¡¯s detail information");
//			System.out.println("8. Exit");
//			String option = scanner.nextLine().trim();
//			switch (option) {
//			case "1":
//				readFileList();
//				break;
//			// case "2":
//			// createSubdirectories();
//			// break;
//			// case "3":
//			// UploadAndDownloadFiles();
//			// break;
//			// case "4":
//			// deleteFiles();
//			// break;
//			// case "5":
//			// deleteSubdirectories();
//			// break;
//			// case "6":
//			// changeName();
//			// break;
//			// case "7":
//			// readInfor();
//			// break;
//			case "8":
//				run = false;
//				break;
//			default:
//				break;
//
//			}
//			scanner.close();
//		}
//
//	}

	public static void main(String[] args) throws IOException {
//		NetWorkingClient client = new NetWorkingClient("", 9999);
//		client.setup();
		new NetWorkingClient("192.168.31.238",TCPport);
	}

}
