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


		Thread t = new Thread(() ->{
			byte[] buffer = new byte[1024];
			boolean login = false;
			boolean exit = false;
			try {
				while (true) {

					appStart(buffer,login,exit);
				}
				
				
			}catch(IOException ex) {
				System.err.println("Connection dropped!");
				System.exit(-1);
			}
		});
		t.start();

		

//		out.close();
//		clientSocket.close();
		

	}

	public void appStart(byte[] buffer,boolean login, boolean exit) throws IOException {
		Scanner scanner = new Scanner(System.in);
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
					
						String cmd = scanner.nextLine();
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
						} else if (com[0].equalsIgnoreCase("upload")) {//command 
							Scanner upload = new Scanner(System.in);
							System.out.print("Please input the upload path and filename:");
							String uploadpath = upload.nextLine();
							cmd = com[0] + " " + com[1] + " " + uploadpath;
							out.writeInt(cmd.length());
							out.write(cmd.getBytes(), 0, cmd.length());
							upload(com[1]);
							upload.close();
							break;
						} else if (com[0].equalsIgnoreCase("download")) {
							Scanner download = new Scanner(System.in);
							System.out.print("Please input which directory to download:");
							String downloadpath = download.nextLine();
							out.writeInt(cmd.length());
							out.write(cmd.getBytes(), 0, cmd.length());
							download(downloadpath);
							break;
						} else {

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
		scanner.close();
	}
	
	public void upload(String path) {

		try {
			byte[] buffer = new byte[1024];
			File file = new File(path);
			FileInputStream inFile = new FileInputStream(file);

			long size = file.length();
			out.writeLong(size);

			while (in.available() > 0) {
				int len = inFile.read(buffer, 0, buffer.length);
				out.write(buffer, 0, len);
			}

			System.out.println("Client Tranmission finished.");
		} catch (IOException e) {
			System.err.println("Transmission error.");
		}
	}

	public void download(String downloadpath) throws IOException {
		byte[] buffer = new byte[1024];
		try {
			int nameLen = in.readInt();
			in.read(buffer, 0, nameLen);
			String name = new String(buffer, 0, nameLen);
			String apath = downloadpath +"/"+ name;
			File newfile = new File(apath);
			long size = in.readLong();
			
			FileOutputStream output = new FileOutputStream(newfile);
			while (size > 0) {
				int len = in.read(buffer, 0, buffer.length);
				output.write(buffer, 0, len);
				size -= len;
//				System.out.print(size+" ");
			}
			System.out.println("\n Client download completed.");

//			in.close();
//			output.close();
		} catch (IOException e) {
			System.err.println("unable to download file.");
		}
	}

	public static void main(String[] args) throws IOException {
		// NetWorkingClient client = new NetWorkingClient("", 9999);
		// client.setup();
		new NetWorkingClient("192.168.31.238", TCPport);
//		new NetWorkingClient("158.182.8.145", TCPport);
//		new NetWorkingClient("192.168.31.199", TCPport);
	}

}
