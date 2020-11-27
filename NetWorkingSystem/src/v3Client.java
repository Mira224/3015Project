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
import java.util.concurrent.TimeUnit;

class Server {
    String srvName;
    String IP;
}

public class v3Client {
    static int TCPport = 9999;
    static int UDPport = 12346;
    static int UdestPort=9998;
    DatagramSocket udpSocket;
    Socket clientSocket;
    String com = "";// which command the user require
    DataInputStream in;
    DataOutputStream out;
    ArrayList<Server> srvList = new ArrayList<Server>();

    public v3Client() {
        //udp thread
        Thread udp = new Thread(() -> {
            try {
                discovery(UDPport);
            } catch (IOException e) {
                e.printStackTrace();
            }

        });
        udp.start();

        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //tcp thread
        try {
            while (true)
                new v3Client(srvList, TCPport);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void discovery(int udpPort) throws IOException {
        udpSocket = new DatagramSocket(udpPort);
        byte[] str = "PC A".getBytes();
        InetAddress destination = InetAddress.getByName("255.255.255.255");
        DatagramPacket packet = new DatagramPacket(str, str.length, destination, UdestPort);
        udpSocket.send(packet);

        while (true) {


            DatagramPacket p = new DatagramPacket(new byte[1024], 1024);
            udpSocket.receive(p);
            if (packet != p) {
                byte[] data = p.getData();
                String srvInfo = new String(data, 0, p.getLength());

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
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                try {

                        appStart(login, exit);

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
                System.out.println();
                System.out.println("Available commands:");
                System.out.println();
                System.out.println("| rd directory_path | Remove sub-directory ");
                System.out.println("| dir path | Read file list in the path ");
                System.out.println("| md new_directory_path | Create new directory ");
                System.out.println("| del file_path | File Delete");
                System.out.println("| readDe file_path | Read details of the file ");
                System.out.println("| ren original_file_path new_filename | File rename");
                System.out.println("| upload | Upload the file");
                System.out.println("| download | download the file");
                System.out.println("| logout");
                System.out.println("| exit");
                System.out.println();

                do {
                    System.out.print("Please input your command>");
                    Scanner newScanner = new Scanner(System.in);
                    String cmd = newScanner.nextLine();
                    String[] com = cmd.split("\\s+");
                    if(com.length==1){
                        if (com[0].equalsIgnoreCase("logout")) {
                            login = false;
                            out.writeInt(cmd.length());
                            out.write(cmd.getBytes(), 0, cmd.length());

                            int l = in.readInt();
                            in.read(buffer, 0, l);
                            String sMsg = new String(buffer, 0, l);
                            System.out.println(sMsg);
                            break;
                        } else if (com[0].equalsIgnoreCase("upload")) {// command
                            upload(cmd);
                            int length = in.readInt();

                            while (length > 0) {
                                int blen = in.read(buffer, 0, buffer.length);

                                String ser = new String(buffer, 0, blen);

                                System.out.println(ser);

                                length -= blen;
                            }
                        } else if (com[0].equalsIgnoreCase("download")) {

                            download(cmd);

                        }else if(com[0].equalsIgnoreCase("exit")){
                            login=false;
                            exit = true;
                            out.writeInt(cmd.length());
                            out.write(cmd.getBytes(), 0, cmd.length());

                            int l = in.readInt();
                            in.read(buffer, 0, l);
                            String sMsg = new String(buffer, 0, l);
                            System.out.println(sMsg);
                            break;
                        }else{
                            System.out.println("Wrong command.");

                        }

                    } else {

                        out.writeInt(cmd.length());
                        out.write(cmd.getBytes(), 0, cmd.length());
                        int length = in.readInt();

                        while (length > 0) {
                            int blen = in.read(buffer, 0, buffer.length);

                            String ser = new String(buffer, 0, blen);

                            System.out.println(ser);

                            length -= blen;

                        }

                    }

                } while (login);
            }

        } while (!exit);

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

//            System.out.println("Client Transmission finished.");

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

//		v3Client c = new v3Client();

//		c.end();
    }

}