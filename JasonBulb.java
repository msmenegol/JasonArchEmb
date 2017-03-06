import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import jason.asSyntax.*;

public class JasonBulb{
  // socket object
  private Socket socket = null;

  public void init() throws UnknownHostException,   IOException, ClassNotFoundException {
    // class instance
    //jasonBulb client = new jasonBulb();

    // socket tcp connection
    String ip = "localhost";
    int port = 6969;
    try{
        this.socketConnect(ip, port);
      } catch (UnknownHostException e) {
          e.printStackTrace();
      }
    System.out.println("connection established");


    // writes and receives the message
    //String message = "message123";

    //System.out.println("Sending: " + message);
    //String returnStr = client.echo(message);
    //System.out.println("Receiving: " + returnStr);
  }
    // make the connection with the socket
  private void socketConnect(String ip, int port) throws UnknownHostException, IOException {
      System.out.println("[Connecting to socket...]");
      this.socket = new Socket(ip, port);
  }

    // writes and receives the full message int the socket (String)
  public String SendReceive(String message) {
    if (this.socket!=null){
    try {
      // out & in
      PrintWriter out = new PrintWriter(getSocket().getOutputStream(), true);
      BufferedReader in = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));
      // writes str in the socket and read
      out.println(message);
      String returnStr = in .readLine();
      return returnStr;
    } catch (IOException e) {
        e.printStackTrace();
    }
  }
    System.out.println("uh-oh");
    return null;
  }
    // get the socket instance
  private Socket getSocket() {
       return socket;
  }
}
