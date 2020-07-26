package APL.types;

import APL.Scope;

public abstract class Settable extends Obj {
  
  public abstract Value get(Scope sc);
  public abstract void set(Value v, boolean update, Scope sc, Callable blame);
  
  public Obj getOrThis(Scope sc) {
    Value got = get(sc);
    if (got == null) return this;
    return got;
  }
}