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

		clientSocket = new Socket(serverIP, port);
		in = new DataInputStream(clientSocket.getInputStream());
		out = new DataOutputStream(clientSocket.getOutputStream());

		Thread t = new Thread(() -> {

			boolean login = false;
			boolean exit = false;
			try {
				while (true) {

					appStart(login, exit);
				}

			} catch (IOException ex) {
				System.err.println("Connection dropped!");
				System.exit(-1);
			}
		});
		t.start();

		// out.close();
		// clientSocket.close();

	}

	public void appStart(boolean login, boolean exit) throws IOException {
		Scanner scanner = new Scanner(System.in);
		do {
			byte[] buffer = new byte[1024];
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
					Scanner newScanner = new Scanner(System.in);
					String cmd = newScanner.nextLine();
					String[] com = cmd.split("\\s+");
					if (com[0].equalsIgnoreCase("logout")) {
						login = false;
						out.writeInt(cmd.length());
						out.write(cmd.getBytes(), 0, cmd.length());
						System.out.println();
						int l = in.readInt();
						in.read(buffer, 0, l);
						String sMsg = new String(buffer, 0, l);
						System.out.println(sMsg);
						break;
					} else if (com[0].equalsIgnoreCase("upload")) {// command
						upload(cmd);

					} else if (com[0].equalsIgnoreCase("download")) {

						download(cmd);

					} else {

						out.writeInt(cmd.length());
						out.write(cmd.getBytes(), 0, cmd.length());

					}
		
					int length = in.readInt();
					System.out.println("Test2");
					while (length > 0) {
						int blen = in.read(buffer, 0, buffer.length);
						System.out.println("Test3");
						String ser = new String(buffer, 0, blen);

						System.out.println(ser);
						System.out.println("abc");
						length -= blen;
					}

				} while (login);
			}

		} while (!login);
		scanner.close();
	}

	public void upload(String cmd) throws IOException {
		Scanner upload = new Scanner(System.in);
		System.out.print("Please input the path and filename of the file you want to upload:");
		String uploadfile = upload.nextLine();

		boolean fileExist = false;

		do {
			File fileToUpload = new File(uploadfile);
			if (fileToUpload.exists()) {
				if (fileToUpload.isDirectory()) {
					System.out.print("This is a directory.");

				} else {
					fileExist = true;
					break;
				}

			} else {
				System.out.println(uploadfile + " does not exist.");
			}
			System.out.print("Please input the path and filename of the file you want to upload:");
			uploadfile = upload.nextLine();

		} while (!fileExist);
		cmd += " " + uploadfile;
		System.out.print("Please input where to upload with filename:");
		String uploadpath = upload.nextLine();
		cmd += " " + uploadpath;
		out.writeInt(cmd.length());
		out.write(cmd.getBytes(), 0, cmd.length());

		String[] com = cmd.split("\\s+");
		try {
			byte[] buffer = new byte[1024];
			File file = new File(com[1]);
			FileInputStream inFile = new FileInputStream(file);

			long size = file.length();
			out.writeLong(size);

			while (size > 0) {
				int len = inFile.read(buffer, 0, buffer.length);
				out.write(buffer, 0, len);
				size -= len;
			}

			System.out.println("Client Transmission finished.");

		} catch (IOException e) {
			System.err.println("Transmission error.");
		}
	}

	public void download(String cmd) throws IOException {
		Scanner download = new Scanner(System.in);
		System.out.print("Please input the file you wish to download (path and filename):");
		String downloadfile = download.nextLine();
		cmd = "download " + downloadfile;
		boolean downloadPathExist = false;
		String downloadpath = "";
		do {
			System.out.print("Please input where do you wish to download the file(path ended with'/'):");
			downloadpath = download.nextLine();
			File downloadp = new File(downloadpath);

			if (downloadp.exists()) {
				if (downloadp.isDirectory()) {
					downloadPathExist = true;

				} else {
					System.out.println("This is not a directory.");
				}
			} else {
				System.out.println(downloadpath + " does not exist.");
			}
		} while (!downloadPathExist);

		out.writeInt(cmd.length());
		out.write(cmd.getBytes(), 0, cmd.length());
		byte[] buffer = new byte[1024];
		try {
			DataInputStream in = new DataInputStream(clientSocket.getInputStream());
			int nameLen = in.readInt();
			in.read(buffer, 0, nameLen);
			String name = new String(buffer, 0, nameLen);

			File newfile = new File(downloadpath + name);
			long size = in.readLong();

			FileOutputStream output = new FileOutputStream(newfile);
			while (size > 0) {
				int len = in.read(buffer, 0, buffer.length);
				output.write(buffer, 0, len);
				size -= len;
				// System.out.print(size+" ");
			}
			System.out.println("\n Client download completed.");

		} catch (IOException e) {
			System.err.println("unable to download file.");
		}
	}

	public static void main(String[] args) throws IOException {
		// NetWorkingClient client = new NetWorkingClient("", 9999);
		// client.setup();
		// new NetWorkingClient("192.168.31.238", TCPport);
		// new NetWorkingClient("158.182.12.206", TCPport);
		new NetWorkingClient("192.168.31.199", TCPport);
	}

}
