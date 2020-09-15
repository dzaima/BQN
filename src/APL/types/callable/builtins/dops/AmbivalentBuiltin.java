package APL.types.callable.builtins.dops;

import APL.types.Value;
import APL.types.callable.Md2Derv;
import APL.types.callable.builtins.Md2Builtin;

public class AmbivalentBuiltin extends Md2Builtin {
  public String repr() {
    return "⊘";
  }
  
  public Value call(Value f, Value g, Value x, Md2Derv derv) {
    return f.call(x);
  }
  public Value callInv(Value f, Value g, Value x) {
    return f.callInv(x);
  }
  
  public Value call(Value f, Value g, Value w, Value x, Md2Derv derv) {
    return g.call(w, x);
  }
  public Value callInvW(Value f, Value g, Value w, Value x) {
    return g.callInvW(w, x);
  }
  public Value callInvX(Value f, Value g, Value w, Value x) {
    return g.callInvX(w, x);
  }
}