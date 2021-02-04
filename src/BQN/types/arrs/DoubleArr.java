package BQN.types.arrs;

import BQN.errors.DomainError;
import BQN.tools.Pervasion;
import BQN.types.*;

import java.util.ArrayList;

public class DoubleArr extends Arr {
  public static final double[] EMPTY = new double[0];
  final public double[] arr;
  
  public DoubleArr(double[] arr, int[] sh) {
    super(sh, arr.length);
    this.arr = arr;
  }
  public DoubleArr(double[] arr) { // 1D
    super(new int[]{arr.length}, arr.length);
    this.arr = arr;
  }
  
  public DoubleArr(ArrayList<Double> arrl) {
    super(new int[]{arrl.size()}, arrl.size());
    arr = new double[ia];
    int j = 0;
    for (double d : arrl) arr[j++] = d;
  }
  
  
  public Value get(int i) { return Num.of(arr[i]); }
  
  
  
  public Value[] valuesClone() {
    Value[] vs = new Value[ia];
    for (int i = 0; i < ia; i++) vs[i] = new Num(arr[i]);
    return vs;
  }
  
  
  public int[] asIntArrClone() {
    int[] r = new int[ia];
    for (int i = 0; i < ia; i++) {
      int conv = (int) arr[i];
      if (arr[i] != conv) throw new DomainError("Using a fractional number as integer");
      r[i] = conv;
    }
    return r;
  }
  
  
  public double[] asDoubleArr() {
    return arr;
  }
  public double[] asDoubleArrClone() {
    return arr.clone();
  }
  public double sum() {
    double r = 0;
    for (double val : arr) r+= val;
    return r;
  }
  
  
  
  public boolean quickDoubleArr() { return true; }
  public boolean quickDepth1() { return true; }
  public Value ofShape(int[] sh) { return new DoubleArr(arr, sh); }
  public Value fItem() { return Num.ZERO; }
  public Value fItemS() { return Num.ZERO; }
  public int arrInfo() { return Pervasion.ARR_F64; }
  
  public int hashCode() {
    if (hash == 0) {
      for (double d : arr) {
        hash*= 31;
        if (d != 0d) hash+= Double.hashCode(d); // ¯0 == 0
      }
      hash = shapeHash(hash);
    }
    return hash;
  }
  
  public Arr reverseOn(int dim) {
    if (r() == 0) {
      if (dim != 0) throw new DomainError("rotating a scalar with a non-0 axis");
      return this;
    }
    if (dim < 0) dim+= r();
    // 2×3×4:
    // 0 - 3×4s for 2
    // 1 - 4s for 3
    // 2 - 1s for 4
    int chunkS = 1;
    int cPSec = shape[dim]; // chunks per section
    for (int i = r()-1; i > dim; i--) {
      chunkS*= shape[i];
    }
    int sec = chunkS * cPSec; // section length
    double[] res = new double[ia];
    int c = 0;
    while (c < ia) {
      for (int i = 0; i < cPSec; i++) {
        for (int j = 0; j < chunkS; j++) {
          res[c + (cPSec-i-1)*chunkS + j] = arr[c + i*chunkS + j];
        }
      }
      c+= sec;
    }
    return new DoubleArr(res, shape);
  }
}