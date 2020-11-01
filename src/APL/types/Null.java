package APL.types;

import APL.tools.FmtInfo;
import APL.types.arrs.ChrArr;

public class Null extends Primitive {
  public static final Null NULL = new Null();
  private Null() { }
  
  
  
  public boolean eq(Value o) {
    return o==NULL;
  }
  public int hashCode() {
    return 387678968; // random yay
  }
  
  public Value pretty(FmtInfo f) { return new ChrArr("•null"); }
  public String ln(FmtInfo f) { return "•null"; }
}