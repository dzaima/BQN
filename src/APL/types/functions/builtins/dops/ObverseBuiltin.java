package APL.types.functions.builtins.dops;

import APL.types.*;
import APL.types.functions.*;

public class ObverseBuiltin extends Dop {
  @Override public String repr() {
    return "⍫";
  }
  
  
  public Value call(Value f, Value g, Value x, DerivedDop derv) {
    Fun ff = f.asFun();
    return ff.call(x);
  }
  public Value call(Value f, Value g, Value w, Value x, DerivedDop derv) {
    Fun ff = f.asFun();
    return ff.call(w, x);
  }
  
  public Value callInv(Value f, Value g, Value x) {
    Fun gf = g.asFun();
    return gf.call(x);
  }
  public Value callInvW(Value f, Value g, Value w, Value x) {
    Fun gf = g.asFun();
    return gf.call(w, x);
  }
  
  public Value callInvA(Value f, Value g, Value w, Value x) { // fall-back to 𝔽
    Fun gf = f.asFun();
    return gf.callInvA(w, x);
  }
}