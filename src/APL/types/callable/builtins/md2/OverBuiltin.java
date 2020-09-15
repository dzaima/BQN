package APL.types.callable.builtins.md2;

import APL.types.Value;
import APL.types.callable.Md2Derv;
import APL.types.callable.builtins.Md2Builtin;

public class OverBuiltin extends Md2Builtin {
  @Override public String repr() {
    return "○";
  }
  
  public Value call(Value f, Value g, Value x, Md2Derv derv) {
    return f.call(g.call(x));
  }
  public Value call(Value f, Value g, Value w, Value x, Md2Derv derv) {
    return f.call(g.call(w), g.call(x));
  }
  
  public Value callInvX(Value f, Value g, Value w, Value x) {
    return g.callInv(f.callInvX(g.call(w), x));
  }
  public Value callInvW(Value f, Value g, Value w, Value x) {
    return g.callInv(f.callInvW(w, g.call(x)));
  }
  
}