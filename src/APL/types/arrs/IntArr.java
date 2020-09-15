package APL.types.arrs;

import APL.errors.DomainError;
import APL.types.*;

public class IntArr extends Arr {
  public final int[] arr;
  public IntArr(int[] arr, int[] shape) {
    super(shape, arr.length);
    this.arr = arr;
  }
  public IntArr(int[] arr) {
    super(new int[]{arr.length}, arr.length);
    this.arr = arr;
  }
  public IntArr(byte[] arr) {
    super(new int[]{arr.length}, arr.length);
    int[] a = new int[ia];
    for (int i = 0; i < ia; i++) a[i] = arr[i]&0xff;
    this.arr = a;
  }
  
  public static Arr maybe(double[] ds, int[] sh) {
    ia: {
      for (double c : ds) if (c != (int)c) break ia;
      int[] is = new int[ds.length];
      for (int i = 0; i < ds.length; i++) is[i] = (int) ds[i];
      return new IntArr(is, sh);
    }
    return new DoubleArr(ds, sh);
  }
  
  public Value get(int i) { return Num.of(arr[i]); }
  
  public Value[] valuesClone() {
    Value[] res = new Value[ia];
    for (int i = 0; i < arr.length; i++) res[i] = Num.of(arr[i]);
    return res;
  }
  
  
  public int[] asIntArr     () { return arr        ; }
  public int[] asIntArrClone() { return arr.clone(); }
  
  
  public double[] asDoubleArr() {
    return asDoubleArrClone();
  }
  public double[] asDoubleArrClone() {
    double[] r = new double[arr.length];
    for (int i = 0; i < r.length; i++) r[i] = arr[i];
    return r;
  }
  public double sum() {
    long sum = 0;
    for (int c : arr) sum+= c;
    return sum;
  }
  
  
  public boolean quickDoubleArr() { return true; }
  public boolean quickIntArr() { return true; }
  public boolean quickDepth1() { return true; }
  public Value ofShape(int[] sh) { return new IntArr(arr, sh); }
  public Value prototype() { return Num.ZERO; }
  public Value safePrototype() { return Num.ZERO; }
  public Value squeeze() {
    return this;
  }
  
  public int hashCode() {
    if (hash == 0) {
      for (int d : arr) hash = hash*31 + Double.hashCode(d);
      hash = shapeHash(hash);
    }
    return hash;
  }
  
  public Arr reverseOn(int dim) {
    if (rank == 0) {
      if (dim != 0) throw new DomainError("rotating a scalar with a non-0 axis", this);
      return this;
    }
    if (dim < 0) dim+= rank;
    int chunkS = 1;
    int cPSec = shape[dim]; // chunks per section
    for (int i = rank-1; i > dim; i--) {
      chunkS*= shape[i];
    }
    int sec = chunkS * cPSec; // section length
    int[] res = new int[ia];
    int c = 0;
    while (c < ia) {
      for (int i = 0; i < cPSec; i++) {
        for (int j = 0; j < chunkS; j++) {
          res[c + (cPSec-i-1)*chunkS + j] = arr[c + i*chunkS + j];
        }
      }
      c+= sec;
    }
    return new IntArr(res, shape);
  }
}