package APL.types.arrs;

import APL.errors.*;
import APL.types.*;

import java.util.Iterator;

public class EmptyArr extends Arr {
  public static final int[] SHAPE0 = new int[]{0};
  public static final EmptyArr SHAPE0Q = new EmptyArr(SHAPE0, null);
  public static final EmptyArr SHAPE0N = new EmptyArr(SHAPE0, Num.ZERO);
  public static final EmptyArr SHAPE0S = new EmptyArr(SHAPE0, Char.SPACE);
  public static final int[] NOINTS = new int[0];
  public static final Value[] NOVALUES = new Value[0];
  
  private final Value proto;
  public EmptyArr(int[] sh, Value proto) {
    super(sh, 0, sh.length);
    this.proto = proto;
  }
  
  public Value get(int i) {
    throw new ImplementationError("internal: using get() on empty array; view )jstack");
  }
  
  
  public String asString() {
    if (rank >= 2) throw new DomainError("Using rank≥2 array as char vector", this);
    return "";
  }
  
  public int[] asIntArrClone() { return NOINTS; } // safe, copy or not - doesn't matter
  public int[] asIntArr     () { return NOINTS; }
  
  public double[] asDoubleArr     () { return DoubleArr.EMPTY; }
  public double[] asDoubleArrClone() { return DoubleArr.EMPTY; }
  public double sum() { return 0; }
  
  public Value[] valuesClone() { return NOVALUES; }
  public Value[] values     () { return NOVALUES; }
  
  
  
  public boolean quickDoubleArr() { return true; }
  public Value ofShape(int[] sh) {
    assert ia == Arr.prod(sh);
    return new EmptyArr(sh, proto);
  }
  
  
  public Value prototype() {
    if (proto == null) throw new DomainError("couldn't get prototype", this);
    return proto;
  }
  public Value safePrototype() {
    return proto;
  }
  
  
  
  
  private static final Iterator<Value> EIT = new Iterator<Value>() {
    public boolean hasNext() { return false; }
    public Value next() { throw new IllegalStateException("iterating empty array"); }
  };
  public Iterator<Value> iterator() {
    return EIT;
  }
  public Value squeeze() {
    return this;
  }
}