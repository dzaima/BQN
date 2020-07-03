package APL.types;

import APL.Type;
import APL.errors.*;
import APL.types.arrs.*;

import java.util.*;


public abstract class Value extends Obj implements Iterable<Value> {
  public final int[] shape;
  public final int rank;
  public final int ia; // item amount
  Value(int[] shape) {
    this.shape = shape;
    rank = shape.length;
    ia = Arr.prod(shape);
  }
  Value(int[] shape, int ia, int rank) {
    this.shape = shape;
    this.ia = ia;
    this.rank = rank;
  }
  public int[] asIntVec() { // succeeds on rank ≤ 1
    if (rank > 1) throw new DomainError("Using rank "+rank+" array as an integer vector", this);
    return asIntArr();
  }
  public abstract int[] asIntArrClone();
  public int[] asIntArr() {
    return asIntArrClone();
  }
  public abstract int asInt();
  public boolean scalar() {
    return rank == 0;
  }
  public Value first() {
    return get(0);
  }
  public abstract Value get(int i); // WARNING: UNSAFE; doesn't need to throw for out-of-bounds
  
  
  
  public int compareTo(Value r) {
    Value l = this;
    
    boolean rA = r instanceof Arr;
    boolean lA = l instanceof Arr;
    
    if (l instanceof  Num         && r instanceof Num         ) return ((Num) l).compareTo((Num) r);
    if (l instanceof Char         && r instanceof Char        ) return ((Char) l).compareTo((Char) r);
    if (l instanceof  Num         && (r instanceof Char || rA)) return -1;
    if ((l instanceof Char || lA) && r instanceof Num         ) return 1;
    if (l instanceof BigValue     && r instanceof BigValue    ) return ((BigValue) l).i.compareTo(((BigValue) r).i);
    if (!lA && !rA) {
      throw new DomainError("Failed to compare "+ l +" and "+r, r);
    }
    if (!lA) return -1;
    if (!rA) return  1;
    
    if (l.rank != r.rank) throw new RankError("Expected ranks to be equal for compared elements", r);
    
    if (l.rank > 1) throw new DomainError("Expected rank of compared array to be ≤ 2", l);
    
    int min = Math.min(l.ia, r.ia);
    for (int i = 0; i < min; i++) {
      int cr = l.get(i).compareTo(r.get(i));
      if (cr != 0) return cr;
    }
    return Integer.compare(l.ia, r.ia);
  }
  
  
  public abstract String asString();
  
  
  public Integer[] gradeUp() {
    if (rank != 1) throw new DomainError("grading rank ≠ 1", this);
    Integer[] na = new Integer[ia];
    
    for (int i = 0; i < na.length; i++) {
      na[i] = i;
    }
    Arrays.sort(na, (a, b) -> get(a).compareTo(get(b)));
    return na;
  }
  public Integer[] gradeDown() {
    if (rank != 1) throw new DomainError("grading rank ≠ 1", this);
    Integer[] na = new Integer[ia];
    
    for (int i = 0; i < na.length; i++) {
      na[i] = i;
    }
    Arrays.sort(na, (a, b) -> get(b).compareTo(get(a)));
    return na;
  }
  
  public int[] eraseDim(int place) {
    int[] res = new int[rank-1];
    System.arraycopy(shape, 0, res, 0, place);
    System.arraycopy(shape, place+1, res, place, rank-1-place);
    return res;
  }
  @Override
  public Type type() {
    return Type.array;
  }
  
  public abstract Value prototype(); // what to append to this array
  public abstract Value safePrototype();
  
  public String oneliner() {
    return toString();
  }
  
  
  public Value[] values() {
    return valuesCopy();
  }
  public Value[] valuesCopy() {
    Value[] vs = new Value[ia];
    for (int i = 0; i < ia; i++) vs[i] = get(i);
    return vs;
  }
  
  @Override
  public Iterator<Value> iterator() {
    return new ValueIterator();
  }
  
  class ValueIterator implements Iterator<Value> {
    int c = 0;
    @Override
    public boolean hasNext() {
      return c < ia;
    }
    
    @Override
    public Value next() {
      return get(c++);
    }
  }
  
  public Value at(int[] pos) {
    if (pos.length != rank) throw new RankError("array rank was "+rank+", tried to get item at rank "+pos.length, this);
    int x = 0;
    for (int i = 0; i < rank; i++) {
      if (pos[i] < 0) throw new DomainError("Tried to access item at position "+pos[i], this);
      if (pos[i] >= shape[i]) throw new DomainError("Tried to access item at position "+pos[i]+" while max was "+(shape[i]-1), this);
      x+= pos[i];
      if (i != rank-1) x*= shape[i+1];
    }
    return get(x);
  }
  public Value at(int[] pos, Value def) { // 0-indexed
    int x = 0;
    for (int i = 0; i < rank; i++) {
      if (pos[i] < 0 || pos[i] >= shape[i]) return def;
      x+= pos[i];
      if (i != rank-1) x*= shape[i+1];
    }
    return get(x);
  }
  public Value simpleAt(int[] pos) {
    int x = 0;
    for (int i = 0; i < rank; i++) {
      x+= pos[i];
      if (i != rank-1) x*= shape[i+1];
    }
    return get(x);
  }
  
  public abstract Value ofShape(int[] sh); // don't call with ×/sh ≠ ×/shape! ()
  
  public double sum() {
    double res = 0;
    for (Value v : this) {
      res+= v.asDouble();
    }
    return res;
  }
  public double[] asDoubleArr() { // warning: also succeeds on a primitive number; don't modify
    double[] res = new double[ia];
    int i = 0;
    for (Value c : values()) {
      res[i++] = c.asDouble();
    }
    return res;
  }
  public double[] asDoubleArrClone() {
    return asDoubleArr().clone();
  }
  public double asDouble() {
    throw new DomainError("Using "+this.humanType(true)+" as a number", this);
  }
  public boolean quickDoubleArr() { // if true, asDoubleArr must succeed; warning: also succeeds on a primitive number
    return false;
  }
  public Value squeeze() {
    if (ia == 0) return this;
    Value f = get(0);
    if (f instanceof Num) {
      double[] ds = new double[ia];
      for (int i = 0; i < ia; i++) {
        if (get(i) instanceof Num) ds[i] = get(i).asDouble();
        else {
          ds = null;
          break;
        }
      }
      if (ds != null) return new DoubleArr(ds, shape);
    }
    if (f instanceof Char) {
      StringBuilder s = new StringBuilder();
      for (int i = 0; i < ia; i++) {
        if (get(i) instanceof Char) s.append(((Char) get(i)).chr);
        else {
          s = null;
          break;
        }
      }
      if (s != null) return new ChrArr(s.toString(), shape);
    }
    boolean anyBetter = false;
    Value[] optimized = new Value[ia];
    Value[] values = values();
    for (int i = 0, valuesLength = values.length; i < valuesLength; i++) {
      Value v = values[i];
      Value vo = v.squeeze();
      if (vo != v) anyBetter = true;
      optimized[i] = vo;
    }
    if (anyBetter) return new HArr(optimized, shape);
    return this;
  }
  
  
  public /*open*/ Fun asFun() {
    return new Fun() {
      public String repr() {
        return Value.this.toString();
      }
  
      public Value call(Value w) {
        return Value.this;
      }
  
      public Value call(Value a, Value w) {
        return Value.this;
      }
    };
  }
}