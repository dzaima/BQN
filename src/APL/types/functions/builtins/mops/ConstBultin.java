package APL.types.functions.builtins.mops;

import APL.types.Value;
import APL.types.functions.DerivedMop;
import APL.types.functions.builtins.MopBuiltin;

public class ConstBultin extends MopBuiltin {
  public String repr() {
    return "˙";
  }
  
  public Value call(Value f, Value x, DerivedMop derv) {
    return f;
  }
  public Value call(Value f, Value w, Value x, DerivedMop derv) {
    return f;
  }
}