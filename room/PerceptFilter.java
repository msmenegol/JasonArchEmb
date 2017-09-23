import java.util.*;
import java.lang.*;

public class PerceptFilter{
  //for percepts with args
  public static List<String[]> filter(String[] newPercept, List<String[]> oldPercepts){
    List<String[]> filteredPercepts = new ArrayList<String[]>();
    switch (newPercept[0]) {//switch on the functor of the percept
      //ADD OTHER PERCEPTS CASES HERE
      case "sunLight":
        //System.out.println(newTerms[0] + "   " + oldTerms.get(0)[0]);
        for(int i = 0; i<oldPercepts.size(); i++){
          if(oldPercepts.get(i)[0].equals("sunLight")){
            if(Math.abs(Double.parseDouble(oldPercepts.get(i)[1]) - Double.parseDouble(newPercept[1])) < 30){//oly detect big changes in sunlight
              //System.out.println("new sunLight is " + newPercept[1]);
              filteredPercepts.add(oldPercepts.get(i));
              return filteredPercepts;
            }
          }
        }
        filteredPercepts.add(newPercept);
      default:
        filteredPercepts.add(newPercept);
    }
    return filteredPercepts;
  }
}
