
import jason.architecture.*;
import jason.asSemantics.*;
import jason.asSyntax.*;
import jason.infra.centralised.BaseCentralisedMAS;
import java.util.*;


//import jasonBulb;//client class

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
  //*********************************************************


  //JasonBulb makes the interface between the Agent and the rest of the system
  private JasonBulb bulb = new JasonBulb(this);
  Thread bulbThread = new Thread(bulb);

  private List<Literal> worldState = new ArrayList<Literal>();

  private Map<ActionExec,String> waitingConfirmList = new HashMap<ActionExec,String>();

  //Application specific attributes
//  private P3d position = new P3d(); //position right now
/*
  private Literal position = ASSyntax.createLiteral("position",
                                ASSyntax.createNumber(0),
                                ASSyntax.createNumber(0),
                                ASSyntax.createNumber(0));

  private Literal waypoint = ASSyntax.createLiteral("waypoint",
                                ASSyntax.createNumber(0),
                                ASSyntax.createNumber(0),
                                ASSyntax.createNumber(0));
*/
  @Override
  public void init(){
    //initialize the bulb
    //try{bulb.init();}
    //catch(Exception e){e.printStackTrace();}
    bulbThread.setDaemon(true);
    bulbThread.start();
    //while(!bulb.isReady());//wait for it to initialize properly
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
      return p;
  }

    // this method get the agent actions
    @Override
    public void act(ActionExec action) {
        //general variables for action handling

        //getTS().getLogger().info("Agent " + getAgName() + " is doing: " + action.getActionTerm().getFunctor() + " to " + x + ", " + y + ", " + z);


        String s = actionToString(action);
        System.out.println("action is " + s);

        bulb.bulbSend(encodeAction(actionToString(action)));

          //DO SOMETHING IF ACTION NEVER GETS CONFIRMED

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
        System.out.println("Literal is" + literalPercept.toString());
        this.worldState.add(literalPercept);
      }
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

    public List<String> getPercepts(String functor){
      List<Literal> litPercepts = findFunctor(this.worldState, functor);
      List<String> strPercepts = new ArrayList<String>();
      for(Literal object : litPercepts){
        strPercepts.add(((Literal) object).toString());
      }
      return strPercepts;
    }
/*
    public String perceptToString(Literal percept){
      String s = percept.getFunctor() + "(";
      List<Term> terms = percept.getTerms();
        if(term.isString()){
          s = s + ((StringTerm) term).getString() + ",";
        } else if(term.isNumeric()){
          try{
            s = s + Double.toString(((NumberTerm) term).solve()) + ",";
          } catch(Exception e) {e.printStackTrace();}
        }
      s = s.substring(0, s.length()-1) + ")"; //take last ',' out and close with ')'
      return s;
    }
*/
    public String actionToString(ActionExec action){
      String s = action.getActionTerm().getFunctor() + "(";
      List<Term> terms = action.getActionTerm().getTerms();
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
      return s;
    }


    public void confirmAction(String actionStr){
      System.out.println("Confirmation was called with " + actionStr);
      Iterator it = waitingConfirmList.entrySet().iterator();
      while(it.hasNext()){
        Map.Entry pair = (Map.Entry)it.next();
        if(pair.getValue().equals(actionStr)){
          //set that the execution was ok
          System.out.println("Confirming " + pair.getValue());
          ((ActionExec)pair.getKey()).setResult(true);
          actionExecuted((ActionExec)pair.getKey());
          it.remove();
        }
      }
    }

    public boolean isAction(String actionStr){
      return actionStr.substring(0,this.actionID.length()).equals(this.actionID);
    }

    public String decodeAction(String message){
      return message.substring(this.actionID.length(),message.length());
    }

    private String encodeAction(String message){
      System.out.println("Encoded " + message + "   " + this.actionID);
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
