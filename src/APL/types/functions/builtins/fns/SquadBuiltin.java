package APL.types.functions.builtins.fns;

import APL.errors.DomainError;
import APL.types.*;
import APL.types.functions.Builtin;

public class SquadBuiltin extends Builtin {
  @Override public String repr() {
    return "⌷";
  }
  
  public Value call(Value w) {
    if (w instanceof Arr) return w;
    if (w instanceof APLMap) return ((APLMap) w).kvPair();
    throw new DomainError("⍵ not array nor map", this, w);
  }
  
  public Value call(Value a, Value w) {
    return w.at(a.asIntVec());
  }
}