package APL.types.functions.builtins.dops;

import APL.types.*;
import APL.types.functions.*;

public class ObverseBuiltin extends Dop {
  @Override public String repr() {
    return "⍫";
  }
  
  
  public Value call(Value aa, Value ww, Value w, DerivedDop derv) {
    Fun aaf = aa.asFun();
    return aaf.call(w);
  }
  public Value call(Value aa, Value ww, Value a, Value w, DerivedDop derv) {
    Fun aaf = aa.asFun();
    return aaf.call(a, w);
  }
  
  public Value callInv(Value aa, Value ww, Value w) {
    Fun wwf = ww.asFun();
    return wwf.call(w);
  }
  public Value callInvW(Value aa, Value ww, Value a, Value w) {
    Fun wwf = ww.asFun();
    return wwf.call(a, w);
  }
  
  public Value callInvA(Value aa, Value ww, Value a, Value w) { // fall-back to ⍶
    Fun aaf = aa.asFun();
    return aaf.callInvA(a, w);
  }
}