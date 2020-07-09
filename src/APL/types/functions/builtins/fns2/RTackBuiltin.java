package APL.types.functions.builtins.fns2;

import APL.errors.DomainError;
import APL.types.Value;
import APL.types.functions.Builtin;

public class RTackBuiltin extends Builtin {
  @Override public String repr() {
    return "⊢";
  }
  
  
  
  public Value call(Value x) { return x; }
  public Value call(Value w, Value x) { return x; }
  
  public Value callInv(Value x) {
    return x;
  }
  public Value callInvW(Value a, Value w) {
    return w;
  }
  public Value callInvA(Value a, Value w) {
    throw new DomainError("⊣˜⁼ is impossible", this);
  }
}