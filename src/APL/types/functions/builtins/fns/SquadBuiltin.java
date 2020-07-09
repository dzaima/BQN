package APL.types.functions.builtins.fns;

import APL.errors.DomainError;
import APL.types.*;
import APL.types.functions.Builtin;

public class SquadBuiltin extends Builtin {
  @Override public String repr() {
    return "⌷";
  }
  
  public Value call(Value x) {
    if (x instanceof Arr) return x;
    if (x instanceof APLMap) return ((APLMap) x).kvPair();
    throw new DomainError("⌷: 𝕩 not array nor map", this, x);
  }
  
  public Value call(Value a, Value w) {
    return w.at(a.asIntVec());
  }
}