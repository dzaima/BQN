package APL.types.callable.builtins.fns;

import APL.Main;
import APL.types.Value;
import APL.types.callable.builtins.FnBuiltin;

public class FormatBuiltin extends FnBuiltin {
  @Override public String repr() {
    return "⍕";
  }
  
  
  
  public Value call(Value x) {
    return Main.toAPL(x.toString());
  }
}