import java.lang.Math;

public class P3d {
  private double x;
  private double y;
  private double z;

  public P3d(){
    this.x = Double.NaN;
    this.y = Double.NaN;
    this.z = Double.NaN;
  }

  public P3d(double a, double b, double c){
    this.x = a;
    this.y = b;
    this.z = c;
  }

  public void set(double v1, double v2, double v3){
    this.x = v1;
    this.y = v2;
    this.z = v3;
  }
  public void setX(double val){
    this.x = val;
  }
  public void setY(double val){
    this.y = val;
  }
  public void setZ(double val){
    this.z = val;
  }

  public double getX(){
    return this.x;
  }
  public double getY(){
    return this.y;
  }
  public double getZ(){
    return this.z;
  }

  public double dist(double a, double b, double c){
    return Math.sqrt(Math.pow(this.x - a, 2) + Math.pow(this.y - b, 2) + Math.pow(this.z - c, 2));
  }

}
