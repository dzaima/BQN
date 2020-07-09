package APL.types.functions.builtins.dops;

import APL.types.*;
import APL.types.functions.*;

public class JotUBBuiltin extends Dop {
  @Override public String repr() {
    return "⍛";
  }
  
  public Value call(Value f, Value g, Value w, Value x, DerivedDop derv) {
    Fun aaf = f.asFun(); Fun wwf = g.asFun();
    return wwf.call(aaf.call(w), x);
  }
  
  public Value callInvW(Value f, Value g, Value w, Value x) {
    Fun aaf = f.asFun(); Fun wwf = g.asFun();
    return wwf.callInvW(aaf.call(w), x);
  }
  
  public Value callInvA(Value f, Value g, Value w, Value x) {
    Fun aaf = f.asFun(); Fun wwf = g.asFun();
    return aaf.callInv(wwf.callInvA(w, x));
  }
}