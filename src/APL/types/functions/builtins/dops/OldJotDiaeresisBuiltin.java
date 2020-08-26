package APL.types.functions.builtins.dops;

import APL.types.*;
import APL.types.functions.*;

public class OldJotDiaeresisBuiltin extends Dop {
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
  
  public Value callInvW(Value f, Value g, Value w, Value x) {
    return g.callInvW(w, f.callInv(x));
  }
  
  public Value callInvA(Value f, Value g, Value w, Value x) {
    return g.callInvA(f.callInv(w), x);
  }
}