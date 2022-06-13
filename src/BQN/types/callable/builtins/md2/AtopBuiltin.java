package BQN.types.callable.builtins.md2;

import BQN.tools.FmtInfo;
import BQN.types.*;
import BQN.types.callable.Md2Derv;
import BQN.types.callable.builtins.Md2Builtin;

public class AtopBuiltin extends Md2Builtin {
  public String ln(FmtInfo f) { return "∘"; }
  
  public Value call(Value f, Value g, Value x, Md2Derv derv) {
    return f.call(g.call(x));
  }
  public Value call(Value f, Value g, Value w, Value x, Md2Derv derv) {
    return f.call(g.call(w, x));
  }
  
  
  public Value callInv(Value f, Value g, Value x) {
    return g.callInv(f.callInv(x));
  }
  
  public Value callInvX(Value f, Value g, Value w, Value x) {
    return g.callInvX(w, f.callInv(x));
  }
  public Value callInvW(Value f, Value g, Value w, Value x) {
    return g.callInvW(f.callInv(w), x);
  }
  
  public Value under(Value f, Value g, Value o, Value x, Md2Derv derv) {
    return g.under(new Fun(/*AA Value f,Value o*/) { public String ln(FmtInfo fi) { return f.ln(fi); }
      public Value call(Value x) {
        return f.under(o, x);
      }
    }, x);
  }
}