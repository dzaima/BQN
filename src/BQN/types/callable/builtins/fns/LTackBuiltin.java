package BQN.types.callable.builtins.fns;

import BQN.errors.DomainError;
import BQN.tools.FmtInfo;
import BQN.types.Value;
import BQN.types.callable.builtins.FnBuiltin;

public class LTackBuiltin extends FnBuiltin {
  public String ln(FmtInfo f) { return "⊣"; }
  
  public Value call(Value x) { return x; }
  public Value call(Value w, Value x) { return w; }
  
  public Value callInv(Value x) {
    return x;
  }
  public Value callInvX(Value w, Value x) {
    if (w.eq(x)) return w;
    throw new DomainError("⊣⁼: expected 𝕨 and 𝕩 to be equal", this);
  }
  public Value callInvW(Value w, Value x) {
    return w;
  }
}