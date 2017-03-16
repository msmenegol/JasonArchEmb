
import jason.architecture.*;
import jason.asSemantics.*;
import jason.asSyntax.*;
import jason.infra.centralised.BaseCentralisedMAS;
import java.util.*;
import java.util.concurrent.*;

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
  private static final String heartbeat = "*";
  //*********************************************************


  //JasonBulb makes the interface between the Agent and the rest of the system
  private JasonBulb bulb = new JasonBulb(this);
  Thread bulbThread = new Thread(bulb);

  private List<Literal> worldState = new ArrayList<Literal>();
  private List<Literal> newWorldState = new ArrayList<Literal>();

  private ConcurrentHashMap<ActionExec,String> waitingConfirmList = new ConcurrentHashMap<ActionExec,String>();

  @Override
  public void init(){
    //initialize the bulb
    bulbThread.setDaemon(true);
    bulbThread.start();

    try{
      Thread.sleep(200);//wait a bit for bulb to start up
    }catch(Exception e){e.printStackTrace();}

  }
    // this method just add some perception for the agent

  @Override
  public List<Literal> perceive() {
      List<Literal> p = new ArrayList<Literal>();//super.perceive();
      //p.add(waypoint);
      p.addAll(this.worldState);
      //p.addAll();
      bulb.bulbSend(this.heartbeat);//request new percepts
      return p;
  }

    // this method get the agent actions
    @Override
    public void act(ActionExec action) {

        String s = actionToString(action);

        waitingConfirmList.put(action,actionToString(action));

        boolean done = bulb.bulbSend(encodeAction(actionToString(action)));

        if(!done){
          //Abort action
          action.setResult(false);
          actionExecuted(action);
          System.out.println("action fail");
        }


    }


    public void addPercept(String percept){
      String[] strTerms = percept.split("[(),]");//get functor, aka thing before "("
      Literal literalPercept = ASSyntax.createLiteral(strTerms[0]);
      //Build literalPercept to be added
      if(!strTerms[0].isEmpty()){//if there is a functor
        if(strTerms.length>1){
          for(int i=1; i < strTerms.length; i++){
            if(strTerms[i].matches(".*\\d+.*")){//if there are numbers in the term
              double number = Double.NaN;
              try{
                number = Double.parseDouble(strTerms[i]);
              }catch(Exception e){e.printStackTrace();}

              literalPercept.addTerm(ASSyntax.createNumber(number));
            }else{ //otherwise, it's a string
              literalPercept.addTerm(ASSyntax.createString(strTerms[i]));
            }
          }
        }
        this.worldState.removeAll(findFunctor(worldState, strTerms[0]));//remove all emergencies with same functor
        this.worldState.add(literalPercept);
      }
    }

    public List<Literal> addPercepts(String percepts){
      String[] strPercepts = percepts.split(";");

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
      }
      return this.newWorldState;
    }

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

    public boolean isHeartbeat(String message){
      return this.heartbeat.equals(message);
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
