package APL.types.functions.builtins.dops;

import APL.types.Value;
import APL.types.functions.*;

public class ObverseBuiltin extends Dop {
  @Override public String repr() {
    return "⍫";
  }
  
  
  public Value call(Value f, Value g, Value x, DerivedDop derv) {
    return f.call(x);
  }
  public Value call(Value f, Value g, Value w, Value x, DerivedDop derv) {
    return f.call(w, x);
  }
  
  public Value callInv(Value f, Value g, Value x) {
    return g.call(x);
  }
  public Value callInvW(Value f, Value g, Value w, Value x) {
    return g.call(w, x);
  }
  
  public Value callInvA(Value f, Value g, Value w, Value x) { // fall-back to 𝔽
    return f.callInvA(w, x);
  }
}