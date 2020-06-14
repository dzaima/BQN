package APL.types.functions.builtins.dops;

import APL.types.*;
import APL.types.functions.*;

public class JotUBBuiltin extends Dop {
  @Override public String repr() {
    return "⍛";
  }
  
  public Value call(Value aa, Value ww, Value a, Value w, DerivedDop derv) {
    Fun aaf = isFn(aa, '⍶'); Fun wwf = isFn(ww, '⍹');
    return wwf.call(aaf.call(a), w);
  }
  
  public Value callInvW(Value aa, Value ww, Value a, Value w) {
    Fun aaf = isFn(aa, '⍶'); Fun wwf = isFn(ww, '⍹');
    return wwf.callInvW(aaf.call(a), w);
  }
  
  public Value callInvA(Value aa, Value ww, Value a, Value w) {
    Fun aaf = isFn(aa, '⍶'); Fun wwf = isFn(ww, '⍹');
    return aaf.callInv(wwf.callInvA(a, w));
  }
}