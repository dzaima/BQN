package APL.types.functions.builtins.fns;

import APL.Scope;
import APL.errors.DomainError;
import APL.types.*;
import APL.types.functions.Builtin;

public class SquadBuiltin extends Builtin {
  public SquadBuiltin(Scope sc) {
    super("⌷", 0x011, sc);
  }
  
  public Obj call(Value w) {
    if (w instanceof Arr) return w;
    if (w instanceof APLMap) return ((APLMap) w).toArr();
    throw new DomainError("⍵ not array nor map", this, w);
  }
  
  public Obj call(Value a, Value w) {
    return w.at(a.toIntArr(this), this);
  }
}