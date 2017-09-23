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
      if((line = (conInfo.readLine()).split(":"))[0].equals("ip")){this.ip = line[1];}
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

    bulbSend(cortex.getAgName());

    while(true){
      if(this.socket.isClosed()){
        break;
      }
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

          if(!"".equals(message)){//if it's a valid message
            if(cortex.isAction(message)){//if it's an action
              cortex.confirmAction(cortex.decodeAction(message));//confirm execution

            } else if(cortex.isFail(message)){//if it's a fail
              cortex.failAction(cortex.decodeFail(cortex.decodeAction(message)));

            } else if(cortex.isMessage(message)){
              cortex.addToMailBox(cortex.decodeMessage(message));

            } else if(cortex.isPercept(message)){//then it's a percept
              List<String[]> newPercepts = cortex.splitPercepts(cortex.decodePercept(message));
              List<Literal> percepts = new ArrayList<Literal>();

              for(String[] newPercept : newPercepts){
                percepts.addAll(cortex.toLiteral(PerceptFilter.filter(newPercept,cortex.getPercepts())));
              }

              cortex.addPercepts(percepts);

            }
          }
        }
      } catch(Exception e) {
        e.printStackTrace();
        this.out.close();

        try{
          this.in.close();
          this.socket.close();
        } catch(Exception closeException){
          closeException.printStackTrace();
        }
      }
    }
  }
    // get the socket instance
  private Socket getSocket() {
       return socket;
  }

  public boolean isReady() {
    return this.ready;
  }
}
