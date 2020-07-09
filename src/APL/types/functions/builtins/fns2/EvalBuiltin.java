package APL.types.functions.builtins.fns2;

import APL.*;
import APL.types.Value;
import APL.types.functions.Builtin;

public class EvalBuiltin extends Builtin {
  @Override public String repr() {
    return "⍎";
  }
  public final Scope sc;
  public EvalBuiltin(Scope sc) {
    this.sc = sc;
  }
  
  public Value call(Value x) {
    return Main.exec(x.asString(), sc);
  }
}