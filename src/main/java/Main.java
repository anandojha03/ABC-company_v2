import com.sun.net.httpserver.Request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class Main {
  public static void main(String[] args) {

    Socket clientSocket = null;
     try {
       ServerSocket serverSocket = new ServerSocket(4221);

       // Since the tester restarts your program quite often, setting SO_REUSEADDR
       // ensures that we don't run into 'Address already in use' errors
       serverSocket.setReuseAddress(true);

       while (true) {
           clientSocket = serverSocket.accept();
           System.out.println("accepted new connection");
           ClientHandler clientHandler = new ClientHandler(clientSocket);
           new Thread(clientHandler).start();
       }

     } catch (IOException e) {
       System.out.println("IOException: " + e.getMessage());
     } catch (Exception e) {
         System.out.println(e.getMessage());
     }
  }
}
