package BQN.types.callable.builtins;

import BQN.tools.FmtInfo;
import BQN.types.*;

public abstract class Md1Builtin extends Md1 {
  public boolean eq(Value o) {
    return this.getClass() == o.getClass();
  }
  
  public int hashCode() {
    return ln(FmtInfo.def).hashCode();
  }
}
