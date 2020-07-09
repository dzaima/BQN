package APL.types.functions;

import APL.types.*;

public class HalfDerivedDop extends Mop {
  public final Value g;
  public final Dop op;
  
  public HalfDerivedDop(Value g, Dop op) {
    this.g = g;
    this.op = op;
  }
  
  public Fun derive(Value f) {
    return new DerivedDop(f, g, op);
  }
  
  public String repr() {
    String gs = g.toString();
    if (!(g instanceof Arr) && gs.length() != 1) gs = "("+gs+")";
    return op.repr()+gs;
  }
}