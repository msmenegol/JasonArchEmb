import java.util.*;
import java.lang.*;

public class PerceptFilter{
  //for percepts without args
  /*
  public static boolean filter(String functor){
    switch (functor) {
      //ADD FUNTOR-ONLY CASES HERE
      default:
        return true;
    }
  }
*/
  //for percepts with args
  public static List<String[]> filter(String[] newPercept, List<String[]> oldPercepts){
    List<String[]> filteredPercepts = new ArrayList<String[]>();
    switch (newPercept[0]) {//switch on the functor of the percept
      //ADD OTHER PERCEPTS CASES HERE
      default:
        filteredPercepts.add(newPercept);
        return filteredPercepts;
    }
  }
}
