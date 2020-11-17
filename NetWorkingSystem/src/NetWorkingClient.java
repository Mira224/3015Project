import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class NetWorkingClient {
	static int TCPport = 9999;
	Socket clientSocket;
	String com = "";// which command the user require
	DataInputStream in;
	DataOutputStream out;

	public NetWorkingClient(String serverIP, int port) throws IOException {
		Scanner scanner = new Scanner(System.in);

		clientSocket = new Socket(serverIP, port);
		in = new DataInputStream(clientSocket.getInputStream());
		out = new DataOutputStream(clientSocket.getOutputStream());

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
					System.out.print("Please input your command>");
					while (scanner.hasNextLine()) {
						String cmd = scanner.nextLine();
						String[] com = cmd.split("\\s+");
						if (com[0].equalsIgnoreCase("logout")) {
							login = false;
							out.writeInt(cmd.length());
							out.write(cmd.getBytes(), 0, cmd.length());
							System.out.println("See you~");

							break;
						} else if (com[0].equalsIgnoreCase("upload")) {
							Scanner upload = new Scanner(System.in);
							System.out.println("Please input which directory to upload:");
							String uploadpath = upload.nextLine();
							cmd = com[0] + " " + com[1] + " " + uploadpath;
							out.writeInt(cmd.length());
							out.write(cmd.getBytes(), 0, cmd.length());
							upload(com[1]);
							upload.close();
							break;
						} else if (com[0].equalsIgnoreCase("download")) {

							out.writeLong(cmd.length());
							out.write(cmd.getBytes(), 0, cmd.length());
							download();
							break;
						} else {

							out.writeInt(cmd.length());
							out.write(cmd.getBytes(), 0, cmd.length());

						}

						int length = in.readInt();
						in.read(buffer, 0, length);
						String ser = new String(buffer, 0, length);
						System.out.println(ser);
					}
				} while (login);
			}

		} while (!login);

		out.close();
		clientSocket.close();
		scanner.close();

	}

	public void upload(String path) {

		try {
			byte[] buffer = new byte[1024];
			File file = new File(path);
			FileInputStream inFile = new FileInputStream(file);

			long size = file.length();
			out.writeLong(size);

			while (inFile.available() > 0) {
				int len = inFile.read(buffer, 0, buffer.length);
				out.write(buffer, 0, len);
			}

			System.out.println("Tranmission finished.");
		} catch (IOException e) {
			System.err.println("Transmission error.");
		}
	}

	public void download() throws IOException {
		byte[] buffer = new byte[1024];
		try {
			int nameLen = in.readInt();
			in.read(buffer, 0, nameLen);
			String name = new String(buffer, 0, nameLen);
			long size = in.readLong();
			File newfile = new File(name);
			FileOutputStream output = new FileOutputStream(newfile);
			while (size > 0) {
				int len = in.read(buffer, 0, buffer.length);
				output.write(buffer, 0, len);
				size -= len;
				System.out.print(".");
			}
			System.out.println("\nDownload completed.");

			in.close();
			output.close();
		} catch (IOException e) {
			System.err.println("unable to download file.");
		}
	}

	public static void main(String[] args) throws IOException {
		// NetWorkingClient client = new NetWorkingClient("", 9999);
		// client.setup();
		new NetWorkingClient("192.168.31.238", TCPport);
	}

}
