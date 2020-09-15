package APL.types.functions.builtins;

import APL.types.Value;
import APL.types.functions.Dop;

public abstract class DopBuiltin extends Dop {
  public boolean eq(Value o) {
    return this.getClass() == o.getClass();
  }
  
  public int hashCode() {
    return repr().hashCode();
  }
}
