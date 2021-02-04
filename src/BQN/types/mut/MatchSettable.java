package BQN.types.mut;

import BQN.Scope;
import BQN.errors.DomainError;
import BQN.types.*;

public class MatchSettable extends Settable {
  private final Value v;
  
  public MatchSettable(Value v) {
    this.v = v;
  }
  
  public Value get(Scope sc) {
    return v;
  }
  
  public void set(Value x, boolean update, Scope sc, Callable blame) {
    if (!x.eq(v)) throw new DomainError("setting a constant to a non-equivalent value", blame);
  }
  
  public boolean seth(Value x, Scope sc) {
    return x.eq(v);
  }
}
