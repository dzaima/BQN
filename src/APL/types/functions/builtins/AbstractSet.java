package APL.types.functions.builtins;

import APL.Type;
import APL.types.*;

public abstract class AbstractSet extends Callable {
  public AbstractSet() {
    super(null);
  }
  
  public abstract Value callObj(Obj a, Value w, boolean update);
  
  @Override
  public Type type() {
    return Type.set;
  }
  
  public Fun asFun() {
    throw new AssertionError("this object shouldn't be accessible");
  }
}