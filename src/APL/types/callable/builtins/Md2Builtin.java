package APL.types.callable.builtins;

import APL.tools.FmtInfo;
import APL.types.*;

public abstract class Md2Builtin extends Md2 {
  public boolean eq(Value o) {
    return this.getClass() == o.getClass();
  }
  
  public int hashCode() {
    return ln(FmtInfo.dbg).hashCode();
  }
}
