package APL.types;

import APL.Type;
import APL.errors.*;
import APL.types.arrs.*;
import APL.types.functions.builtins.mops.CellBuiltin;

import java.util.*;


public abstract class Value extends Obj implements Iterable<Value>, Comparable<Value> {
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
  
  
  
  
  public abstract Value get(int i); // WARNING: UNSAFE; doesn't need to throw for out-of-bounds
  public Value first() { return get(0); }
  
  
  
  // methods for interpreting this as other types
  
  public /*open*/ double asDouble() { throw new DomainError("Using "+this.humanType(true)+" as a number", this); }
  public /*open*/ int asInt() { throw new DomainError("Using "+humanType(true)+" as integer"); }
  public /*open*/ String asString() {
    char[] cs = new char[ia];
    for (int i = 0; i < ia; i++) cs[i] = ((Char) get(i)).chr;
    return new String(cs);
  }
  
  public /*open*/ int[] asIntArr() { return asIntArrClone(); }
  public /*open*/ int[] asIntArrClone() {
    int[] res = new int[ia];
    for (int i = 0; i < ia; i++) res[i] = get(i).asInt();
    return res;
  }
  
  
  public /*open*/ Value[] values() { return valuesClone(); }
  public /*open*/ Value[] valuesClone() {
    Value[] vs = new Value[ia];
    for (int i = 0; i < ia; i++) vs[i] = get(i);
    return vs;
  }
  
  
  public /*open*/ double[] asDoubleArr() { return asDoubleArrClone(); }
  public /*open*/ double[] asDoubleArrClone() {
    double[] res = new double[ia];
    for (int i = 0; i < ia; i++) res[i] = get(i).asDouble();
    return res;
  }
  public /*open*/ double sum() {
    double res = 0;
    for (Value v : this) res+= v.asDouble();
    return res;
  }
  public /*open*/ int[] asIntVec() { // also works on rank≡0; immutable
    if (rank > 1) throw new DomainError("Using rank "+rank+" array as an integer vector", this);
    return asIntArr();
  }
  
  
  
  
  
  public /*open*/ boolean quickDoubleArr() { return false; } // if true, asDoubleArr must succeed; warning: also true for a primitive number
  public /*open*/ boolean quickIntArr() { return false; }
  public /*open*/ boolean notIdentity() { return false; } // whether asFun().call(…) != this
  public boolean scalar() { return rank == 0; }
  public abstract Value ofShape(int[] sh); // don't call with ×/sh ≠ ×/shape!
  public abstract Value safePrototype(); // what to append to this array
  public Value prototype() {
    Value v = safePrototype();
    if (v==null) throw new DomainError("Getting prototype of "+this, this);
    return v;
  }
  
  public /*open*/ String oneliner() {
    return toString();
  }
  
  
  
  public /*open*/ Iterator<Value> iterator() {
    //noinspection Convert2Diamond java 8
    return new Iterator<Value>() { int c = 0;
      public boolean hasNext() { return c < ia; }
      public Value next() { return get(c++); }
    };
  }
  
  
  // outdated bad item getting methods; TODO don't use
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
  
  
  public int compareTo(Value r) {
    Value l = this;
    
    boolean rA = r instanceof Arr;
    boolean lA = l instanceof Arr;
    
    if ( l instanceof Num         &&  r instanceof Num        ) return ((Num) l).compareTo((Num) r);
    if ( l instanceof Char        &&  r instanceof Char       ) return ((Char) l).compareTo((Char) r);
    if ( l instanceof Num         && (r instanceof Char || rA)) return -1;
    if ((l instanceof Char || lA) &&  r instanceof Num        ) return  1;
    if ( l instanceof BigValue    &&  r instanceof BigValue   ) return ((BigValue) l).i.compareTo(((BigValue) r).i);
    if (!lA && !rA) {
      throw new DomainError("Failed to compare "+l+" and "+r, r);
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
  public Integer[] gradeUp() {
    if (rank == 0) throw new DomainError("cannot grade rank 0", this);
    if (rank != 1) return new HArr(CellBuiltin.cells(this)).gradeUp();
    Integer[] na = new Integer[ia];
    for (int i = 0; i < na.length; i++) na[i] = i;
    Arrays.sort(na, (a, b) -> get(a).compareTo(get(b)));
    
    return na;
  }
  public Integer[] gradeDown() {
    if (rank == 0) throw new DomainError("cannot grade rank 0", this);
    if (rank != 1) return new HArr(CellBuiltin.cells(this)).gradeDown();
    
    Integer[] na = new Integer[ia];
    for (int i = 0; i < na.length; i++) na[i] = i;
    
    Arrays.sort(na, (a, b) -> get(b).compareTo(get(a)));
    return na;
  }
  
  
  public /*open*/ Fun asFun() {
    return new Fun() {
      public String repr() {
        return Value.this.toString();
      }
      
      public Value call(Value x) {
        return Value.this;
      }
      
      public Value call(Value w, Value x) {
        return Value.this;
      }
    };
  }
  
  public Type type() { // TODO remove
    return Type.array;
  }
}