
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
public class eJasonArch extends AgArch {

  private jasonBulb bulb = new jasonBulb();
  private P3d position = new P3d(); //position right now
  private P3d waypoint = new P3d(); //waypoint that UAV is going to now

  @Override
  public void init(){
    try{bulb.init();}
    catch(Exception e){e.printStackTrace();}
  }
    // this method just add some perception for the agent

  @Override
  public List<Literal> perceive() {
      List<Literal> p = new ArrayList<Literal>();//super.perceive();
      p.add(ASSyntax.createLiteral("waypoint",
                                    ASSyntax.createNumber(this.waypoint.getX()),
                                    ASSyntax.createNumber(this.waypoint.getY()),
                                    ASSyntax.createNumber(this.waypoint.getZ())));

      //String position = bulb.SendReceive("position");
      //l.add(Literal.parseLiteral(position)); //message should be pos(x,y,z)
      return p;
  }

    // this method get the agent actions
    @Override
    public void act(ActionExec move) {

        //getting args from move(x,y,z)
        double x = Double.NaN;
        double y = Double.NaN;
        double z = Double.NaN;
        try{
          x = ((NumberTerm) move.getActionTerm().getTerm(0)).solve();
          y = ((NumberTerm) move.getActionTerm().getTerm(1)).solve();
          z = ((NumberTerm) move.getActionTerm().getTerm(2)).solve();}
        catch(Exception e){
          e.printStackTrace();
        }

        getTS().getLogger().info("Agent " + getAgName() + " is doing: " + move.getActionTerm().getFunctor() + " to " + x + ", " + y + ", " + z);

        String s = bulb.SendReceive("move("+ x + "," + y + "," + z + ")");
        //subsistute the following by some kind of parsing
        System.out.println(s);
        if(s.equals("move("+x+","+y+","+z+")")){
          this.waypoint.set(x,y,z);
        }
        System.out.println(this.waypoint.getX());
        // set that the execution was ok
        //move.setResult(true);
        //actionExecuted(move);
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
