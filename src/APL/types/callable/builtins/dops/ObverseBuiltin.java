package APL.types.callable.builtins.dops;

import APL.types.Value;
import APL.types.callable.DerivedDop;
import APL.types.callable.builtins.DopBuiltin;

public class ObverseBuiltin extends DopBuiltin {
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
  public Value callInvX(Value f, Value g, Value w, Value x) {
    return g.call(w, x);
  }
  
  public Value callInvW(Value f, Value g, Value w, Value x) { // fall-back to 𝔽
    return f.callInvW(w, x);
  }
}