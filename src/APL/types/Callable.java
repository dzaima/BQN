package APL.types;

import APL.types.arrs.SingleItemArr;

public abstract class Callable extends Primitive {
  
  public Value ofShape(int[] sh) {
    if (sh.length==0) return this;
    return new SingleItemArr(this, sh);
  }
}