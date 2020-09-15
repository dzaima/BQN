package APL.types.functions.builtins.dops;

import APL.types.Value;
import APL.types.functions.DerivedDop;
import APL.types.functions.builtins.DopBuiltin;

public class OldJotDiaeresisBuiltin extends DopBuiltin {
  @Override public String repr() {
    return "⍤";
  }
  
  
  
  @Override
  public Value call(Value f, Value g, Value w, Value x, DerivedDop derv) {
    return f.call(g.call(w, x));
  }
  
  public Value call(Value f, Value g, Value x, DerivedDop derv) {
    return f.call(g.call(x));
  }
  
  public Value callInv(Value f, Value g, Value x) {
    return f.call(g.call(x));
  }
  
  public Value callInvX(Value f, Value g, Value w, Value x) {
    return g.callInvX(w, f.callInv(x));
  }
  
  public Value callInvW(Value f, Value g, Value w, Value x) {
    return g.callInvW(f.callInv(w), x);
  }
}