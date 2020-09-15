package APL.types.functions.builtins.dops;

import APL.types.Value;
import APL.types.functions.DerivedDop;
import APL.types.functions.builtins.DopBuiltin;

public class AmbivalentBuiltin extends DopBuiltin {
  public String repr() {
    return "⊘";
  }
  
  public Value call(Value f, Value g, Value x, DerivedDop derv) {
    return f.call(x);
  }
  public Value callInv(Value f, Value g, Value x) {
    return f.callInv(x);
  }
  
  public Value call(Value f, Value g, Value w, Value x, DerivedDop derv) {
    return g.call(w, x);
  }
  public Value callInvW(Value f, Value g, Value w, Value x) {
    return g.callInvW(w, x);
  }
  public Value callInvX(Value f, Value g, Value w, Value x) {
    return g.callInvX(w, x);
  }
}