package BQN.types.callable.builtins;

import BQN.tools.FmtInfo;
import BQN.types.*;

public abstract class FnBuiltin extends Fun {
  public boolean eq(Value o) {
    return this.getClass() == o.getClass();
  }
  
  public int hashCode() {
    return ln(FmtInfo.def).hashCode(); // probably could even pass null and it should work
  }
}