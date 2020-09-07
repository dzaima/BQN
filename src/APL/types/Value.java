package APL.types;

import APL.errors.*;
import APL.tools.Pervasion;
import APL.types.arrs.*;
import APL.types.functions.*;
import APL.types.functions.builtins.mops.ConstBultin;

import java.util.Iterator;


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
  
  public /*open*/ Value[] values() { return valuesClone(); }
  public /*open*/ Value[] valuesClone() {
    Value[] vs = new Value[ia];
    for (int i = 0; i < ia; i++) vs[i] = get(i);
    return vs;
  }
  
  public /*open*/ int[] asIntArr() { return asIntArrClone(); }
  public /*open*/ int[] asIntArrClone() {
    int[] res = new int[ia];
    for (int i = 0; i < ia; i++) res[i] = get(i).asInt();
    return res;
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
  
  
  
  
  public /*open*/ boolean quickDoubleArr() { return false; } // if true, asDoubleArr must succeed; also true for Num
  public /*open*/ boolean quickIntArr   () { return false; } // if true, asIntArr must succeed; also true for integer Num
  public /*open*/ boolean quickDepth1   () { return false; } // true if object is guaranteed to be depth 1 (returning false always is allowed)
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
  
  public boolean equals(Object obj) {
    return obj instanceof Value && eq((Value) obj);
  }
  public abstract boolean eq(Value o);
  public int compareTo(Value x) {
    Value w = this;
    
    if (w instanceof Num       && x instanceof Num      ) return Double.compare(((Num) w).num, ((Num) x).num);
    if (w instanceof Char      && x instanceof Char     ) return ((Char) w).compareTo((Char) x);
    if (w instanceof Num       && x instanceof Char     ) return -1;
    if (w instanceof Char      && x instanceof Num      ) return  1;
    if (w instanceof BigValue  && x instanceof BigValue ) return ((BigValue) w).i.compareTo(((BigValue) x).i);
    if (w instanceof Primitive && x instanceof Primitive) throw new DomainError("Cannot compare "+w+" and "+x);
    if (Math.min(w.ia, x.ia) == 0) return Integer.compare(w.ia, x.ia);
  
    int rc = Integer.compare(w.rank+(w instanceof Primitive?0:1), x.rank+(x instanceof Primitive?0:1));
    int rr = Math.min(w.rank, x.rank);
    int ri = 0; // matching shape tail
    while (ri<rr  &&  w.shape[w.shape.length-1-ri] == x.shape[x.shape.length-1-ri]) ri++;
    int rm = Arr.prod(w.shape, w.shape.length-ri, w.shape.length);
    if (ri<rr) {
      int wm = w.shape[w.shape.length-1-ri];
      int xm = x.shape[x.shape.length-1-ri];
      rc = Integer.compare(wm, xm);
      rm*= Math.min(wm, xm);
    }
  
    for (int i = 0; i < rm; i++) {
      int c = w.get(i).compareTo(x.get(i));
      if (c!=0) return c;
    }
    return rc;
  }
  
  
  
  
  public String repr() { return toString(); } // todo this is a stupid function
  public abstract Value call(         Value x);
  public abstract Value call(Value w, Value x);
  public Value identity() { return null; }
  public Pervasion.NN2N dyNum() { return null; }
  
  public Value callInv (         Value x) { if (eq(x))return this; throw new DomainError(this+"⁼: not equal to argument", this); }
  public Value callInvX(Value w, Value x) { if (eq(x))return this; throw new DomainError(this+"⁼: not equal to 𝕩", this); }
  public Value callInvW(Value w, Value x) { if (eq(w))return this; throw new DomainError(this+"⁼: not equal to 𝕨", this); }
  
  public Value under (Value o,          Value x) { throw new DomainError("Cannot execute under "+humanType(true)); }
  public Value underW(Value o, Value w, Value x) { throw new DomainError("Cannot execute under "+humanType(true)); }
  public Value underA(Value o, Value w, Value x) { throw new DomainError("Cannot execute under "+humanType(true)); }
  public Value constant(Callable blame) {
    if (this instanceof Callable) {
      if (this instanceof DerivedMop && ((DerivedMop) this).op instanceof ConstBultin) return ((DerivedMop) this).f;
      throw new DomainError(blame+": cannot interpret "+humanType(true)+" as a constant", blame, this);
    }
    return this;
  }
  
  public String humanType(boolean article) {
    if (this instanceof Arr     )return article? "an array"     : "array";
    if (this instanceof Char    )return article? "a character"  : "character";
    if (this instanceof Num     )return article? "a number"     : "number";
    if (this instanceof APLMap  )return article? "a map"        : "map";
    if (this instanceof Fun     )return article? "a function"   : "function";
    if (this instanceof Null    )return article? "null"         : "null";
    if (this instanceof Mop     )return article? "a 1-modifier" : "1-modifier";
    if (this instanceof Dop     )return article? "a 2-modifier" : "2-modifier";
    if (this instanceof BigValue)return article? "a bigint"     : "bigint";
    if (this instanceof Nothing )return article? "nothing"      : "nothing";
    return getClass().getSimpleName();
  }
  public abstract int hashCode();
  
}