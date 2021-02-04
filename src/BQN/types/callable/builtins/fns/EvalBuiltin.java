package BQN.types.callable.builtins.fns;

import BQN.*;
import BQN.tools.FmtInfo;
import BQN.types.Value;
import BQN.types.callable.builtins.FnBuiltin;

public class EvalBuiltin extends FnBuiltin {
  public String ln(FmtInfo f) { return "⍎"; }
  
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