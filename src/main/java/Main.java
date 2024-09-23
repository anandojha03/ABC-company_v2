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

       clientSocket = serverSocket.accept(); // Wait for connection from client.
       String failureResponse = "HTTP/1.1 404 Not Found\r\n\r\n";

         BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
         String request = in.readLine();
         String[] httpHeaders = request.split(" ");
         if (httpHeaders[1].equals("/")) {
             String successResponse = "HTTP/1.1 200 OK\r\n\r\n";
             clientSocket.getOutputStream().write(successResponse.getBytes());
         }else if (httpHeaders[1].startsWith("/echo")) {
             String[] body = httpHeaders[1].split("/");
             String successResponse = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\nContent-Length: " + body[2].length() + "\r\n\r\n" + body[2];
             clientSocket.getOutputStream().write(successResponse.getBytes());
         }else if (httpHeaders[1].equals("/user-agent")) {
             String header;
             do {
                 header = in.readLine();
             } while (!header.startsWith("User-Agent"));
             String[] headers = header.split(" ");
             String successResponse = "HTTP/1.1 200 OK \r\nContent-Type: text/plain\r\nContent-Length: " + headers[1].length() + "\r\n\r\n" + headers[1];
             clientSocket.getOutputStream().write(successResponse.getBytes());
         }else {
             clientSocket.getOutputStream().write(failureResponse.getBytes());
         }

       System.out.println("accepted new connection");
     } catch (IOException e) {
       System.out.println("IOException: " + e.getMessage());
     } catch (Exception e) {
         System.out.println(e.getMessage());
     }
  }
}
