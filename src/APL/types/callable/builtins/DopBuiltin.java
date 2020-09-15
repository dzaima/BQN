package APL.types.callable.builtins;

import APL.types.*;

public abstract class DopBuiltin extends Dop {
  public boolean eq(Value o) {
    return this.getClass() == o.getClass();
  }
  
  public int hashCode() {
    return repr().hashCode();
  }
}
