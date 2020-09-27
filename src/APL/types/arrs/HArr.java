package APL.types.arrs;

import APL.errors.DomainError;
import APL.types.*;

import java.util.ArrayList;

public class HArr extends Arr {
  private final Value[] arr;
  public HArr(Value[] v, int[] sh) {
    super(sh, v.length);
    arr = v;
  }
  public HArr(ArrayList<Value> v) { // 1D
    super(new int[]{v.size()});
    arr = v.toArray(new Value[0]);
  }
  public HArr(Value[] v) { // 1D
    super(new int[]{v.length}, v.length);
    arr = v;
  }
  
  public HArr(ArrayList<Value> v, int[] sh) {
    super(sh);
    arr = v.toArray(new Value[0]);
  }
  
  
  
  
  public Value get(int i) { return arr[i]; }
  
  
  
  public Value[] values     () { return arr        ; }
  public Value[] valuesClone() { return arr.clone(); }
  
  
  public Value prototype() {
    if (ia == 0) throw new DomainError("failed to get prototype", this);
    return arr[0].prototype();
  }
  public Value safePrototype() {
    if (ia == 0) return null;
    return arr[0].safePrototype();
  }
  public Value ofShape(int[] sh) {
    assert ia == Arr.prod(sh);
    return new HArr(arr, sh);
  }
  
  
  
  public String asString() {
    StringBuilder r = new StringBuilder(ia);
    for (Value v : arr) {
      if (!(v instanceof Char)) throw new DomainError("Using array containing "+v.humanType(true)+" as string", this);
      r.append(((Char) v).chr);
    }
    return r.toString();
  }
}