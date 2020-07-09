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
    Fun ff = g.asFun();
    return f.asFun().call(ff.call(w), ff.call(x));
  }
  
  public Value callInvW(Value f, Value g, Value w, Value x) {
    Fun ff = f.asFun();
    Fun gf = g.asFun();
    return gf.callInv(ff.callInvW(gf.call(w), x));
  }
  public Value callInvA(Value f, Value g, Value w, Value x) {
    Fun ff = f.asFun();
    Fun gf = g.asFun();
    return gf.callInv(ff.callInvA(w, gf.call(x)));
  }
  
}