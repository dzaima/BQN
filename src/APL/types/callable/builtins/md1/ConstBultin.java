package APL.types.callable.builtins.md1;

import APL.types.Value;
import APL.types.callable.DerivedMop;
import APL.types.callable.builtins.Md1Builtin;

public class ConstBultin extends Md1Builtin {
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