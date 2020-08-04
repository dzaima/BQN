package APL.types.mut;

import APL.Scope;
import APL.types.*;

public abstract class Settable extends Obj {
  public abstract Value get(Scope sc);
  public abstract void set(Value x, boolean update, Scope sc, Callable blame);
  public abstract boolean seth(Value x, Scope sc); // returns if was successful
}