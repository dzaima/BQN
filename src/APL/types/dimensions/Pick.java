package APL.types.dimensions;

import APL.Scope;
import APL.errors.SyntaxError;
import APL.types.*;
import APL.types.functions.builtins.dops.AtBuiltin;
import APL.types.functions.builtins.fns2.LBoxUBBuiltin;

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
  
  public void set(Value v, boolean update, Callable blame) {
    if (update) throw new SyntaxError("←: Must use ↩ to set indexed elements");
    var.set(AtBuiltin.at(v, idx, val, blame), false, blame);
  }
  
  public Value get() {
    return LBoxUBBuiltin.on(idx, val, obj);
  }
  
  public Obj getOrThis() {
    return get();
  }
  
  @Override
  public String toString() {
    return var.name+"["+ val +"]";
  }
}