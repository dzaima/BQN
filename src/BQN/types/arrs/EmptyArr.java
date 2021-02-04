package BQN.types.arrs;

import BQN.errors.*;
import BQN.tools.Pervasion;
import BQN.types.*;

import java.util.Iterator;

public class EmptyArr extends Arr {
  public static final int[] SHAPE0 = new int[]{0};
  public static final EmptyArr SHAPE0Q = new EmptyArr(SHAPE0, null);
  public static final EmptyArr SHAPE0N = new EmptyArr(SHAPE0, Num.ZERO);
  public static final EmptyArr SHAPE0S = new EmptyArr(SHAPE0, Char.SPACE);
  public static final int[] NOINTS = new int[0];
  public static final Value[] NOVALUES = new Value[0];
  public static final String[] NOSTRS = new String[0];
  
  private final Value proto;
  public EmptyArr(int[] sh, Value proto) {
    super(sh, 0);
    this.proto = proto;
  }
  
  public Value get(int i) {
    throw new ImplementationError("internal: using get() on empty array; view )jstack");
  }
  
  
  public String asString() {
    if (r() >= 2) throw new DomainError("Using rank≥2 array as char vector");
    return "";
  }
  
  public int[] asIntArrClone() { return NOINTS; } // safe, copy or not - doesn't matter
  public int[] asIntArr     () { return NOINTS; }
  
  public double[] asDoubleArr     () { return DoubleArr.EMPTY; }
  public double[] asDoubleArrClone() { return DoubleArr.EMPTY; }
  public double sum() { return 0; }
  
  public static final long[] NOLONGS = new long[0];
  public long[] asBitLongs() { return NOLONGS; }
  
  public Value[] valuesClone() { return NOVALUES; }
  public Value[] values     () { return NOVALUES; }
  
  
  
  public boolean quickIntArr() { return true; }
  public boolean quickDoubleArr() { return true; }
  public Value ofShape(int[] sh) {
    assert ia == Arr.prod(sh);
    return new EmptyArr(sh, proto);
  }
  public int arrInfo() {
    return proto==Char.SPACE? Pervasion.ARR_C16 : Pervasion.ARR_BIT;
  }
  
  public Value fItem() {
    if (proto == null) throw new DomainError("couldn't get prototype");
    return proto;
  }
  public Value fItemS() {
    return proto;
  }
  public Value fMineS() {
    return this;
  }
  
  private static final Iterator<Value> EIT = new Iterator<Value>() {
    public boolean hasNext() { return false; }
    public Value next() { throw new IllegalStateException("iterating empty array"); }
  };
  public Iterator<Value> iterator() {
    return EIT;
  }
}