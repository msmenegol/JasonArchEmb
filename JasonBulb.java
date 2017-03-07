import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

import jason.asSyntax.*;

public class JasonBulb implements Runnable{
  //parent architecture instance
  private EJasonArch cortex;
  // socket object
  private Socket socket;
  // marks if connection was established
  private boolean ready = false;

  //constructor
  public JasonBulb(EJasonArch c){
    this.cortex = c;
  }

  public void run() {
    // socket tcp connection
    String ip = "localhost";
    int port = 6969;
    try{
        this.socketConnect(ip, port);
      } catch (Exception e) {
        e.printStackTrace();
      }
    this.ready = true;
    System.out.println("connection established");

    //while(true){
    //  checkEmergency();
    //}
  }
    // make the connection with the socket
  private void socketConnect(String ip, int port) throws UnknownHostException, IOException {
      System.out.println("[Connecting to socket...]");
      this.socket = new Socket(ip, port);
  }

    // writes and receives the full message int the socket (String)
  public String sendReceive(String message) {
    //System.out.println("beginning send");
    if(this.socket!=null){

      try{
        //System.out.println(socket.getInetAddress().isReachable(1000));
        if(!socket.getInetAddress().isReachable(1000)){
          //if it isn't reachable or timeout of 1s
          System.out.println("not reachable");//do something
        }
      } catch(Exception e){e.printStackTrace();}

      try {
        //System.out.println("trying send");
        // out & in
        PrintWriter out = new PrintWriter(getSocket().getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));
        // writes str in the socket and read
        //System.out.println(message + " sent");
        out.println(message);

        //checkEmergency();//do not proceed while an emergency is happening
/*
        String returnStr;
        while((returnStr = in.readLine()) != null);
        return returnStr;*/
        String returnStr = in.readLine();
        return returnStr;
      } catch (IOException e) {
          e.printStackTrace();
      }
    }
    //System.out.println("socket is null");
    return "notReady";//try again
  }
    // get the socket instance
  private Socket getSocket() {
       return socket;
  }

  public boolean checkEmergency(){
    if (this.socket!=null){
      try{
        PushbackReader peekableIn = new PushbackReader(new InputStreamReader(getSocket().getInputStream()));
        int c = peekableIn.read(); //peek at input buffer
        System.out.println((char)c);
        if(c == '!'){//if it's an emergency
          System.out.println("EMERGENCY!");
          BufferedReader in = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));
          String emergencyID = in.readLine();//get whole emergencyID
          cortex.emergency(emergencyID);//transmit emergency to cortex
          return true;
        } else {//otherwise
          peekableIn.unread((int) c);//put thingS back in place
          return false;
        }
      } catch(Exception e){
        return false;
      }
    } return false;
  }

}
