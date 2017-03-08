
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

  //JasonBulb makes the interface between the Agent and the rest of the system
  private JasonBulb bulb = new JasonBulb(this);
  Thread bulbThread = new Thread(bulb);

  public List<Literal> emergencyList = new ArrayList<Literal>();

  public static final int maxTries = 10;
  public static final int holdTime = 100;

  //Application specific attributes
  private P3d position = new P3d(); //position right now
  private P3d waypoint = new P3d(); //waypoint that UAV will be going to now

  @Override
  public void init(){
    //initialize the bulb
    //try{bulb.init();}
    //catch(Exception e){e.printStackTrace();}
    bulbThread.setDaemon(true);
    bulbThread.start();
    //while(!bulb.isReady());//wait for it to initialize properly

  }
    // this method just add some perception for the agent

  @Override
  public List<Literal> perceive() {
      List<Literal> p = new ArrayList<Literal>();//super.perceive();
      p.add(ASSyntax.createLiteral("waypoint",
                                    ASSyntax.createNumber(this.waypoint.getX()),
                                    ASSyntax.createNumber(this.waypoint.getY()),
                                    ASSyntax.createNumber(this.waypoint.getZ())));

      p.addAll(emergencyList);
      //String position = bulb.SendReceive("position");
      //l.add(Literal.parseLiteral(position)); //message should be pos(x,y,z)
      return p;
  }

    // this method get the agent actions
    @Override
    public void act(ActionExec action) {
        //general variables for action handling
        int tries = 0;
        boolean done = false;
        boolean confirmed = false;

        List<Term> terms = action.getActionTerm().getTerms();


        //getting args from move(x,y,z)
        double x = Double.NaN;
        double y = Double.NaN;
        double z = Double.NaN;
        try{
          x = ((NumberTerm) action.getActionTerm().getTerm(0)).solve();
          y = ((NumberTerm) action.getActionTerm().getTerm(1)).solve();
          z = ((NumberTerm) action.getActionTerm().getTerm(2)).solve();}
        catch(Exception e){
          e.printStackTrace();
        }

        getTS().getLogger().info("Agent " + getAgName() + " is doing: " + action.getActionTerm().getFunctor() + " to " + x + ", " + y + ", " + z);

        //keep trying until it's ready
        //String s = action.getActionTerm().getFunctor() + "("+ x + "," + y + "," + z + ")";
        String s = convertAction(action);
        System.out.println("action is " + s);

        while(((done = bulb.bulbSend(s))==false) && tries<maxTries){
          System.out.println("notSent");
          tries++;
          try{
            Thread.sleep(holdTime);
          } catch(Exception e){e.printStackTrace();}
        }

        tries = 0;

        if(done == false){
          //action did not go through
          //do something about it
          System.out.println("NotDone");
        }
        //subsistute the following by some kind of parsing
        //if(s == null) System.out.println("s is null");
        //System.out.println(s);

        while(((confirmed = bulb.isInMailbox(s))==false) && tries<maxTries){//wait for the confirmation
          System.out.println("notReceived");
          tries++;
          try{
            Thread.sleep(holdTime);
          } catch(Exception e){e.printStackTrace();}
        }

        if(confirmed == false){
          //action did not go through
          //do something about it
          System.out.println("NotConfirmed");
        }

        this.waypoint.set(x,y,z);

        System.out.println(this.waypoint.getX());
        // set that the execution was ok
        //action.setResult(true);
        //actionExecuted(action);
    }

    public void emergency(String emergencyID){
      String[] strTerms = emergencyID.split("\\(");//get functor, aka thing before "("
      if(!strTerms[0].isEmpty()){//if there is a functor
        emergencyList.removeAll(findFunctor(emergencyList, strTerms[0]));//remove all emergencies with same functor
      }
      emergencyList.add(ASSyntax.createLiteral(emergencyID));
      //TODO: Change to add emergencyID directly to belief base
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

    private String convertAction(ActionExec action){
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
