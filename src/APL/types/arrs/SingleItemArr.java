package APL.types.arrs;

import APL.Main;
import APL.errors.DomainError;
import APL.types.*;

import java.util.Iterator;

public class SingleItemArr extends Arr {
  private final Value item;
  
  public SingleItemArr(Value item, int[] shape) {
    super(shape);
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
    if (r() >= 2) throw new DomainError("Using rank≥2 array as string", this);
    if (!(item instanceof Char)) throw new DomainError("Using non-char array as string", this);
    char c = ((Char) item).chr;
    return Main.repeat(String.valueOf(c), ia);
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
  public Arr reverseOn(int dim) { return this; }
  
  public boolean quickDoubleArr() { return item instanceof Num; }
  public boolean quickIntArr() { return item instanceof Num && Num.isInt(((Num) item).num); }
  public boolean quickDepth1() { return item instanceof Primitive; }
  public Value[] valuesClone() {
    Value[] vs = new Value[ia];
    for (int i = 0; i < ia; i++) vs[i] = item;
    return vs;
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
  
  public String oneliner() {
    String r = Main.formatAPL(shape) + "⥊";
    if (!(item instanceof Primitive)) r+= "<";
    return r + item.oneliner();
  }
  
  public Iterator<Value> iterator() {
    //noinspection Convert2Diamond java 8
    return new Iterator<Value>() { int c = 0;
      public boolean hasNext() {
        return c < ia;
      }
      public Value next() { c++; return item; }
    };
  }
}