package APL.types.functions.builtins.mops;

import APL.types.Value;
import APL.types.functions.*;

public class ConstBultin extends Mop {
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