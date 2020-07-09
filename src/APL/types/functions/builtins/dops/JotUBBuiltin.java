package APL.types.functions.builtins.dops;

import APL.types.*;
import APL.types.functions.*;

public class JotUBBuiltin extends Dop {
  @Override public String repr() {
    return "⍛";
  }
  
  public Value call(Value f, Value g, Value w, Value x, DerivedDop derv) {
    Fun ff = f.asFun(); Fun gf = g.asFun();
    return gf.call(ff.call(w), x);
  }
  
  public Value callInvW(Value f, Value g, Value w, Value x) {
    Fun ff = f.asFun(); Fun gf = g.asFun();
    return gf.callInvW(ff.call(w), x);
  }
  
  public Value callInvA(Value f, Value g, Value w, Value x) {
    Fun ff = f.asFun(); Fun gf = g.asFun();
    return ff.callInv(gf.callInvA(w, x));
  }
}