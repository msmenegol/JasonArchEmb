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
      default:
        return true;
    }
  }
}
