import java.util.*;
import java.lang.*;

public class PerceptFilter{
  //for percepts without args
  public static boolean filter(String functor){
    return true;
  }

  //for percepts with args
  public static boolean filter(String functor, String[] newTerms, List<String[]> oldTerms){
    switch (functor) {
      case "position":
/* FILTER POSITION */
        Double[] newXYZ = new Double[newTerms.length];
        for(int i=0;i<newXYZ.length;i++){
          newXYZ[i] = Double.parseDouble(newTerms[i]);
        }

        List<Double[]> oldXYZ = new ArrayList<Double[]>();
        for(String[] strings : oldTerms){
          oldXYZ.add(new Double[strings.length]);
          for(int i=0;i<strings.length;i++){
            oldXYZ.get(oldXYZ.size()-1)[i] = Double.parseDouble(strings[i]);//set last element added to oldXYZ
          }
        }

        for(Double[] xyz : oldXYZ){
          if(Math.sqrt(Math.pow(newXYZ[0]-xyz[0],2)+Math.pow(newXYZ[1]-xyz[1],2)+Math.pow(newXYZ[2]-xyz[2],2)) < 10) return false;
        }
        System.out.println("positionReceived");
        return true;
/* IF NOT IN THE LIST, DON'T FILTER */
      default:
        return true;
    }
  }
}
