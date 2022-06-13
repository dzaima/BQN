package BQN.types;

import BQN.tools.Pervasion;
import BQN.types.arrs.SingleItemArr;

public abstract class Primitive extends Value {
  private static final int[] SHAPE = new int[0];
  
  public Primitive() {
    super(SHAPE, 1);
  }
  
  public Value call(         Value x) { return this; }
  public Value call(Value w, Value x) { return this; }
  
  public Value get(int i) { return this; }
  public Value ofShape(int[] sh) { assert Arr.prod(sh) == 1;
    return new SingleItemArr(this, sh);
  }
  
  
  public Value fItemS() {
    return null;
  }
  public Value fMineS() {
    return fItemS();
  }
  
  public int arrInfo() {
    return Pervasion.ARR_ATM;
  }
}