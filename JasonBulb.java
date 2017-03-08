import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

import jason.asSyntax.*;

public class JasonBulb implements Runnable{
  //timeout time in ms
  public static final int timeout = 1000;
  //parent architecture instance
  private EJasonArch cortex;
  // socket object
  private Socket socket;

  //mailbox
  private List<String> mailbox = new ArrayList<String>();

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
    System.out.println("[Connection established]");

    while(true){
      bulbReceive();
    }
  }
    // make the connection with the socket
  private void socketConnect(String ip, int port) throws UnknownHostException, IOException {
      System.out.println("[Connecting to socket...]");
      this.socket = new Socket(ip, port);
  }

  public boolean bulbSend(String message){//return true if message was sent
    if(this.socket!=null){
      try{
        if(this.socket.getInetAddress().isReachable(timeout)){
        PrintWriter out = new PrintWriter(getSocket().getOutputStream(), true);
        out.println(message);
        //out.close();
        return true;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    } return false;
  }


  public void bulbReceive(){//return the message or ""
    if(this.socket!=null){
      try{
        if(this.socket.getInetAddress().isReachable(timeout)){
          BufferedReader in = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));

          System.out.println("reading");
          String message;
          while((message = in.readLine())!=null){
            if(isEmergency(message)){
              System.out.println("sendEm");
              cortex.emergency(parseEmergency(message));
            } else {
              this.mailbox.add(message);
            }
          }
        }
      } catch(Exception e) {
          e.printStackTrace();
        }
      }
  }
  private boolean isEmergency(String s){
    System.out.println("checking em on" + s);
    if(!s.isEmpty()){if(s.substring(0, 1).equals("!")){return true;}}//emergency messages begin with !
    return false;
  }

  private String parseEmergency(String em){
    return em.substring(1);
  }

  public boolean isInMailbox(String message){
    return this.mailbox.remove(message);
  }

/*
    // writes and receives the full message int the socket (String)
  public String sendReceive(String message) {
    //System.out.println("beginning send");
    if(this.socket!=null){

      try{
        //System.out.println(socket.getInetAddress().isReachable(1000));
        if(!socket.getInetAddress().isReachable(timeout)){
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

        //if(this.checkEmergency()){return "notReady";}//do not proceed while an emergency is happening

        String returnStr = in.readLine();
        return returnStr;
      } catch (IOException e) {
          e.printStackTrace();
      }
    }
    //System.out.println("socket is null");
    return "notReady";//try again
  }
*/

    // get the socket instance
  private Socket getSocket() {
       return socket;
  }

/*
  public boolean checkEmergency(){
    if (this.socket!=null){
      try{
        //System.out.println(socket.getInetAddress().isReachable(1000));
        if(!socket.getInetAddress().isReachable(1000)){
          //if it isn't reachable or timeout of 1s
          System.out.println("not reachable");//do something
          return false;
        }
      } catch(Exception e){e.printStackTrace();}

      try{
        BufferedReader in = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));
        char c = peek(in);
        System.out.println(c);
        if(c == '!'){//if it's an emergency
          System.out.println("EMERGENCY!");
          //BufferedReader in = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));
          String emergencyID = in.readLine();//get whole emergencyID
          in.close();
          cortex.emergency(emergencyID);//transmit emergency to cortex
          return true;
        }
        in.close();
        System.out.println("notEm");
        return false;//else {//otherwise
          //System.out.println("putting it back");
          //peekableIn.unread(c);//put thingS back in place
          //return false;
        //}
      } catch(Exception e){
        return false;
      }
    } return false;
  }

  private char peek(BufferedReader buf){
    try{
      buf.mark(1);
      char b = (char) buf.read();
      buf.reset();
      return b;
    } catch(Exception e){e.printStackTrace();}
    return '\0';
  }
*/
}
