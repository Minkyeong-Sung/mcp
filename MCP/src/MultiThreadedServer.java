import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.UUID;

public class MultiThreadedServer {

  // A generic function to generate HTTP request
  // host: Hostname of server (e.g. sing.cse.ust.hk)
  // port: Port number (e.g. Web 80 FTP 21 SMTP 25)d
  // path: URL path e.g. "/index.php"
  // method: GET or POST
  public static void HTTP_Request(String host, int port, String path,String data) throws UnknownHostException, IOException {
    //Resolve the hostname to an IP address
    InetAddress ip = InetAddress.getByName(host);

    //Open socket to a specific host and port
    Socket socket = new Socket(host, port);
        
    //Get input and output streams for the socket
    OutputStream out = socket.getOutputStream();
    InputStream in = socket.getInputStream();

    // Constructs a HTTP POST request
      // The end of HTTP POST header should be \r\n\r\n
      // After HTTP POST header, it's HTTP POST data
      // POST it's different from GET: the data of POST is added at the end of HTTP request
      String request = "POST " + path + " HTTP/1.0\r\n" + "Accept: */*\r\n"
         + "Host: " + host + "\r\n"
         + "TID:" + UUID.randomUUID().toString() + "\r\n"
         + "VIN:" + "local_test\r\n" 
         + "Content-Type: application/json; charset=UTF-8\r\n"
         + "Authorization:Basic dG1zOmhtYWdlbjI=\r\n"
         + "Content-Length: " + data.length() + "\r\n\r\n" + data;

      // Send off HTTP POST request
      out.write(request.getBytes());
      out.flush();
        
    // Reads the server's response
    StringBuffer response=new StringBuffer();
    byte[] buffer = new byte[4096];
    int bytes_read;

    // Reads HTTP response
    while ((bytes_read = in.read(buffer, 0, 4096)) != -1) {
      // Print server's response 
      for(int i = 0; i < bytes_read; i++)
        response.append((char)buffer[i]);
    }
        
    if (response.substring(response.indexOf(" ") + 1, response.indexOf(" ") + 4).equals("200")) {
      //Save the payload of the HTTP response message
      File file = new File("tr_tc_13.json");
      PrintWriter printWriter = new PrintWriter(file);
      printWriter.println(response.substring(response.indexOf("\r\n\r\n") + 4));
      printWriter.close();
    } else
      System.out.println("HTTP request failed");
    
    // Closes socket
    socket.close();
  }
    
  public static void main(String[] args) throws Exception {
     while(true) {
       // Hostname
       String host = "10.107.66.41";

       // Port
       int port = 8081;

       // Path
       String path = "C:/GlobalMCTClient/traffic";
       
       // GET/POST data
       String data = "name=POON";
       
       // Generates HTTP request
       HTTP_Request(host, port, path, data);
    }
  }
}