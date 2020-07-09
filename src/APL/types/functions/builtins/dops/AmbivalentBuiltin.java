package APL.types.functions.builtins.dops;

import APL.types.Value;
import APL.types.functions.*;

public class AmbivalentBuiltin extends Dop {
  public Value call(Value f, Value g, Value x, DerivedDop derv) {
    return f.asFun().call(x);
  }
  public Value callInv(Value f, Value g, Value x) {
    return f.asFun().callInv(x);
  }
  
  public Value call(Value f, Value g, Value w, Value x, DerivedDop derv) {
    return g.asFun().call(w, x);
  }
  public Value callInvA(Value f, Value g, Value w, Value x) {
    return g.asFun().callInvA(w, x);
  }
  public Value callInvW(Value f, Value g, Value w, Value x) {
    return g.asFun().callInvW(w, x);
  }
  
  public String repr() {
    return "⊘";
  }
}