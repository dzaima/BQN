package APL.types;

import APL.Scope;
import APL.types.arrs.SingleItemArr;

public abstract class Callable extends Primitive {
  final public Scope sc;
  protected Callable(Scope sc) {
    this.sc = sc;
  }
  public Value ofShape(int[] sh) {
    if (sh.length==0) return this;
    return new SingleItemArr(this, sh);
  }
}