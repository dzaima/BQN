package APL.types.callable.builtins;

import APL.tools.FmtInfo;
import APL.types.*;

public abstract class FnBuiltin extends Fun {
  public boolean eq(Value o) {
    return this.getClass() == o.getClass();
  }
  
  public int hashCode() {
    return ln(FmtInfo.dbg).hashCode(); // probably could even pass null and it should work
  }
}