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
  //ready
  private boolean ready = false;
  //socket object
  private Socket socket;
  //connection info
  String ip = "localhost";
  int port = 6969;
  //in buffer
  private BufferedReader in;
  //out buffer
  private PrintWriter out;
  //mailbox
  private List<String> mailbox = new ArrayList<String>();

  //constructor
  public JasonBulb(EJasonArch c){
    this.cortex = c;
  }

  public void run() {
    try{
      String[] line;
      BufferedReader conInfo = new BufferedReader(new FileReader("connectionInfo"));
      if((line = (conInfo.readLine()).split(":"))[0].equals("port")){this.ip = line[1];}
      if((line = (conInfo.readLine()).split(":"))[0].equals("port")){this.port = Integer.parseInt(line[1]);}
    } catch(Exception e){}
    // socket tcp connection
    //String ip = "localhost";
    //int port = 6969;
    try{
      this.socketConnect(this.ip, this.port);
    } catch (Exception e) {
      e.printStackTrace();
    }

    System.out.println("[Connection established]");
    try{
      this.in = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));
      this.out = new PrintWriter(getSocket().getOutputStream(), true);
    }catch(Exception e){e.printStackTrace();}

    this.ready = true;

    while(true){
      bulbReceive();
    }
  }
    // make the connection with the socket
  private void socketConnect(String ip, int port) throws UnknownHostException, IOException {
      System.out.println("[Connecting to socket...]");
      this.socket = new Socket(ip, port);
  }

  public void bulbSend(String message){//return true if message was sent
    if(this.socket!=null){
      while(!this.ready);//do not try to send before buffers are set up
      try{
        if(this.socket.getInetAddress().isReachable(timeout)){
          //PrintWriter out = new PrintWriter(getSocket().getOutputStream(), true);
          System.out.println("sending " + message);
          this.out.println(message);
          //out.close();
          //return true;
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
    } //return false;
  }


  public void bulbReceive(){//return the message or ""
    if(this.socket!=null){
      try{
        if(this.socket.getInetAddress().isReachable(timeout)){
          //BufferedReader in = new BufferedReader(new InputStreamReader(getSocket().getInputStream()));
          System.out.println("reading");

          String message = this.in.readLine();

          System.out.println("message is " + message);

          if(!message.equals("")){//if it's a alid message
            if(cortex.isAction(message)){//if it's an action
              //System.out.println("sendEm");
              cortex.confirmAction(cortex.decodeAction(message));//confirm execution
            } else if(cortex.isPercept(message)){//then it's a percept
              cortex.addPercept(cortex.decodePercept(message));
            }
          }
        }
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
  }
/*
  private boolean isEmergency(String s){
    System.out.println("checking on" + s);
    if(!s.isEmpty()){if(s.substring(0, 1).equals("!")){return true;}}//emergency messages begin with !
    return false;
  }

  private String parseEmergency(String em){
    return em.substring(1);
  }

  public boolean isInMailbox(String message){
    return this.mailbox.remove(message);
  }
*/
    // get the socket instance
  private Socket getSocket() {
       return socket;
  }


}
