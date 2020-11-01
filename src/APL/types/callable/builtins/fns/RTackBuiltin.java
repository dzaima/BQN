package APL.types.callable.builtins.fns;

import APL.errors.DomainError;
import APL.tools.FmtInfo;
import APL.types.Value;
import APL.types.callable.builtins.FnBuiltin;

public class RTackBuiltin extends FnBuiltin {
  public String ln(FmtInfo f) { return "⊢"; }
  
  public Value call(Value x) { return x; }
  public Value call(Value w, Value x) { return x; }
  
  public Value callInv(Value x) {
    return x;
  }
  public Value callInvX(Value w, Value x) {
    return x;
  }
  public Value callInvW(Value w, Value x) {
    throw new DomainError("⊣˜⁼ is impossible", this);
  }
}