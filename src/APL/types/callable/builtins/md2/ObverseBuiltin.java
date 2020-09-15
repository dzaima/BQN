package APL.types.callable.builtins.md2;

import APL.types.Value;
import APL.types.callable.Md2Derv;
import APL.types.callable.builtins.Md2Builtin;

public class ObverseBuiltin extends Md2Builtin {
  @Override public String repr() {
    return "⍫";
  }
  
  
  public Value call(Value f, Value g, Value x, Md2Derv derv) {
    return f.call(x);
  }
  public Value call(Value f, Value g, Value w, Value x, Md2Derv derv) {
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