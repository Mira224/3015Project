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
					System.out.println("Please input your command>");

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
						System.out.println("Please input the upload directory:");
						String uploadpath = upload.nextLine();
						cmd = cmd + " " + uploadpath;
						out.writeInt(cmd.length());
						out.write(cmd.getBytes(), 0, cmd.length());
						upload(com[1]);
						upload.close();

					} else if(com[0].equalsIgnoreCase("download")){
				
						out.writeInt(cmd.length());
						out.write(cmd.getBytes(), 0, cmd.length());
						download();
					}else {
					
						out.writeInt(cmd.length());
						out.write(cmd.getBytes(), 0, cmd.length());

					}

					int length = in.readInt();
					in.read(buffer, 0, length);
					String ser = new String(buffer, 0, length);
					System.out.println(ser);
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

			out.writeInt(file.getName().length());
			out.write(file.getName().getBytes());
			long size = file.length();
			out.writeLong(size);

			while (inFile.available() > 0) {
				int len = in.read(buffer, 0, buffer.length);
				out.write(buffer, 0, len);
			}
			inFile.close();
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
		while(size > 0) {
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
