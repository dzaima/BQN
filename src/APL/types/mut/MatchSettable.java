package APL.types.mut;

import APL.Scope;
import APL.errors.DomainError;
import APL.types.*;

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
