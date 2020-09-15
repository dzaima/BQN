package APL.types.functions.builtins.fns2;

import APL.errors.DomainError;
import APL.types.Value;
import APL.types.functions.builtins.FnBuiltin;

public class RTackBuiltin extends FnBuiltin {
  @Override public String repr() {
    return "⊢";
  }
  
  
  
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