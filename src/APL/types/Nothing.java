package APL.types;

import APL.errors.SyntaxError;
import APL.tools.FmtInfo;
import APL.types.arrs.ChrArr;

public class Nothing extends Primitive {
  public static final Nothing inst = new Nothing();
  
  
  SyntaxError used() {
    return new SyntaxError("didn't expect ·");
  }
  
  
  
  public String asString() { throw used(); }
  public Value safePrototype() { throw used(); }
  public Value ofShape(int[] sh) { throw used(); }
  
  
  public Value pretty(FmtInfo f) { return new ChrArr("·"); }
  public String    ln(FmtInfo f) { return "·";  }
  
  
  public int hashCode() { return 0; }
  public boolean eq(Value o) {
    return o instanceof Nothing;
  }
}