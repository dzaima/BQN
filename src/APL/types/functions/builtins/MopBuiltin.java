package APL.types.functions.builtins;

import APL.types.*;

public abstract class MopBuiltin extends Mop {
  public boolean eq(Value o) {
    return this.getClass() == o.getClass();
  }
  
  public int hashCode() {
    return repr().hashCode();
  }
}
