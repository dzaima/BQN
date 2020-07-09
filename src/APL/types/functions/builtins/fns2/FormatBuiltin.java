package APL.types.functions.builtins.fns2;

import APL.Main;
import APL.types.Value;
import APL.types.functions.Builtin;

public class FormatBuiltin extends Builtin {
  @Override public String repr() {
    return "⍕";
  }
  
  
  
  public Value call(Value w) {
    return Main.toAPL(w.toString());
  }
}