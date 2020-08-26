package APL.types.functions.builtins.dops;

import APL.types.Value;
import APL.types.functions.*;

public class AmbivalentBuiltin extends Dop {
  public Value call(Value f, Value g, Value x, DerivedDop derv) {
    return f.call(x);
  }
  public Value callInv(Value f, Value g, Value x) {
    return f.callInv(x);
  }
  
  public Value call(Value f, Value g, Value w, Value x, DerivedDop derv) {
    return g.call(w, x);
  }
  public Value callInvA(Value f, Value g, Value w, Value x) {
    return g.callInvA(w, x);
  }
  public Value callInvW(Value f, Value g, Value w, Value x) {
    return g.callInvW(w, x);
  }
  
  public String repr() {
    return "⊘";
  }
}