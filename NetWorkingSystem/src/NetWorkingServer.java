import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Scanner;
//import java.io.FileInputStream;
//import java.io.FileOutputStream;

public class NetWorkingServer {
	ServerSocket srvSocket;

	public NetWorkingServer(int port) throws IOException {

		// receiving socket from client
		srvSocket = new ServerSocket(port);
		byte[] buffer = new byte[1024];
		boolean whetherLogin = false;
		boolean whetherExit = false;
		
			
		while(true) {
			System.out.printf("Listening at port %d...\n", port);

			Socket clientSocket = srvSocket.accept();

			System.out.printf("Established a connection to host %s:%d\n\n", clientSocket.getInetAddress(),clientSocket.getPort());

			DataInputStream in = new DataInputStream(clientSocket.getInputStream());
			DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
			int len = in.readInt();
			in.read(buffer, 0, len);

			String command = new String(buffer, 0, len);
			String[] com = command.split("\\s");
			String reMsg = "";

			switch (com[0]) {
			// login
			case "0":
				if (whetherLogin == false) {
					if (Login(com[1] + " " + com[2])) {
						reMsg = "Login success";
						whetherLogin = true;
					} else {
						reMsg = "Login failed";
					}
				} else {
					reMsg = "Already Logged in";
				}
				break;
			// read file list
			case "1":
				reMsg = readFile(com[1]);
				break;
			// create sub-directories
			case "2":
				reMsg = createDir(com[1]);
				
				// upload files
			case "3":
				reMsg=uploadFile(com[1]);
				//download  files
			case "4":
				
			case "9":
				whetherLogin = false;
				break;
			// exit
			case "10":
				whetherExit = true;
				whetherLogin = false;
				break;
			default:
				reMsg = "Invalid command";
			}

			out.writeInt(reMsg.length());
			out.write(reMsg.getBytes(), 0, reMsg.length());
			clientSocket.close();
			
		} 
	}

	// login function
	public static boolean Login(String userinfo) throws IOException {
		File user = new File("userinfo.txt");
		Scanner u = new Scanner(user);
		while (u.hasNext()) {
			if (u.nextLine().equals(userinfo)) {
				u.close();
				return true;
			}
		}
		u.close();
		return false;
	}

	// function read files
	public static String readFile(String path) {
		File read = new File(path);
		if (!read.exists()) {
			return "Not Find The File.";
		}
		String reMsg = "";
		File[] files = read.listFiles();
		for (File f : files) {
			if (!f.isFile()) {
				String forFile = String.format("%s %10d %s/n", new Date(f.lastModified()), f.length(), f.getName());
				reMsg += forFile;
				String forDir = String.format("%s %10s %s/n", new Date(f.lastModified()), "<DIR>", f.getName());
				reMsg += forDir;
			}
		}
		return reMsg;
	}

	// function create sub-directories
	public static String createDir(String path) {
		File file = new File(path);
		if (file.exists() && file.isDirectory()) {
			return "The Directory Exists!";
		}
		new File(path).mkdirs();
		
		return "Created Successfully";
	}

	//function upload files
	public static String uploadFile(String path) {
		
		return "Successfully Upload";
	}
	
	
	
	
	
	
	
	public static void main(String[] args) throws IOException {
		new NetWorkingServer(9999);
	}
}
