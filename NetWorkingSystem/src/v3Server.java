import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class v3Server {
    ServerSocket srvSocket;
    DatagramSocket udpSocket;
    ArrayList<Socket> list = new ArrayList<Socket>();

    //Open threads to listen to udp and tcp at the same time
    public v3Server(int tcpPort, int udpPort) throws IOException {
        // receiving socket from client
        Thread t1 = new Thread(() -> {
            try {
                udpServer(udpPort);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        t1.start();
        Thread t2 = new Thread(() -> {
            try {
                tcpServer(tcpPort);
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
        t2.start();
    }

    public void udpServer(int udpPort) throws IOException {
        udpSocket = new DatagramSocket(udpPort);// udp
        System.out.println("Listening at udpPort...9998");
        while (true) {
//After receiving the message from the client, send reply
            DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
            udpSocket.receive(packet);
            byte[] str = "Server A".getBytes();
            InetAddress clientDest = packet.getAddress();

            DatagramPacket srvPacket = new DatagramPacket(str, str.length, clientDest, 12346);
            udpSocket.send(srvPacket);
        }
    }

 //TCP server
    public void tcpServer(int tcpPort) throws IOException {
        srvSocket = new ServerSocket(tcpPort);// tcp
        while (true) {

            // TCP Connection
            System.out.printf("Listening at port %d...\n", tcpPort);
            Socket clientSocket = srvSocket.accept();

            synchronized (list) {
                list.add(clientSocket);
                System.out.printf("Total %d clients are connected.\n", list.size());
            }

            Thread t = new Thread(() -> {
                try {
                    serve(clientSocket);
                } catch (IOException e) {
                    System.err.println("Server: connection dropped.");
                }
                synchronized (list) {
                    list.remove(clientSocket);
                }
            });
            t.start();
        }

    }

    //The controller of functions for commands received from TCP
    public void serve(Socket clientSocket) throws IOException {
        boolean whetherLogin = false;
        boolean whetherExit = false;

        System.out.printf("Established a connection to host %s:%d\n\n", clientSocket.getInetAddress(),
                clientSocket.getPort());

        while (!whetherExit) {
            DataInputStream in = new DataInputStream(clientSocket.getInputStream());
            DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
            int len = in.readInt();
            byte[] buffer = new byte[len];
            in.read(buffer, 0, len);

            String command = new String(buffer);

            String[] com = command.split("\\s");
            String reMsg = "";

            switch (com[0]) {
                // login (once only)
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
                    break;
                // upload files
                case "upload":
                    try {
                        long size = in.readLong();
                        File file = new File(com[2]);
                        String check = file.getParent();

                        File chkPath = new File(check);
                        if (chkPath.exists() && chkPath.isDirectory()) {
                            if (file.exists()) {//Empty the input stream
                                while (size > 0) {
                                    int re = in.read(buffer, 0, buffer.length);
                                    size -= re;
                                }
                                reMsg = "File/Directory " + com[2] + " exists. Please change the file name.";
                            } else {
                                System.out.println("Downloading file " + com[2]);
                                FileOutputStream output = new FileOutputStream(file);
                                while (size > 0) {
                                    int re = in.read(buffer, 0, buffer.length);
                                    output.write(buffer, 0, re);
                                    size -= re;
                                }
//                                System.out.printf("\nDownload completed." + "\n");
                                reMsg = "Upload completed.";
                            }
                        }else{
                            while (size > 0) {
                                int re = in.read(buffer, 0, buffer.length);
                                size -= re;
                            }
                            reMsg="Path incorrect. Please check the path.";
                        }
                    } catch (IOException e) {
                        System.err.println("Unable to download the file.");
                        reMsg = "Unable to upload file.";
                    }
                    break;

                // download files
                case "download":
                    try {
                        File file = new File(com[1]);
                        if (file.exists()) {
                            if (file.isFile()) {
                                reMsg = "";
                                out.writeInt(reMsg.length());
                                System.out.println("Uploading file " + com[1]);
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
  //                              System.out.println("Success upload.");

                            } else {
                                reMsg = com[1] + "is not a file.";
                            }
                        } else {
                            reMsg = com[1] + " not exist.";
                        }
                    } catch (IOException e) {
                        System.err.println("Unable to upload file.");
                        reMsg = "Unable to download the file.";
                    }
                    break;

                // delete file fine
                case "del":
                    reMsg = delFile(com[1]);
                    break;
                // delete sub-directory fine
                case "rd":
                    reMsg = delDir(com[1]);
                    break;
                // change file/target name fine
                case "ren":
                    reMsg = rename(com[1], com[2]);
                    break;
                // read files details fine
                case "readDe":
                    reMsg = shwDetail(com[1]);
                    break;
                // logout
                case "logout":
                    reMsg = "Good bye.";
                    whetherLogin = false;
                    break;
                // exit
                case "exit":
                    reMsg = "Bye~";
                    whetherExit = true;
                    whetherLogin = false;
                    break;
                default:
                    reMsg = "Invalid command";
            }

            out.writeInt(reMsg.length());
            out.write(reMsg.getBytes(), 0, reMsg.length());

        }
    }

    // login function
    public boolean Login(String userinfo) throws IOException {
        File user = new File("userinfo.txt");
        Scanner u = new Scanner(user);
        while (u.hasNext()) {
            if (u.nextLine().equals(userinfo)) {
                // u.close();
                return true;
            }
        }
        // u.close();
        return false;
    }

    // function read files fine
    public String readFile(String path) {
        File read = new File(path);
        if (!read.exists()) {
            return "Not Find The File.";
        }
        String reMsg = "";
        File[] files = read.listFiles();
        for (File f : files) {
            if (!f.isFile()) {
                String forDir = String.format("%s %10s %s\n", new Date(f.lastModified()), "<DIR>", f.getName());
                reMsg += forDir;
            } else {
                String forFile = String.format("%s %10d %s\n", new Date(f.lastModified()), f.length(), f.getName());
                reMsg += forFile;
            }
        }
        return reMsg;
    }

    // function create sub-directories fine
    public String createDir(String path) {
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
                return "To delete a file, use DEL command.";
            }
            File[] f = dir.listFiles();
            if (f.length != 0) {
                return "The Directory " + path + " Is Not Empty!";
            }
            new File(path).delete();
            return "Successful delete" + path;
        }
        return "Cannot Find Directory " + path + "\n";

    }

    // function rename file
    public String rename(String path, String filename) throws IOException {
        File rename = new File(path);
        String newFileName = rename.getParent() + "/" + filename;
        File newFile = new File(newFileName);

        if (rename.exists()) {
            if (rename.isDirectory()) {
                return "This is not a file";
            } else if (rename.isFile()) {
                boolean re = rename.renameTo(newFile);
                if (!re) {
                    return "Fail to rename file " + path;
                } else {
                    return "Rename compeleted.";
                }
            }
        }

        return "File " + path + " is not exist.";
    }

    // function show file details
    public String shwDetail(String path) throws IOException {
        File show = new File(path);
        String str1 = "name : " + show.getName() + "\n";
        String str2 = "size (bytes) : " + show.length() + "\n";
        String str3 = "absolute path? : " + show.isAbsolute() + "\n";
        String str4 = "exists? : " + show.exists() + "\n";
        String str5 = "hidden? : " + show.isHidden() + "\n";
        String str6 = "dir? : " + show.isDirectory() + "\n";
        String str7 = "file? : " + show.isFile() + "\n";
        String str8 = "modified (timestamp) : " + show.lastModified() + "\n";
        String str9 = "readable? : " + show.canRead() + "\n";
        String strA = "writable? : " + show.canWrite() + "\n";
        String strB = "executable? : " + show.canExecute() + "\n";
        String strC = "parent : " + show.getParent() + "\n";
        String strD = "absolute file : " + show.getAbsoluteFile() + "\n";
        String strE = "absolute path : " + show.getAbsolutePath() + "\n";
        String strF = "canonical file : " + show.getCanonicalFile() + "\n";
        String strG = "canonical path : " + show.getCanonicalPath() + "\n";
        String strH = "partition space (bytes) : " + show.getTotalSpace() + "\n";
        String strI = "usable space (bytes) : " + show.getUsableSpace() + "\n";

        return str1 + str2 + str3 + str4 + str5 + str6 + str7 + str8 + str9 + strA + strB + strC + strD + strE + strF
                + strG + strH + strI;
    }

    public static void main(String[] args) throws IOException {
        int tcpPort = 9999;
        int udpPort = 9998;

    }
}