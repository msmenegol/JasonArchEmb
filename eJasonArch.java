
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
  private Point3d position = new Point3d(); //position right now
  private Point3d waypoint = new Point3d(); //waypoint that UAV is going to now

  @Override
  public void init(){
    try{bulb.init();}
    catch(Exception e){e.printStackTrace();}
  }
    // this method just add some perception for the agent
/*
    @Override
    public List<Literal> perceive() {
        List<Literal> l = new ArrayList<Literal>();
        String position = bulb.SendReceive("position");
        l.add(Literal.parseLiteral(position)); //message should be pos(x,y,z)
        return l;
    }
*/
    // this method get the agent actions
    @Override
    public void act(ActionExec move(x,y,z) {
        getTS().getLogger().info("Agent " + getAgName() + " is doing: " + move.getActionTerm().getFunctor() + " to " + move.getActionTerm().getTerm(0) + ", " + move.getActionTerm().getTerm(1) + ", " + move.getActionTerm().getTerm(2));
        String s = bulb.SendReceive("move" + "(" + x + "," + y + "," + z + ")");
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
