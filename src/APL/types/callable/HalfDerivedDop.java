package APL.types.callable;

import APL.types.*;

public class HalfDerivedDop extends Md1 {
  public final Value g;
  public final Md2 op;
  
  public HalfDerivedDop(Value g, Md2 op) {
    this.g = g;
    this.op = op;
  }
  
  public Value derive(Value f) {
    return new DerivedDop(f, g, op);
  }
  
  public String repr() {
    String gs = g.toString();
    if (!(g instanceof Arr) && gs.length() != 1) gs = "("+gs+")";
    return op.repr()+gs;
  }
  public boolean eq(Value o) { // reminder: Ddop has its own HalfDerivedDdop
    if (!(o instanceof HalfDerivedDop)) return false;
    HalfDerivedDop that = (HalfDerivedDop) o;
    return g.eq(that.g) && op.eq(that.op);
  }
  public int hashCode() {
    return 31*g.hashCode() + op.hashCode();
  }
}