package APL.types.functions.builtins.fns2;

import APL.errors.NYIError;
import APL.types.Value;
import APL.types.functions.Builtin;
import APL.types.functions.builtins.fns.OldCatBuiltin;

public class JoinBuiltin extends Builtin {
  public String repr() {
    return "∾";
  }
  
  public Value call(Value w) {
    throw new NYIError("TODO monadic ∾", this, w);
  }
  
  public Value call(Value a, Value w) {
    return OldCatBuiltin.cat(a, w, 0, this);
  }
}
