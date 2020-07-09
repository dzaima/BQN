package APL.types.functions.builtins;

import APL.types.*;

public class DerivedSet extends AbstractSet {
  
  private final SetBuiltin s;
  private final Fun f;
  
  public DerivedSet(SetBuiltin s, Fun f) {
    this.s = s;
    this.f = f;
  }
  
  @Override public Value call(Obj w, Value x, boolean update) {
    s.call(w, f.call(((Settable) w).get(), x), update);
    return x;
  }
  
  public String toString() {
    return f.repr()+"←";
  }
}