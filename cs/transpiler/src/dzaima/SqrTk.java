package dzaima;

import java.util.*;

public class SqrTk extends ArrTk {
  public SqrTk(List<Tk> tks) {
    super(tks);
  }
  
  public String src() {
    return "["+ct()+"]";
  }
  
  public ArrTk mod(Vec<Tk> n) {
    return new SqrTk(n);
  }
}
