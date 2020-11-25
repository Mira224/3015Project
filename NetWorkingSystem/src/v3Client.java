import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

class Server {
	String srvName;
	String IP;
}

public class v3Client {
	static int TCPport = 9999;
	static int UDPport = 9998;
	DatagramSocket udpSocket;
	Socket clientSocket;
	String com = "";// which command the user require
	DataInputStream in;
	DataOutputStream out;
	ArrayList<Server> srvList = new ArrayList<Server>();

	public v3Client() {
		Thread t1 = new Thread(() -> {
			try {
				discovery(UDPport);
			} catch (IOException e) {
				e.printStackTrace();
			}

		});
		t1.start();

		Thread t2 = new Thread(() -> {
			try {
				while (true)
					new v3Client(srvList, TCPport);
			} catch (IOException e) {
				e.printStackTrace();
			}

		});
		t2.start();
	}

	public void discovery(int udpPort) throws IOException {
		udpSocket = new DatagramSocket(udpPort);
		byte[] str = "Online".getBytes();
		InetAddress destination = InetAddress.getByName("255.255.255.255");
		DatagramPacket packet = new DatagramPacket(str, str.length, destination, udpPort);
		udpSocket.send(packet);
		System.out.println("UP.");

		while (true) {

			System.out.println("In while.");
			DatagramPacket p = new DatagramPacket(new byte[1024], 1024);
			udpSocket.receive(p);

			byte[] data = p.getData();
			String srvInfo = new String(data, 0, p.getLength());
			if (!srvInfo.equals("Online")) {
				int size = p.getLength();
				String srcAddr = p.getAddress().toString();
				srcAddr = srcAddr.substring(1, srcAddr.length());
				Server s = new Server();
				s.srvName = srvInfo;
				s.IP = srcAddr;

				srvList.add(0, s);
			}
		}

	}

	public v3Client(ArrayList<Server> srvList2, int port) throws IOException {
		if (srvList2.size() > 0) {
			boolean opCorrect = false;
			int option = 0;
			for (int i = 0; i < srvList2.size(); i++) {
				System.out.println(i + " " + srvList2.get(i).IP + " " + srvList2.get(i).srvName);
			}

			while (!opCorrect) {
				System.out.println("Which server do you want to join?");
				Scanner srvOption = new Scanner(System.in);
				option = srvOption.nextInt();
				if (option < srvList2.size() && option >= 0)
					opCorrect = true;

			}
			if (opCorrect) {
				clientSocket = new Socket(srvList2.get(option).IP, port);
				in = new DataInputStream(clientSocket.getInputStream());
				out = new DataOutputStream(clientSocket.getOutputStream());

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
			}
		}
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
						int length = in.readInt();

						System.out.println(length);
						while (length > 0) {
							int blen = in.read(buffer, 0, buffer.length);

							String ser = new String(buffer, 0, blen);

							System.out.println(ser);

							length -= blen;
						}
					} else if (com[0].equalsIgnoreCase("download")) {

						download(cmd);

					} else {

						out.writeInt(cmd.length());
						out.write(cmd.getBytes(), 0, cmd.length());
						int length = in.readInt();

						System.out.println(length);
						while (length > 0) {
							int blen = in.read(buffer, 0, buffer.length);

							String ser = new String(buffer, 0, blen);

							System.out.println(ser);

							length -= blen;

						}

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
		int length = in.readInt();
		in.read(buffer, 0, length);

		System.out.println(length);
		if (length == 0) {
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
				}
				System.out.println("Client download completed.");

			} catch (IOException e) {
				System.err.println("unable to download file.");
			}
		} else {

			String ser = new String(buffer, 0, length);

			System.out.println(ser);

		}

	}

	public void end() {
		udpSocket.close();
		System.out.println("bye-bye");
	}

	public static void main(String[] args) throws IOException {

		v3Client c = new v3Client();

//		c.end();
	}

}