package dzaima;

import java.util.*;

public abstract class ArrTk extends Tk {
  List<Tk> tks;
  
  public ArrTk(List<Tk> tks) {
    this.tks = tks;
  }
  
  public String ct() {
    return join(tks);
  }
  public static String join(List<Tk> tks) {
    StringBuilder r = new StringBuilder();
    for (Tk c : tks) r.append(c.src());
    return r.toString();
  }
  
  public abstract ArrTk mod(Vec<Tk> n);
}
