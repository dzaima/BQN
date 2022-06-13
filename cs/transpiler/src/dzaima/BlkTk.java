package dzaima;

import java.util.*;

public class BlkTk extends ArrTk {
  public BlkTk(List<Tk> tks) {
    super(tks);
  }
  
  public ArrTk mod(Vec<Tk> n) {
    return new BlkTk(n);
  }
  
  public String src() {
    return "{"+ct()+"}";
  }
}
