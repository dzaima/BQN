package APL.types.functions.builtins.fns;

import APL.*;
import APL.types.*;
import APL.types.functions.*;

public class EvalBuiltin extends Builtin {
  @Override public String repr() {
    return "⍎";
  }
  
  public EvalBuiltin(Scope sc) {
    super(sc);
  }
  
  public Value call(Value w) {
    return Main.exec(w.asString(), sc);
  }
}