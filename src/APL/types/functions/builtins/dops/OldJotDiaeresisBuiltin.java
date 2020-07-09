package APL.types.functions.builtins.dops;

import APL.types.*;
import APL.types.functions.*;

public class OldJotDiaeresisBuiltin extends Dop {
  @Override public String repr() {
    return "⍤";
  }
  
  
  
  @Override
  public Value call(Value f, Value g, Value w, Value x, DerivedDop derv) {
    Fun ff = f.asFun(); Fun gf = g.asFun();
    return ff.call(gf.call(w, x));
  }
  
  public Value call(Value f, Value g, Value x, DerivedDop derv) {
    Fun ff = f.asFun(); Fun gf = g.asFun();
    return ff.call(gf.call(x));
  }
  
  public Value callInv(Value f, Value g, Value x) {
    Fun ff = f.asFun(); Fun gf = g.asFun();
    return ff.call(gf.call(x));
  }
  
  public Value callInvW(Value f, Value g, Value w, Value x) {
    Fun ff = f.asFun(); Fun gf = g.asFun();
    return gf.callInvW(w, ff.callInv(x));
  }
  
  public Value callInvA(Value f, Value g, Value w, Value x) {
    Fun ff = f.asFun(); Fun gf = g.asFun();
    return gf.callInvA(ff.callInv(w), x);
  }
}