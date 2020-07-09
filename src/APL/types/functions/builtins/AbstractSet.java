package APL.types.functions.builtins;

import APL.Type;
import APL.types.*;

public abstract class AbstractSet extends Callable {
  
  public abstract Value call(Obj w, Value x, boolean update);
  
  @Override
  public Type type() {
    return Type.set;
  }
  
  
  public Fun asFun() { throw new AssertionError("this object shouldn't be accessible"); }
  public boolean notIdentity() { throw new AssertionError("this object shouldn't be accessible"); }
}