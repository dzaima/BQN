package APL.types.functions.builtins.dops;

import APL.types.*;
import APL.types.functions.*;

public class OverBuiltin extends Dop {
  @Override public String repr() {
    return "○";
  }
  
  public Value call(Value aa, Value ww, Value w, DerivedDop derv) {
    return aa.asFun().call(ww.asFun().call(w));
  }
  public Value call(Value aa, Value ww, Value a, Value w, DerivedDop derv) {
    Fun wwf = ww.asFun();
    return aa.asFun().call(wwf.call(a), wwf.call(w));
  }
  
  // public Value callInvW(Obj aa, Obj ww, Value a, Value w) { // +TODO uncomment & fix
  //   Fun f = isFn(aa, '⍶');
  //   Fun g = isFn(ww, '⍹');
  //   return g.callInv(f.callInvW(g.call(a), w));
  // }
  // public Value callInvA(Obj aa, Obj ww, Value a, Value w) {
  //   Fun f = isFn(aa, '⍶');
  //   Fun g = isFn(ww, '⍹');
  //   return g.callInv(f.callInvA(a, g.call(w)));
  // }
  
}