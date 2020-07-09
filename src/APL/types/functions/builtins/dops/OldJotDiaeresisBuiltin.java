package APL.types.functions.builtins.dops;

import APL.types.*;
import APL.types.functions.*;

public class OldJotDiaeresisBuiltin extends Dop {
  @Override public String repr() {
    return "⍤";
  }
  
  
  
  @Override
  public Value call(Value f, Value g, Value w, Value x, DerivedDop derv) {
    Fun aaf = f.asFun(); Fun wwf = g.asFun();
    return aaf.call(wwf.call(w, x));
  }
  
  public Value call(Value f, Value g, Value x, DerivedDop derv) {
    Fun aaf = f.asFun(); Fun wwf = g.asFun();
    return aaf.call(wwf.call(x));
  }
  
  public Value callInv(Value f, Value g, Value x) {
    Fun aaf = f.asFun(); Fun wwf = g.asFun();
    return aaf.call(wwf.call(x));
  }
  
  public Value callInvW(Value f, Value g, Value w, Value x) {
    Fun aaf = f.asFun(); Fun wwf = g.asFun();
    return wwf.callInvW(w, aaf.callInv(x));
  }
  
  public Value callInvA(Value f, Value g, Value w, Value x) {
    Fun aaf = f.asFun(); Fun wwf = g.asFun();
    return wwf.callInvA(aaf.callInv(w), x);
  }
}