package BQN.types;

import BQN.errors.SyntaxError;
import BQN.tools.FmtInfo;
import BQN.types.arrs.ChrArr;

public class Nothing extends Primitive {
  public static final Nothing inst = new Nothing();
  
  
  SyntaxError used() {
    return new SyntaxError("didn't expect ·");
  }
  
  
  
  public String asString() { throw used(); }
  public Value fItemS() { throw used(); }
  public Value ofShape(int[] sh) { throw used(); }
  
  
  public Value pretty(FmtInfo f) { return new ChrArr("·"); }
  public String    ln(FmtInfo f) { return "·";  }
  
  
  public int hashCode() { return 0; }
  public boolean eq(Value o) {
    return o instanceof Nothing;
  }
}