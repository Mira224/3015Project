import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
		DataInputStream in;
		DataOutputStream out;

		while (whetherExit == false) {
			System.out.printf("Listening at port %d...\n", port);

			Socket clientSocket = srvSocket.accept();

			System.out.printf("Established a connection to host %s:%d\n\n", clientSocket.getInetAddress(),
					clientSocket.getPort());

			in = new DataInputStream(clientSocket.getInputStream());
			out = new DataOutputStream(clientSocket.getOutputStream());
			int len = in.readInt();
			in.read(buffer, 0, len);

			String command = new String(buffer, 0, len);
			String[] com = command.split("\\s");
			String reMsg = "";

			switch (com[0]) {
			// login
			case "login":
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
			case "dir":
				reMsg = readFile(com[1]);
				break;
			// create sub-directories
			case "md":
				reMsg = createDir(com[1]);

				// upload files
			case "upload":
				try {
					System.out.print("Downloading file %s " + com[2]);
					long size = in.readLong();
					System.out.printf("(%d)", size);

					File file = new File(com[1]);
					FileOutputStream output = new FileOutputStream(file);

					while(size > 0) {
						int read = in.read(buffer, 0, buffer.length);
						output.write(buffer, 0, read);
						size -= read;
					}
					System.out.print("\nDownload completed.");
					reMsg="Upload completed.";
					in.close();
					output.close();
				} catch (IOException e) {
					System.err.println("Unable to download the file.");
					reMsg="Unable to upload file.";
				}
				break;
				
				// download files
			case "download":
				try {
					System.out.print("Uploading file %s " + com[1]);
					File file = new File(com[1]);
					FileInputStream input = new FileInputStream(file);
					out.writeInt(file.getName().length());
					out.write(file.getName().getBytes());
					long size = file.length();
					out.writeLong(size);
					while (size > 0) {
						int read = input.read(buffer, 0, buffer.length);
						out.write(buffer, 0, read);
						size -= read;
					}
					input.close();
					out.close();
					System.out.println("Finishied!");
				} catch (IOException e) {
					System.err.println("Unable to upload file.");
					reMsg="Unable to download the file.";
				}
				break;
				
			// delete file
			case "del":
				reMsg = delFile(com[1]);
				break;
			// delete sub-directory
			case "rd":
				reMsg = delDir(com[1]);
				break;
			// change file/target name
			case "ren":
				reMsg = rename(com[1], com[2]);
				break;
			// read files details
			case "readDe":
				reMsg=shwDetail(com[1]);
				break;
			// logout
			case "logout":
				whetherLogin = false;
				break;
			// exit
			case "exit":
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
	public boolean Login(String userinfo) throws IOException {
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
	public  String readFile(String path) {
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
	public  String createDir(String path) {
		File create = new File(path);
		if (create.exists() && create.isDirectory()) {
			return "The Directory Exists!";
		}
		new File(path).mkdirs();

		return "Created Successfully";
	}
	
	// function delete file
	public String delFile(String path) {
		File delf = new File(path);
		if (delf.exists()) {
			if (delf.isDirectory()) {
				return "To delete a directory, use RD command.";
			}
			new File(path).delete();
			return "Successful delete" + path;
		}
		return "Cannot Find File " + path;
	}

	// function delete sub-directory
	public String delDir(String path) {
		File dir = new File(path);
		if (dir.exists()) {
			if (dir.isFile()) {
				return "To delete a directory, use DEL command.";
			}
			File[] f = dir.listFiles();
			if (f.length != 0) {
				return "The Directory " + path + " Is Not Empty!";
			}
			new File(path).delete();
			return "Successful delete" + path;
		}
		return "Cannot Find Directory \"+path";

	}
	// function rename file
	public String rename(String path, String filename) throws IOException {
		String reMsg = "Successfully renamed.";
		File dir = new File(path);
		String newFileName = dir.getParent() + filename;
		File newFile = new File(newFileName);
		if (dir.isDirectory()) {
			reMsg = "This is not a file";
		} else if (dir.isFile()) {
			if (!dir.exists()) {
				reMsg = "File Not Fiound";

			} else {
				boolean re = dir.renameTo(newFile);
				if (!re) {
					reMsg = "Fail.";
				}
			}
		}

		return reMsg;
	}

	// function show file details
	public String shwDetail(String path) throws IOException {
		File show=new File(path);
		String str1="name : " + show.getName()+"\n";
		String str2="size (bytes) : " + show.length()+"\n";
		String str3="absolute path? : " + show.isAbsolute()+"\n";
		String str4="exists? : " + show.exists()+"\n";
		String str5="hidden? : " + show.isHidden()+"\n";
		String str6="dir? : " + show.isDirectory()+"\n";
		String str7="file? : " + show.isFile()+"\n";
		String str8="modified (timestamp) : " + show.lastModified()+"\n";
		String str9="readable? : " + show.canRead()+"\n";
		String strA="writable? : " + show.canWrite()+"\n";
		String strB="executable? : " + show.canExecute()+"\n";
		String strC="parent : " + show.getParent()+"\n";
		String strD="absolute file : " + show.getAbsoluteFile()+"\n";
		String strE="absolute path : " + show.getAbsolutePath()+"\n";
		String strF="canonical file : " + show.getCanonicalFile()+"\n";
		String strG="canonical path : " + show.getCanonicalPath()+"\n";
		String strH="partition space (bytes) : " + show.getTotalSpace()+"\n";
		String strI="usable space (bytes) : " + show.getUsableSpace()+"\n";
		
		return str1+str2+str3+str4+str5+str6+str7+str8+str9+strA+strB+strC+strD+strE+strF+strG+strH+strI;
	}

	public static void main(String[] args) throws IOException {
		int tcpPort=9999;
		int udpPort=9998;
		try {
			new NetWorkingServer(tcpPort);
		} catch (IOException e) {
			System.err.println("Failed to connect port");
		}
	}
}
