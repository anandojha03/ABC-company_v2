import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@AllArgsConstructor
public class ClientHandler implements Runnable{

    Socket clientSocket;
    String fileDirectory;

    @Override
    public void run() {
        Request request = parseRequest(clientSocket);
        try {
            String response = handle(request);
            System.out.println("response sending " + response);
            if (response != null) {
                clientSocket.getOutputStream().write(response.getBytes());
            }

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

            StringBuffer bodyBuffer = new StringBuffer();
            while (in.ready()) {
                bodyBuffer.append((char) in.read());
            }

            request.setBody(bodyBuffer.toString());
            return request;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String handle(Request request) throws IOException {

        if (request.getMethod().equals("POST") && (request.getPath().startsWith("/files"))) {
                String[] arr = request.getPath().split("/");
                String fileName = arr[2];
                File file = new File(fileDirectory + fileName);
                if (file.createNewFile()) {
                    FileWriter fileWriter = new FileWriter(file);
                    fileWriter.write(request.getBody());
                    fileWriter.close();
                }
                return new String("HTTP/1.1 201 Created\r\n\r\n");
        }
        else if (request.getPath().equals("/")) {
            return new String(Constants.HTTP_1_1 + " " + Constants.SUCCESS200 + " " + Constants.OK + Constants.CRLF + Constants.CRLF);
        } else if (request.getPath().startsWith("/echo")) {

            if ((request.getHeaders().containsKey("Accept-Encoding"))) {
                if (request.getHeaders().get("Accept-Encoding").contains("gzip")) {
                    System.out.println(request);
                    byte[] responseBody = compressStringToGzip(request.getPath().substring(6));
                    String response = new String("HTTP/1.1 200 OK\r\n" +
                            "Content-Encoding: gzip\r\n" +
                            "Content-Type: text/plain\r\n" +
                            "Content-Length: " + responseBody.length + "\r\n\r\n");
                    clientSocket.getOutputStream().write(response.getBytes());
                    clientSocket.getOutputStream().write(responseBody);
                    return null;
                }else {
                    return new String("HTTP/1.1 200 OK\r\n" +
                            "Content-Type: text/plain\r\n\r\n");
                }

            } else {
                String[] path = request.getPath().split("/");
                return new String(Constants.HTTP_1_1 + " " + Constants.SUCCESS200 + " " + Constants.OK + Constants.CRLF + Constants.CONTENT_TYPE + " " + Constants.TEXT_PLAIN + Constants.CRLF + Constants.CONTENT_LENGTH + " " + path[2].length() + Constants.CRLF + Constants.CRLF + path[2]);

            }
        } else if (request.getPath().startsWith("/user-agent")) {
            return new String(Constants.HTTP_1_1 + " " + Constants.SUCCESS200 + " " + Constants.OK + Constants.CRLF + Constants.CONTENT_TYPE + " " + Constants.TEXT_PLAIN + Constants.CRLF + Constants.CONTENT_LENGTH + " " + request.getHeaders().get("User-Agent").length() + Constants.CRLF + Constants.CRLF + request.getHeaders().get("User-Agent"));
        } else if (request.getPath().startsWith("/files")) {
            String[] path = request.getPath().split("/");
            String fileName = path[2];
            File file = new File(fileDirectory, fileName);
            if (file.exists()) {
                byte[] fileContent = Files.readAllBytes(file.toPath());
                return new String("HTTP/1.1 200 OK\r\nContent-Type: application/octet-stream\r\nContent-Length: " + fileContent.length + Constants.CRLF + Constants.CRLF + new String(fileContent));
            }else {
                return new String("HTTP/1.1 404 Not Found\r\n\r\n");
            }
        }else {
            return new String(Constants.HTTP_1_1 + " 404 Not Found" + Constants.CRLF + Constants.CRLF);
        }
    }

    public static byte[] compressStringToGzip(String str) throws IOException {
        if (str == null || str.isEmpty()) {
            return null;
        }
        byte[] inputBytes = str.getBytes();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(inputBytes);
        }

        return byteArrayOutputStream.toByteArray();
    }


}
