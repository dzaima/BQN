package APL.types.arrs;

import APL.errors.DomainError;
import APL.types.*;

public class Shape1Arr extends Arr {
  private static final int[] SHAPE = new int[]{1};
  private final Value item;
  
  public Shape1Arr(Value item) {
    super(SHAPE, 1, 1);
    this.item = item;
  }
  
  public Value get(int i) { return item; }
  
  public String asString() {
    if (item instanceof Char) return String.valueOf(((Char)item).chr);
    throw new DomainError("Using array containing "+item.humanType(true)+" as string", this);
  }
  
  public double[] asDoubleArrClone() { return new double[]{item.asDouble()}; }
  public int[] asIntArrClone() { return new int[]{item.asInt()}; }
  public Value[] valuesClone() { return new Value[]{item}; }
  
  
  public boolean quickDoubleArr() {
    return item instanceof Num;
  }
  public Value prototype() {
    return item.prototype();
  }
  public Value safePrototype() {
    return item.safePrototype();
  }
  public Value ofShape(int[] sh) {
    assert ia == Arr.prod(sh);
    return new SingleItemArr(item, sh);
  }
}