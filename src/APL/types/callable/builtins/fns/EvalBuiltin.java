package APL.types.callable.builtins.fns2;

import APL.*;
import APL.types.Value;
import APL.types.callable.builtins.FnBuiltin;

public class EvalBuiltin extends FnBuiltin {
  @Override public String repr() {
    return "⍎";
  }
  public final Scope sc;
  public EvalBuiltin(Scope sc) {
    this.sc = sc;
  }
  
  public Value call(Value x) {
    return Main.exec(x.asString(), sc, null);
  }
  
  public Value call(Value w, Value x) {
    return Main.exec(x.asString(), sc, w.values());
  }
}