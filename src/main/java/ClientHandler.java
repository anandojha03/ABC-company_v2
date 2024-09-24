import lombok.AllArgsConstructor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;
import java.util.TreeMap;

@AllArgsConstructor
public class ClientHandler implements Runnable{

    Socket clientSocket;
    @Override
    public void run() {
        Request request = parseRequest(clientSocket);
        String response = handle(request);
        try {
            clientSocket.getOutputStream().write(response.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Request parseRequest (Socket clientSocket) {
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
            Scanner scanner = new Scanner(inputStreamReader);
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String[] line1 = in.readLine().split(" ");
            Request request = new Request();
            request.setMethod(line1[0]);
            request.setPath(line1[1]);

            String line;
            TreeMap<String, String> map = new TreeMap<>();
            while (!(line = in.readLine()).equals("")){
                String[] header1 = line.split(": ");
                map.put(header1[0], header1[1].strip());
            }
            request.setHeaders(map);

            return request;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String handle(Request request) {
        if (request.getPath().equals("/")) {
            return new String(Constants.HTTP_1_1 + " " + Constants.SUCCESS200 + " " + Constants.OK + Constants.CRLF + Constants.CRLF);
        } else if (request.getPath().startsWith("/echo")) {
            String[] path = request.getPath().split("/");
            return new String(Constants.HTTP_1_1 + " " + Constants.SUCCESS200 + " " + Constants.OK + Constants.CRLF + Constants.CONTENT_TYPE + " " + Constants.TEXT_PLAIN + Constants.CRLF + Constants.CONTENT_LENGTH + " " + path[2].length() + Constants.CRLF + Constants.CRLF + path[2]);
        } else if (request.getPath().startsWith("/user-agent")) {
            return new String(Constants.HTTP_1_1 + " " + Constants.SUCCESS200 + " " + Constants.OK + Constants.CRLF + Constants.CONTENT_TYPE + " " + Constants.TEXT_PLAIN + Constants.CRLF + Constants.CONTENT_LENGTH + " " + request.getHeaders().get("User-Agent").length() + Constants.CRLF + Constants.CRLF + request.getHeaders().get("User-Agent"));
        } else {
            String failureResponse = "HTTP/1.1 404 Not Found\r\n\r\n";
            return new String(Constants.HTTP_1_1 + " 404 Not Found" + Constants.CRLF + Constants.CRLF);
        }
    }
}
