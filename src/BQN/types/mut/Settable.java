package BQN.types.mut;

import BQN.Scope;
import BQN.errors.SyntaxError;
import BQN.types.*;

public abstract class Settable extends Obj {
  public abstract Value get(Scope sc);
  public abstract void set(Value x, boolean update, Scope sc, Callable blame);
  public abstract boolean seth(Value x, Scope sc); // returns if was successful
  
  public String name(Scope sc) {
    throw new SyntaxError("Expected a name");
  }
  protected boolean hasName() {
    return false;
  }
}