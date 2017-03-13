import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;

import jason.asSyntax.*;

public class JasonBulb implements Runnable{
  //timeout time in ms
  private static final int timeout = 1000;
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
      cortex.wake();
    }
  }
    // make the connection with the socket
  private void socketConnect(String ip, int port) throws UnknownHostException, IOException {
      System.out.println("[Connecting to socket...]");
      this.socket = new Socket(ip, port);
  }

  public boolean bulbSend(String message){//return true if message was sent
    if(this.socket!=null){
      while(!this.ready);//do not try to send before buffers are set up
      try{
        if(this.socket.getInetAddress().isReachable(timeout)){
          this.out.println(message);
          return true;
        }
      } catch (Exception e) {
        e.printStackTrace();
        return false;
      }
    }
    return false;
  }


  public void bulbReceive(){//return the message or ""
    if(this.socket!=null){
      try{
        if(this.socket.getInetAddress().isReachable(timeout)){

          String message = this.in.readLine();

          if(!message.equals("") && !cortex.isHeartbeat(message)){//if it's a valid message
            if(cortex.isAction(message)){//if it's an action
              cortex.confirmAction(cortex.decodeAction(message));//confirm execution

            } else if(cortex.isFail(message)){//if it's a fail
              cortex.failAction(cortex.decodeFail(cortex.decodeAction(message)));

            } else if(cortex.isPercept(message)){//then it's a percept
              String decodedPercept = cortex.decodePercept(message);
              String[] parts = decodedPercept.split("[(),]");//separate into functor and the rest
              List<String[]> oldPercepts = cortex.getPercepts(parts[0]);//get all percepts such as this one

              if(oldPercepts.isEmpty()) cortex.addPercept(decodedPercept); //if there are no others like this one
              else if(parts.length<2){ //there are no arguments to this percept
                if(PerceptFilter.filter(parts[0])) cortex.addPercept(decodedPercept);
              } else {//if there are functor and arguments
                for(int i=0; i<oldPercepts.size(); i++){
                  oldPercepts.set(i, Arrays.copyOfRange(oldPercepts.get(i), 1, oldPercepts.get(i).length)); //substitute each one by just the rest
                }
                if(PerceptFilter.filter(parts[0], Arrays.copyOfRange(parts,1,parts.length), oldPercepts)) cortex.addPercept(decodedPercept);
              }
            }
          }
        }
      } catch(Exception e) {
        e.printStackTrace();
      }
    }
  }
    // get the socket instance
  private Socket getSocket() {
       return socket;
  }
}
