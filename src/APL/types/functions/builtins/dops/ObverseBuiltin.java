package APL.types.functions.builtins.dops;

import APL.types.*;
import APL.types.functions.*;

public class ObverseBuiltin extends Dop {
  @Override public String repr() {
    return "⍫";
  }
  
  
  public Value call(Value f, Value g, Value x, DerivedDop derv) {
    Fun aaf = f.asFun();
    return aaf.call(x);
  }
  public Value call(Value f, Value g, Value w, Value x, DerivedDop derv) {
    Fun aaf = f.asFun();
    return aaf.call(w, x);
  }
  
  public Value callInv(Value f, Value g, Value x) {
    Fun wwf = g.asFun();
    return wwf.call(x);
  }
  public Value callInvW(Value f, Value g, Value w, Value x) {
    Fun wwf = g.asFun();
    return wwf.call(w, x);
  }
  
  public Value callInvA(Value f, Value g, Value w, Value x) { // fall-back to 𝔽
    Fun aaf = f.asFun();
    return aaf.callInvA(w, x);
  }
}