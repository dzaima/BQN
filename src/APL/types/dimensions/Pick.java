package APL.types.dimensions;

import APL.Scope;
import APL.types.*;
import APL.types.functions.builtins.dops.AtBuiltin;
import APL.types.functions.builtins.fns.RShoeUBBuiltin;

public class Pick extends Settable {
  private final Variable var;
  private final Value val;
  private final Value idx;
  private final Brackets obj;
  
  public Pick(Variable var, Brackets where, Scope sc) {
    super(null);
    this.var = var;
    this.val = var.get();
    this.idx = where.val;
    this.obj = where;
  }
  
  @Override
  public void set(Value v, Callable blame) {
    var.update(AtBuiltin.at(v, idx, val, blame));
  }
  
  public Value get() {
    return RShoeUBBuiltin.on(idx, val, obj);
  }
  
  public Obj getOrThis() {
    return get();
  }
  
  @Override
  public String toString() {
    return var.name+"["+ val +"]";
  }
}