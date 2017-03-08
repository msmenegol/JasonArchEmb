
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

  //Application specific attributes
//  private P3d position = new P3d(); //position right now
  private Literal position = ASSyntax.createLiteral("position",
                                ASSyntax.createNumber(0),
                                ASSyntax.createNumber(0),
                                ASSyntax.createNumber(0));

  private Literal waypoint = ASSyntax.createLiteral("waypoint",
                                ASSyntax.createNumber(0),
                                ASSyntax.createNumber(0),
                                ASSyntax.createNumber(0));

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
      p.add(waypoint);

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
        String s = actionToString(action);
        System.out.println("action is " + s);

        done = bulb.bulbSend(s);

        if(!done){
          //action did not go through
          //do something about it
          System.out.println("NotDone");
        }
        //subsistute the following by some kind of parsing
        //if(s == null) System.out.println("s is null");
        //System.out.println(s);

        confirmed = bulb.isInMailbox(s);//wait for the confirmation

        if(!confirmed){
          //action did not go through
          //do something about it
          System.out.println("NotConfirmed");
        }

        waypoint = ASSyntax.createLiteral("waypoint",
                                      ASSyntax.createNumber(x),
                                      ASSyntax.createNumber(y),
                                      ASSyntax.createNumber(z));

        //this.waypoint.set(x,y,z);
        double coordX=0;
        try{
          coordX = ((NumberTerm) waypoint.getTerm(0)).solve();
        } catch(Exception e){e.printStackTrace();}
        System.out.println(coordX);
        // set that the execution was ok
        //action.setResult(true);
        //actionExecuted(action);
    }

    public void addEmergency(String emergencyID){
      String[] strTerms = emergencyID.split("[(),]");//get functor, aka thing before "("
      //System.out.println("EM IS: " + strTerms[0]);
      if(!strTerms[0].isEmpty()){//if there is a functor
        emergencyList.removeAll(findFunctor(emergencyList, strTerms[0]));//remove all emergencies with same functor
      }
      Literal literalEm = ASSyntax.createLiteral(strTerms[0]);
      if(strTerms.length>1){
        for(int i=1; i < strTerms.length; i++){
          if(strTerms[i].matches(".*\\d+.*")){//if there are numbers in the term
            double number=0;
            try{
              number = Double.parseDouble(strTerms[i]);
            }catch(Exception e){e.printStackTrace();}

            literalEm.addTerm(ASSyntax.createNumber(number));
          }else{ //otherwise, it's a string
            literalEm.addTerm(ASSyntax.createString(strTerms[i]));
          }
        }
      }
      emergencyList.add(literalEm);
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

    private String actionToString(ActionExec action){
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
