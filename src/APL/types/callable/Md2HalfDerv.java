package APL.types.callable;

import APL.tools.FmtInfo;
import APL.types.*;

public class Md2HalfDerv extends Md1 {
  public final Value g;
  public final Md2 op;
  
  public Md2HalfDerv(Value g, Md2 op) {
    this.g = g;
    this.op = op;
  }
  
  public Value derive(Value f) {
    return op.derive(f, g);
  }
  
  public String ln(FmtInfo f) {
    return op.ln(f)+"("+g.ln(f)+")";
  }
  
  public boolean eq(Value o) { // reminder: Md2Block has its own Md2HalfDerv
    if (!(o instanceof Md2HalfDerv)) return false;
    Md2HalfDerv that = (Md2HalfDerv) o;
    return g.eq(that.g) && op.eq(that.op);
  }
  public int hashCode() {
    return 31*g.hashCode() + op.hashCode();
  }
}