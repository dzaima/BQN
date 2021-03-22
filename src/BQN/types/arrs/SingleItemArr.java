package BQN.types.arrs;

import BQN.Main;
import BQN.errors.DomainError;
import BQN.tools.Pervasion;
import BQN.types.*;

import java.util.*;

public class SingleItemArr extends Arr {
  private final Value item;
  
  public SingleItemArr(Value item, int[] shape) {
    super(shape);
    this.item = item;
  }
  public SingleItemArr(Value item, int[] shape, int ia) {
    super(shape, ia);
    this.item = item;
  }
  
  public static Value r0(Value v) {
    return new SingleItemArr(v, EmptyArr.NOINTS);
  }
  public static final int[] SH1 = new int[]{1};
  public static Value sh1(Value v) {
    return new SingleItemArr(v, SH1);
  }
  
  public Value get(int i) { return item; }
  
  
  
  public String asString() {
    if (r() > 1) throw new DomainError("Using rank "+r()+" array as string");
    if (!(item instanceof Char)) throw new DomainError("Using non-char array as string");
    char c = ((Char) item).chr;
    return Main.repeat(String.valueOf(c), ia);
  }
  
  public Value fItemS() {
    return item.fMineS();
  }
  
  public Value ofShape(int[] sh) {
    assert ia == Arr.prod(sh);
    return new SingleItemArr(item, sh);
  }
  public Arr reverseOn(int dim) { return this; }
  
  public boolean quickDoubleArr() { return item instanceof Num; }
  public boolean quickIntArr() { return item instanceof Num && Num.isInt(((Num) item).num); }
  public boolean quickDepth1() { return item instanceof Primitive; }
  public Value[] valuesClone() {
    Value[] vs = new Value[ia];
    for (int i = 0; i < ia; i++) vs[i] = item;
    return vs;
  }
  
  public int arrInfo() {
    switch (item.atomInfo()) {
      case Pervasion.ATM_BIT: return Pervasion.ARR_BIT;
      case Pervasion.ATM_I32: return Pervasion.ARR_I32;
      case Pervasion.ATM_F64: return Pervasion.ARR_F64;
      case Pervasion.ATM_CHR: return Pervasion.ARR_C16;
      default               : return Pervasion.ARR_ANY;
    }
  }
  
  public double sum() {
    return item.asDouble() * ia;
  }
  
  
  public int[] asIntArrClone() {
    int vi = item.asInt();
    int[] a = new int[ia];
    for (int i = 0; i < ia; i++) a[i] = vi;
    return a;
  }
  public double[] asDoubleArrClone() {
    double[] res = new double[ia];
    double n = item.asDouble();
    for (int i = 0; i < ia; i++) res[i] = n;
    return res;
  }
  public long[] asBitLongs() {
    double i = item.asDouble();
    if (i!=0 && i!=1) throw new DomainError("Using array containing "+i+" as boolean array");
    long[] r = new long[BitArr.sizeof(ia)];
    if (i!=0) Arrays.fill(r, -1L);
    return r;
  }
  
  public Iterator<Value> iterator() {
    return new Iterator<Value>() { int c = 0;
      public boolean hasNext() {
        return c < ia;
      }
      public Value next() { c++; return item; }
    };
  }
}