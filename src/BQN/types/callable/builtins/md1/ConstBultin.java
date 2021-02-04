package BQN.types.callable.builtins.md1;

import BQN.tools.FmtInfo;
import BQN.types.Value;
import BQN.types.callable.Md1Derv;
import BQN.types.callable.builtins.Md1Builtin;

public class ConstBultin extends Md1Builtin {
  public String ln(FmtInfo f) { return "˙"; }
  
  public Value call(Value f, Value x, Md1Derv derv) {
    return f;
  }
  public Value call(Value f, Value w, Value x, Md1Derv derv) {
    return f;
  }
}