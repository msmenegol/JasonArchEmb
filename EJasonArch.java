
import jason.architecture.*;
import jason.asSemantics.*;
import jason.asSyntax.*;
import jason.infra.centralised.BaseCentralisedMAS;
import java.util.*;
import java.util.zip.*;
import java.util.concurrent.*;
import java.util.logging.*;
import java.io.*;
import jaca.*;
/**
 * Example of an agent that only uses Jason BDI engine. It runs without all
 * Jason IDE stuff. (see Jason FAQ for more information about this example)
 *
 * The class must extend AgArch class to be used by the Jason engine.
 */
public class EJasonArch extends AgArch {
  //******************Editable variables*********************
  private static final String actionID = "!";
  private static final String perceptID = "";
  private static final String failID = "@";
  private static final String messageID = "*";
  //*********************************************************
  private static Logger logger = Logger.getLogger(EJasonArch.class.getSimpleName());

  //JasonBulb makes the interface between the Agent and the rest of the system
  private JasonBulb bulb = new JasonBulb(this);
  Thread bulbThread = new Thread(bulb);

  private List<Literal> worldState = new ArrayList<Literal>();
  private List<Message> mailBox = new ArrayList<Message>();

  private ConcurrentHashMap<ActionExec,String> waitingConfirmList = new ConcurrentHashMap<ActionExec,String>();

  @Override
  public void init(){
    //initialize the bulb
    bulbThread.setDaemon(true);
    bulbThread.start();

    while(!bulb.isReady()){
      try{
          Thread.sleep(20);//wait a bit for bulb to start up
      }catch(Exception e){e.printStackTrace();}
    }
  }

    // this method just add some perception for the agent
  @Override
  public Collection<Literal> perceive() {
      super.perceive();
      Collection<Literal> p = new ArrayList<Literal>();//super.perceive();
      p.addAll(this.worldState);
      //p.addAll();
      //bulb.bulbSend(this.heartbeat);//request new percepts
      return p;
  }

  protected CAgentArch getCartagoArch() {
    AgArch arch = getTS().getUserAgArch().getFirstAgArch();
    while (arch != null) {
        if (arch instanceof CAgentArch) {
            return (CAgentArch)arch;
        }
        arch = arch.getNextAgArch();
    }
    return null;
  }

    // this method get the agent actions
  @Override
  public void act(ActionExec action) {
    //System.out.println(action.toString());
    if(action.getActionTerm().getNS().toString().equals("ext")){
      waitingConfirmList.put(action,actionToString(action));
      System.out.println(actionToString(action));
      boolean done = bulb.bulbSend(encodeAction(actionToString(action)));

      if(!done){
        //Abort action
        action.setResult(false);
        actionExecuted(action);
        System.out.println("action fail");
      }
    }
    else{
      System.out.println("mandei pro cartago");
      super.act(action);
    }
  }

  private static Object stringToObject( String s ) throws IOException , ClassNotFoundException {
     byte [] data = Base64.getDecoder().decode( s );
     GZIPInputStream gzipIn = new GZIPInputStream(new ByteArrayInputStream(data));
     ObjectInputStream ois = new ObjectInputStream( gzipIn );
     Object o  = ois.readObject();
     ois.close();
     return o;
  }

  private static String objectToString( Serializable o ) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    GZIPOutputStream gzipOut = new GZIPOutputStream(baos);
    ObjectOutputStream oos = new ObjectOutputStream( gzipOut );
    oos.writeObject( o );
    oos.flush();
    oos.close();
    return Base64.getEncoder().encodeToString(baos.toByteArray());
  }

  @Override
  public void checkMail() {
    Circumstance C = getTS().getC();
    Message im = new Message(); // pega a msgs da tua conexao
    while (!this.mailBox.isEmpty()) {
      im = this.mailBox.remove(0); // pega  aprox. msgs da tua conexao
      System.out.println("Content is of type: " + im.getPropCont().getClass().getName());
      System.out.println("Received message: " + im.toString());
      C.addMsg(im);
      if (logger.isLoggable(Level.FINE)) logger.fine("received message: " + im);
    }
  }

  @Override
  public void sendMsg(jason.asSemantics.Message m) throws Exception {

    // if content is an mapped object, use it
    if (m.getPropCont() instanceof Atom) {
        Object o = getCartagoArch().getJavaLib().getObject((Atom)m.getPropCont());
        if (o != null)
                m.setPropCont(o);
    }

    String recipient = m.getReceiver();
    String recipientWithMessage = recipient + "," + this.objectToString(m);
    boolean sent = bulb.bulbSend(encodeMessage(recipientWithMessage));
    if(!sent){
      throw new Exception("Message not sent");
    }
  }

  //broadcasts are the same as send, but the destination is null
  @Override
  public void broadcast(jason.asSemantics.Message m) throws Exception {
    String recipientWithMessage = "," + this.objectToString(m);
    boolean sent = bulb.bulbSend(encodeMessage(recipientWithMessage));
    if(!sent){
      throw new Exception("Broadcast not sent");
    }
  }

  public void addToMailBox(String strMsg){
    try{//Message.parseMsg(strMsg)
      this.mailBox.add( new Message( (Message) this.stringToObject(strMsg)));
    } catch(Exception e){
      e.printStackTrace();
    }
  }

  public void addPercepts(List<Literal> newState){
    this.worldState = newState;
  }

  public List<String[]> splitPercepts(String percepts){
    List<String[]> terms = new ArrayList<String[]>();
    String[] splitPercepts = percepts.split(";"); //at this stage, each element is a string p(x,y)
    for(int i = 0; i<splitPercepts.length; i++){
      terms.add(splitPercepts[i].split("[(),]"));//each element of terms become [p, x, y]
    }
    return terms;
  }

  public List<Literal> toLiteral(List<String[]> strPercepts){
    List<Literal> perceptsList = new ArrayList<Literal>();
    if(!strPercepts.isEmpty()){
      for(int i = 0; i<strPercepts.size(); i++){
        String[] strTerms = strPercepts.get(i);

        if(!strTerms[0].isEmpty()){//if there is a functor
          Literal literalPercept = ASSyntax.createLiteral(strTerms[0]);//first is functor
          if(strTerms.length>1){//if there are other terms
            for(int j=1; j < strTerms.length; j++){
              try{
                literalPercept.addTerm(ASSyntax.parseTerm(strTerms[j]));
              }catch(Exception e){e.printStackTrace();}
            }
          }
          perceptsList.add(literalPercept);
        }
      }
    }
    return perceptsList;
  }
      /*
      List<Literal> perceptsList = new ArrayList<Literal>();
      for(int i = 0; i<strPercepts.length; i++){
        String[] strTerms = strPercepts[i].split("[(),]");
        Literal literalPercept = ASSyntax.createLiteral(strTerms[0]);//first is functor

        if(!strTerms[0].isEmpty()){//if there is a functor
          if(strTerms.length>1){
            for(int j=1; j < strTerms.length; j++){
              if(strTerms[j].matches(".*\\d+.*")){//if there are numbers in the term
                double number = Double.NaN;
                try{
                  number = Double.parseDouble(strTerms[j]);
                }catch(Exception e){e.printStackTrace();}

                literalPercept.addTerm(ASSyntax.createNumber(number));
              }else{ //otherwise, it's a string
                literalPercept.addTerm(ASSyntax.createString(strTerms[j]));
              }
            }
          }
          perceptsList.add(literalPercept);
        }
      }
      return perceptsList;
    }
*/
  private List<Literal> findFunctor(List<Literal> list, String functor){
    List<Literal> matches = new ArrayList<Literal>();
    for(Literal object : list){
      if(object.getFunctor().equals(functor)){
        matches.add(object);
      }
    }
    return matches;
  }

  public List<String[]> getPercepts(String functor){
    List<Literal> litPercepts = findFunctor(this.worldState, functor);
    List<String[]> strPercepts = new ArrayList<String[]>();
    for(Literal object : litPercepts){
      strPercepts.add(((Literal) object).toString().split("[(),]"));
    }
    return strPercepts;
  }

  public List<String[]> getPercepts(){
    List<Literal> litPercepts = this.worldState;
    List<String[]> strPercepts = new ArrayList<String[]>();
    for(Literal object : litPercepts){
      strPercepts.add(((Literal) object).toString().split("[(),]"));
    }
    return strPercepts;
  }

  public String actionToString(ActionExec action){
    String s = action.getActionTerm().getFunctor();
    List<Term> terms = action.getActionTerm().getTerms();
    if (action.getActionTerm().hasTerm()){
      s = s + "(";
      for(Term term : terms){
        if(term.isString()){
          s = s + ((StringTerm) term).getString() + ",";
        } else if(term.isNumeric()){
          try{
            s = s + Double.toString(((NumberTerm) term).solve()) + ",";
          } catch(Exception e) {e.printStackTrace();}
        }
      }
      s = s.substring(0, s.length()-1) + ")"; //take last ',' out and close with ')'
    }
    return s;
  }

  public void confirmAction(String actionStr){
    Iterator it = waitingConfirmList.entrySet().iterator();
    while(it.hasNext()){

      Map.Entry pair = (Map.Entry) it.next();
      if(pair.getValue().equals(actionStr)){
        //set that the execution was ok
        ((ActionExec)pair.getKey()).setResult(true);
        actionExecuted((ActionExec)pair.getKey());
        it.remove();
      }
    }
  }

  public void failAction(String actionStr){
    Iterator it = waitingConfirmList.entrySet().iterator();
    while(it.hasNext()){
      Map.Entry pair = (Map.Entry)it.next();
      if(pair.getValue().equals(actionStr)){
        //set that the execution was ok
        ((ActionExec)pair.getKey()).setResult(false);
        actionExecuted((ActionExec)pair.getKey());
        it.remove();
        //waitingConfirmList.remove(pair.getKey());
      }
    }
  }

  public boolean isFail(String failStr){
    return failStr.substring(0,this.failID.length()).equals(this.failID);
  }

  public String decodeFail(String message){
    return message.substring(this.failID.length(),message.length());
  }

  private String encodeFail(String message){
    return this.failID+message;
  }

  public boolean isAction(String actionStr){
    return actionStr.substring(0,this.actionID.length()).equals(this.actionID);
  }

  public String decodeAction(String message){
    return message.substring(this.actionID.length(),message.length());
  }

  private String encodeAction(String message){
    return this.actionID+message;
  }

  public boolean isPercept(String perceptStr){
    return perceptStr.substring(0,this.perceptID.length()).equals(this.perceptID);
  }

  public String decodePercept(String message){
    return message.substring(this.perceptID.length(),message.length());
  }

  private String encodePercept(String message){
    return this.perceptID+message;
  }

  public boolean isMessage(String message){
    return message.startsWith(this.messageID);
  }

  public String decodeMessage(String message){
    return message.substring(this.messageID.length(),message.length());
  }

  private String encodeMessage(String message){
    return this.messageID+message;
  }


    //@Override
    //public boolean canSleep() {
    //    return true;
    //}

    //@Override
    //public boolean isRunning() {
    //    return true;
    //}

    // a very simple implementation of sleep
    //public void sleep() {
    //    try {
    //        Thread.sleep(1000);
    //    } catch (InterruptedException e) {}
    //}

    // Not used methods
    // This simple agent does not need messages/control/...
    //@Override
    //public void sendMsg(jason.asSemantics.Message m) throws Exception {
    //}

    //@Override
    //public void broadcast(jason.asSemantics.Message m) throws Exception {
    //}
    //@Override
    //public void checkMail() {
    //}
}
