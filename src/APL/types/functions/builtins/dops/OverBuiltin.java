package APL.types.functions.builtins.dops;

import APL.types.*;
import APL.types.functions.*;

public class OverBuiltin extends Dop {
  @Override public String repr() {
    return "○";
  }
  
  public Value call(Value f, Value g, Value x, DerivedDop derv) {
    return f.asFun().call(g.asFun().call(x));
  }
  public Value call(Value f, Value g, Value w, Value x, DerivedDop derv) {
    Fun wwf = g.asFun();
    return f.asFun().call(wwf.call(w), wwf.call(x));
  }
  
  public Value callInvW(Value aa, Value ww, Value w, Value x) {
    Fun f = aa.asFun();
    Fun g = ww.asFun();
    return g.callInv(f.callInvW(g.call(w), x));
  }
  public Value callInvA(Value aa, Value ww, Value w, Value x) {
    Fun f = aa.asFun();
    Fun g = ww.asFun();
    return g.callInv(f.callInvA(w, g.call(x)));
  }
  
}