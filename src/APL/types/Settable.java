package APL.types;

import APL.errors.ValueError;

public abstract class Settable extends Obj {
  final Value v;
  protected Settable(Value v) {
    this.v = v;
  }
  public abstract void set(Value v, boolean update, Callable blame);
  public Value get() {
    if (v == null) throw new ValueError("trying to get non-existing value", this);
    return v;
  }
  public Obj getOrThis() {
    if (v == null) return this;
    return v;
  }
  
  public Fun asFun() {
    return get().asFun();
  }
}