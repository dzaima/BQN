package APL.types.functions.builtins;

import APL.types.*;

public abstract class AbstractSet extends Callable {
  
  public abstract Value call(Obj w, Value x, boolean update);
  
  
  
  public Fun asFun() { throw new AssertionError("this object shouldn't be accessible"); }
  public boolean notIdentity() { throw new AssertionError("this object shouldn't be accessible"); }
}