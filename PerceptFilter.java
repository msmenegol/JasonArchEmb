import java.util.*;
import java.lang.*;

public class PerceptFilter{
  public static boolean filter(String functor, String... terms){
    switch (functor) {
      case "position":
        String[] newTerms = terms[0].split("[,]");
        String[] oldTerms = terms[1].split("[,]");
        Double[] x = {Double.parseDouble(newTerms[0]), Double.parseDouble(oldTerms[0])};
        Double[] y = {Double.parseDouble(newTerms[1]), Double.parseDouble(oldTerms[1])};
        Double[] z = {Double.parseDouble(newTerms[2]), Double.parseDouble(oldTerms[2])};
        if(Math.sqrt(Math.pow(x[0]-x[1],2)+Math.pow(y[0]-y[1],2)+Math.pow(z[0]-z[1],2)) < 10) return false;
        else return true;
      default:
        return true;
    }
  }
}
