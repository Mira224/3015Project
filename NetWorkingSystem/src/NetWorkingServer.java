import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class NetWorkingServer {
 ServerSocket srvSocket;

 public NetWorkingServer(int port) throws IOException {

  // receiving socket from client
  srvSocket = new ServerSocket(port);
  byte[] buffer = new byte[1024];
  while (true) {
   System.out.printf("Listening at port %d...\n", port);

   Socket clientSocket = srvSocket.accept();

   System.out.printf("Established a connection to host %s:%d\n\n", clientSocket.getInetAddress(),
     clientSocket.getPort());

   DataInputStream in = new DataInputStream(clientSocket.getInputStream());
   DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
   int len = in.readInt();
   in.read(buffer, 0, len);
   
   String command = new String(buffer, 0, len);
   String[] com = command.split("\\s");
   String reMsg = "";
   
   switch (com[0]) {
   case "0":
    reMsg = Login(com[1] + " " + com[2]);
    break;
   }
   
   out.writeInt(reMsg.length());
   out.write(reMsg.getBytes(), 0, reMsg.length());

   clientSocket.close();
  }
 }
 
 //login function
 public static String Login(String userinfo) throws IOException {
  File user = new File("userinfo.txt");
  Scanner u = new Scanner(user);
  while (u.hasNext()) {
   if (u.nextLine().equals(userinfo)) {
    u.close();
    return "Login success";
   }
  }
  u.close();
  return "Login failed";
 }

 public static void main(String[] args) throws IOException {
  new NetWorkingServer(9999);
 }
}