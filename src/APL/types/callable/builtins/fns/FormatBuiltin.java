package APL.types.callable.builtins.fns;

import APL.types.Value;
import APL.types.arrs.ChrArr;
import APL.types.callable.builtins.FnBuiltin;

public class FormatBuiltin extends FnBuiltin {
  @Override public String repr() {
    return "⍕";
  }
  
  
  
  public Value call(Value x) {
    return new ChrArr(x.repr());
  }
}