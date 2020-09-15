package APL.types.callable.builtins.mops;

import APL.types.Value;
import APL.types.callable.DerivedMop;
import APL.types.callable.builtins.MopBuiltin;

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