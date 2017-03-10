import java.util.*;
import java.lang.*;

public class PerceptFilter{
  //for percepts without args
  public static boolean filter(String functor){
    switch (functor) {
      //ADD FUNTOR-ONLY CASES HERE
      default:
        return true;
    }
  }

  //for percepts with args
  public static boolean filter(String functor, String[] newTerms, List<String[]> oldTerms){
    switch (functor) {
      //ADD OTHER PERCEPTS CASES HERE
      case "pot":
        //System.out.println(newTerms[0] + "   " + oldTerms.get(0)[0]);
        if(Math.abs(Double.parseDouble(newTerms[0]) - Double.parseDouble(oldTerms.get(0)[0])) > 30 ) {
          return true;
        } else return false;
      default:
        return true;
    }
  }
}
