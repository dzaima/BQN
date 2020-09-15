package APL.types.functions.builtins.fns2;

import APL.Main;
import APL.types.Value;
import APL.types.functions.builtins.FnBuiltin;

public class FormatBuiltin extends FnBuiltin {
  @Override public String repr() {
    return "⍕";
  }
  
  
  
  public Value call(Value x) {
    return Main.toAPL(x.toString());
  }
}