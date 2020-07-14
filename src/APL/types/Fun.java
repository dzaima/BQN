package APL.types;

import APL.*;
import APL.errors.*;
import APL.tools.Pervasion;
import APL.types.arrs.*;

import java.util.Iterator;

public abstract class Fun extends Callable {
  
  public Value identity() {
    return null;
  }
  
  protected Fun() { }
  
  public Value call(Value x) {
    throw new IncorrectArgsError("function "+toString()+" called monadically", this, x);
  }
  
  
  public Value call(Value w, Value x) {
    throw new IncorrectArgsError("function "+toString()+" called dyadically", this, w);
  }
  
  public Value callInv(Value x) {
    throw new DomainError(this+" doesn't support monadic inverting", this, x);
  }
  public Value callInvW(Value w, Value x) {
    throw new DomainError(this+" doesn't support dyadic inverting of 𝕩", this, x);
  }
  public Value callInvA(Value w, Value x) {
    throw new DomainError(this+" doesn't support dyadic inverting of 𝕨", this, x);
  }
  
  
  public Value under(Value o, Value x) {
    Value v = o instanceof Fun? ((Fun) o).call(call(x)) : o;
    return callInv(v);
  }
  public Value underW(Value o, Value w, Value x) {
    Value v = o instanceof Fun? ((Fun) o).call(call(w, x)) : o;
    return callInvW(w, v);
  }
  public Value underA(Value o, Value w, Value x) {
    Value v = o instanceof Fun? ((Fun) o).call(call(w, x)) : o;
    return callInvA(v, x);
  }
  
  public static abstract class NumMV {
    public abstract Value call(Num x);
    public boolean retNum() { // overriding this with false means call(int[], int[]) must be overridden too
      return true;
    }
    public double call(double x) {
      return call(new Num(x)).asDouble();
    }
    public void call(double[] res, double[] x) {
      for (int i = 0; i < res.length; i++) res[i] = call(x[i]);
    }
    public Value call(BigValue x) {
      throw new DomainError("bigintegers not allowed here", x);
    }
  
    public Value call(int[] x, int[] sh) {
      double[] res = new double[x.length];
      double[] d = new double[x.length];
      for (int i = 0; i < x.length; i++) d[i] = x[i];
      call(res, d);
      return new DoubleArr(res, sh);
    }
  }
  public interface ChrMV {
    Value call(Char x);
    default Arr call(ChrArr x) {
      Value[] res = new Value[x.ia];
      for (int i = 0; i < x.ia; i++) res[i] = call(Char.of(x.s.charAt(i)));
      return new HArr(res, x.shape);
    }
  }
  public interface MapMV {
    Value call(APLMap x);
  }
  
  
  
  protected Value numM(NumMV nf, Value x) {
    if (x instanceof Arr) {
      if (x.quickIntArr()) return nf.call(x.asIntArr(), x.shape);
      if (x.quickDoubleArr()) {
        double[] res = new double[x.ia];
        nf.call(res, x.asDoubleArr());
        return new DoubleArr(res, x.shape);
      }
      Arr o = (Arr) x;
      Value[] arr = new Value[o.ia];
      for (int i = 0; i < o.ia; i++) {
        arr[i] = numM(nf, o.get(i));
      }
      return new HArr(arr, o.shape);
    }
    if (x instanceof Num     ) return nf.call((Num     ) x);
    if (x instanceof BigValue) return nf.call((BigValue) x);
    throw new DomainError("Expected number, got "+x.humanType(false), this, x);
  }
  
  protected Value numChrM(NumMV nf, ChrMV cf, Value x) {
    if (x instanceof Arr) {
      if (x.quickDoubleArr()) {
        if (x.quickIntArr()) return nf.call(x.asIntArr(), x.shape);
        if (nf.retNum()) {
          double[] res = new double[x.ia];
          nf.call(res, x.asDoubleArr());
          return new DoubleArr(res, x.shape);
        }
      }
      if (x instanceof ChrArr) return cf.call((ChrArr) x);
      Arr o = (Arr) x;
      Value[] arr = new Value[o.ia];
      for (int i = 0; i < o.ia; i++) {
        arr[i] = numChrM(nf, cf, o.get(i));
      }
      return new HArr(arr, o.shape);
    }
    if (x instanceof Char    ) return cf.call((Char    ) x);
    if (x instanceof Num     ) return nf.call((Num     ) x);
    if (x instanceof BigValue) return nf.call((BigValue) x);
    throw new DomainError("Expected either number or character argument, got "+x.humanType(false), this, x);
  }
  
  protected Value numChrMapM(NumMV nf, ChrMV cf, MapMV mf, Value x) {
    if (x instanceof Arr) {
      if (x.quickDoubleArr()) {
        if (x.quickIntArr()) return nf.call(x.asIntArr(), x.shape);
        double[] res = new double[x.ia];
        nf.call(res, x.asDoubleArr());
        return new DoubleArr(res, x.shape);
      }
      Arr o = (Arr) x;
      Value[] arr = new Value[o.ia];
      for (int i = 0; i < o.ia; i++) {
        arr[i] = numChrMapM(nf, cf, mf, o.get(i));
      }
      return new HArr(arr, o.shape);
    }
    if (x instanceof Char    ) return cf.call((Char    ) x);
    if (x instanceof Num     ) return nf.call((Num     ) x);
    if (x instanceof APLMap  ) return mf.call((APLMap  ) x);
    if (x instanceof BigValue) return nf.call((BigValue) x);
    throw new DomainError("Expected either number/char/map, got "+x.humanType(false), this, x);
  }
  
  
  protected Value allD(D_AA f, Value w, Value x) {
    if (w instanceof Primitive && x instanceof Primitive) return f.call(w, x);
    
    if (w.scalar()) {
      Value w0 = w.first();
      
      if (x.scalar()) {
        return new Rank0Arr(allD(f, w0, x.first()));
      } else { // 𝕨 𝕩¨
        Value[] arr = new Value[x.ia];
        Iterator<Value> xi = x.iterator();
        for (int i = 0; i < x.ia; i++) {
          arr[i] = allD(f, w0, xi.next());
        }
        return new HArr(arr, x.shape);
        
      }
    } else {
      if (x.scalar()) { // 𝕨¨ 𝕩
        Value x0 = x.first();
        
        Value[] arr = new Value[w.ia];
        Iterator<Value> wi = w.iterator();
        for (int i = 0; i < w.ia; i++) {
          arr[i] = allD(f, wi.next(), x0);
        }
        return new HArr(arr, w.shape);
        
      } else { // 𝕨 ¨ 𝕩
        Arr.eqShapes(w, x);
        assert w.ia == x.ia;
        Value[] arr = new Value[w.ia];
        Iterator<Value> wi = w.iterator();
        Iterator<Value> xi = x.iterator();
        for (int i = 0; i < w.ia; i++) {
          arr[i] = allD(f, wi.next(), xi.next());
        }
        return new HArr(arr, w.shape);
        
      }
    }
  }
  
  
  
  
  
  
  
  public interface D_AA {
    Value call(Value w, Value x);
  }
  public static abstract class D_NNeN extends D_NN { // dyadic number-number equals number
    public abstract double on(double w, double x);
    public void on(double[] res, double w, double[] x) {
      for (int i = 0; i < x.length; i++) {
        res[i] = on(w, x[i]);
      }
    }
    public void on(double[] res, double[] w, double x) {
      for (int i = 0; i < w.length; i++) {
        res[i] = on(w[i], x);
      }
    }
    public void on(double[] res, double[] w, double[] x) {
      for (int i = 0; i < w.length; i++) {
        res[i] = on(w[i], x[i]);
      }
    }
    
    public Value call(double w, double x) {
      return new Num(on(w, x));
    }
    public Value call(double[] w, double[] x, int[] sh) {
      double[] res = new double[x.length];
      on(res, w, x);
      return new DoubleArr(res, sh);
    }
    public Value call(double w, double[] x, int[] sh) {
      double[] res = new double[x.length];
      on(res, w, x);
      return new DoubleArr(res, sh);
    }
    public Value call(double[] w, double x, int[] sh) {
      double[] res = new double[w.length];
      on(res, w, x);
      return new DoubleArr(res, sh);
    }
    public Value call(BigValue w, BigValue x) {
      throw new DomainError("bigintegers not allowed here", x);
    }
  }
  
  public /*open*/ Pervasion.NN2N dyNum() {
    return null;
  }
  
  public static abstract class D_NNeB extends D_NN { // dyadic number-number equals boolean
    public abstract boolean on(double w, double x);
    public abstract void on(BitArr.BA res, double   w, double[] x);
    public abstract void on(BitArr.BA res, double[] w, double   x);
    public abstract void on(BitArr.BA res, double[] w, double[] x);
    
    public Value call(double w, double x) {
      return on(w, x)? Num.ONE : Num.ZERO;
    }
    public Value call(double[] w, double[] x, int[] sh) {
      BitArr.BA res = new BitArr.BA(sh);
      on(res, w, x);
      return res.finish();
    }
    public Value call(double w, double[] x, int[] sh) {
      BitArr.BA res = new BitArr.BA(sh);
      on(res, w, x);
      return res.finish();
    }
    public Value call(double[] w, double x, int[] sh) {
      BitArr.BA res = new BitArr.BA(sh);
      on(res, w, x);
      return res.finish();
    }
    public Value call(BigValue w, BigValue x) {
      throw new DomainError("bigintegers not allowed here", x);
    }
  }
  
  
  public static abstract class D_NN {
    public abstract Value call(double   w, double   x);
    public abstract Value call(double[] w, double[] x, int[] sh);
    public abstract Value call(double   w, double[] x, int[] sh);
    public abstract Value call(double[] w, double   x, int[] sh);
    public abstract Value call(BigValue w, BigValue x);
    public /*open*/ Value call(double w, BigValue x) { // special requirement for log; only needs to be handled in numD
      return call(new BigValue(w), x);
    }
  }
  public static abstract class D_BB {
    public abstract Value call(BitArr  w, BitArr  x);
    public abstract Value call(boolean w, BitArr  x);
    public abstract Value call(BitArr  w, boolean x);
  }
  public interface D_CC {
    public abstract Value call(char w, char x);
  }
  
  
  protected Value numD(D_NN f, Value w, Value x) {
    if (w.scalar()) {
      if (x.scalar()) { // ⊃𝕨 ⊃𝕩
        if (w instanceof Primitive & x instanceof Primitive) {
          boolean wn = w instanceof Num;
          boolean xn = x instanceof Num;
          if (wn & xn) return f.call(((Num) w).num, ((Num) x).num);
          if ((w instanceof BigValue|wn) & (x instanceof BigValue|xn)) {
            if (wn) return f.call(((Num) w).num, (BigValue) x);
            else return f.call((BigValue) w, xn? new BigValue(((Num) x).num) : (BigValue) x);
          }
          throw new DomainError("calling a number-only function with "+w.humanType(true)+" and "+x.humanType(false), this);
        } else return new Rank0Arr(numD(f, w.first(), x.first()));
        
      } else { // 𝕨¨ 𝕩
        if (x.quickDoubleArr() && w instanceof Num) {
          return f.call(w.asDouble(), x.asDoubleArr(), x.shape);
        }
        Value w0 = w.first();
        Iterator<Value> xi = x.iterator();
        Value[] vs = new Value[x.ia];
        for (int i = 0; i < x.ia; i++) {
          vs[i] = numD(f, w0, xi.next());
        }
        return new HArr(vs, x.shape);
        
      }
    } else {
      if (x.scalar()) { // 𝕨 𝕩¨
        if (w.quickDoubleArr() && x instanceof Num) {
          return f.call(w.asDoubleArr(), x.asDouble(), w.shape);
        }
        Value x0 = x.first();
        Iterator<Value> wi = w.iterator();
        Value[] vs = new Value[w.ia];
        for (int i = 0; i < w.ia; i++) {
          vs[i] = numD(f, wi.next(), x0);
        }
        
        return new HArr(vs, w.shape);
        
      } else { // 𝕨 ¨ 𝕩
        Arr.eqShapes(w, x);
        
        if (w.quickDoubleArr() && x.quickDoubleArr()) {
          return f.call(w.asDoubleArr(), x.asDoubleArr(), w.shape);
        }
        
        Value[] arr = new Value[w.ia];
        Iterator<Value> wi = w.iterator();
        Iterator<Value> xi = x.iterator();
        for (int i = 0; i < w.ia; i++) {
          arr[i] = numD(f, wi.next(), xi.next());
        }
        return new HArr(arr, w.shape);
        
      }
    }
  }
  protected Value bitD(D_NN n, D_BB b, Value w, Value x) {
    if (w.scalar()) {
      if (x.scalar()) { // ⊃𝕨 ⊃𝕩
        if (w instanceof Primitive & x instanceof Primitive) {
          boolean wn = w instanceof Num;
          boolean xn = x instanceof Num;
          if (wn & xn) return n.call(((Num) w).num, ((Num) x).num);
          if ((w instanceof BigValue|wn) & (x instanceof BigValue|xn))
            return n.call(wn? new BigValue(((Num) w).num) : (BigValue) w, xn? new BigValue(((Num) x).num) : (BigValue) x);
          throw new DomainError("calling a number-only function with "+w.humanType(true)+" and "+x.humanType(false), this);
        } else return new Rank0Arr(bitD(n, b, w.first(), x.first()));
        
      } else { // 𝕨¨ 𝕩
        if (w instanceof Primitive) {
          if (x instanceof BitArr && Main.isBool(w)) {
            return b.call(Main.bool(w), (BitArr) x);
          }
          if (w instanceof Num && x.quickDoubleArr()) {
            return n.call(w.asDouble(), x.asDoubleArr(), x.shape);
          }
        }
        Value w0 = w.first();
        Iterator<Value> xi = x.iterator();
        Value[] vs = new Value[x.ia];
        for (int i = 0; i < x.ia; i++) {
          vs[i] = bitD(n, b, w0, xi.next());
        }
        return new HArr(vs, x.shape);
        
      }
    } else {
      if (x.scalar()) { // 𝕨 𝕩¨
        if (x instanceof Primitive) {
          if (w instanceof BitArr && Main.isBool(x)) {
            return b.call((BitArr) w, Main.bool(x));
          }
          if (w instanceof Num && w.quickDoubleArr()) {
            return n.call(w.asDoubleArr(), x.asDouble(), w.shape);
          }
        }
        Value x0 = x.first();
        Iterator<Value> wi = w.iterator();
        Value[] vs = new Value[w.ia];
        for (int i = 0; i < w.ia; i++) {
          vs[i] = bitD(n, b, wi.next(), x0);
        }
        
        return new HArr(vs, w.shape);
        
      } else { // 𝕨 ¨ 𝕩
        Arr.eqShapes(w, x);
        
        if (w instanceof BitArr && x instanceof BitArr) {
          return b.call((BitArr) w, (BitArr) x);
        }
        
        if (w.quickDoubleArr() && x.quickDoubleArr()) {
          return n.call(w.asDoubleArr(), x.asDoubleArr(), w.shape);
        }
        
        Value[] arr = new Value[w.ia];
        Iterator<Value> wi = w.iterator();
        Iterator<Value> xi = x.iterator();
        for (int i = 0; i < w.ia; i++) {
          arr[i] = bitD(n, b, wi.next(), xi.next());
        }
        return new HArr(arr, w.shape);
        
      }
    }
  }
  
  
  protected Value numChrD(D_NN n, D_CC c, D_AA def, Value w, Value x) {
    if (w.scalar()) {
      if (x.scalar()) { // ⊃𝕨 ⊃𝕩
        if (w instanceof Primitive & x instanceof Primitive) {
          boolean wn = w instanceof Num;
          boolean xn = x instanceof Num;
          if (wn & xn) return n.call(((Num) w).num, ((Num) x).num);
          if ((w instanceof BigValue|wn) & (x instanceof BigValue|xn))
            return n.call(wn? new BigValue(((Num) w).num) : (BigValue) w, xn? new BigValue(((Num) x).num) : (BigValue) x);
          if (w instanceof Char & x instanceof Char) return c.call(((Char) w).chr, ((Char) x).chr);
          return def.call(w, x);
        } else return new Rank0Arr(numChrD(n, c, def, w.first(), x.first()));
        
      } else { // 𝕨¨ 𝕩
        if (w instanceof Num && x.quickDoubleArr()) {
          return n.call(w.asDouble(), x.asDoubleArr(), x.shape);
        }
        
        Value w0 = w.first();
        Iterator<Value> xi = x.iterator();
        Value[] vs = new Value[x.ia];
        for (int i = 0; i < x.ia; i++) {
          vs[i] = numChrD(n, c, def, w0, xi.next());
        }
        return new HArr(vs, x.shape);
        
      }
    } else {
      if (x.scalar()) { // 𝕨 𝕩¨
        if (x instanceof Num && w.quickDoubleArr()) {
          return n.call(w.asDoubleArr(), x.asDouble(), w.shape);
        }
        Value x0 = x.first();
        Iterator<Value> wi = w.iterator();
        Value[] vs = new Value[w.ia];
        for (int i = 0; i < w.ia; i++) {
          vs[i] = numChrD(n, c, def, wi.next(), x0);
        }
        
        return new HArr(vs, w.shape);
      } else { // 𝕨 ¨ 𝕩
        Arr.eqShapes(w, x);
        
        if (w.quickDoubleArr() && x.quickDoubleArr()) {
          return n.call(w.asDoubleArr(), x.asDoubleArr(), w.shape);
        }
        
        Value[] arr = new Value[w.ia];
        Iterator<Value> wi = w.iterator();
        Iterator<Value> xi = x.iterator();
        for (int i = 0; i < w.ia; i++) {
          arr[i] = numChrD(n, c, def, wi.next(), xi.next());
        }
        return new HArr(arr, w.shape);
        
      }
    }
  }
  protected Value ncbaD(D_NN n, D_BB b, D_CC c, D_AA def, Value w, Value x) {
    if (w.scalar()) {
      if (x.scalar()) { // ⊃𝕨 ⊃𝕩
        if (w instanceof Primitive & x instanceof Primitive) {
          boolean wn = w instanceof Num;
          boolean xn = x instanceof Num;
          if (wn & xn) return n.call(((Num) w).num, ((Num) x).num);
          else if (w instanceof Char & x instanceof Char) return c.call(((Char) w).chr, ((Char) x).chr);
          else if ((w instanceof BigValue|wn) & (x instanceof BigValue|xn))
            return n.call(wn? new BigValue(((Num) w).num) : (BigValue) w, xn? new BigValue(((Num) x).num) : (BigValue) x);
          else return def.call(w, x);
        } else return new Rank0Arr(ncbaD(n, b, c, def, w.first(), x.first()));
        
      } else { // 𝕨¨ 𝕩
        if (w instanceof Primitive) {
          if (x instanceof BitArr && Main.isBool(w)) {
            return b.call(Main.bool(w), (BitArr) x);
          }
          if (w instanceof Num && x.quickDoubleArr()) {
            return n.call(w.asDouble(), x.asDoubleArr(), x.shape);
          }
        }
        
        Value w0 = w.first();
        Iterator<Value> xi = x.iterator();
        Value[] vs = new Value[x.ia];
        for (int i = 0; i < x.ia; i++) {
          vs[i] = ncbaD(n, b, c, def, w0, xi.next());
        }
        return new HArr(vs, x.shape);
      }
    } else {
      if (x.scalar()) { // 𝕨 𝕩¨
        if (x instanceof Primitive) {
          if (w instanceof BitArr && Main.isBool(x)) {
            return b.call((BitArr) w, Main.bool(x));
          }
          if (w instanceof Num && w.quickDoubleArr()) {
            return n.call(w.asDoubleArr(), x.asDouble(), w.shape);
          }
        }
        Value x0 = x.first();
        Iterator<Value> wi = w.iterator();
        Value[] vs = new Value[w.ia];
        for (int i = 0; i < w.ia; i++) {
          vs[i] = ncbaD(n, b, c, def, wi.next(), x0);
        }
        
        return new HArr(vs, w.shape);
        
      } else { // 𝕨 ¨ 𝕩
        Arr.eqShapes(w, x);
        
        if (w instanceof BitArr && x instanceof BitArr) {
          return b.call((BitArr) w, (BitArr) x);
        }
        if (w.quickDoubleArr() && x.quickDoubleArr()) {
          return n.call(w.asDoubleArr(), x.asDoubleArr(), w.shape);
        }
        
        Value[] arr = new Value[w.ia];
        Iterator<Value> wi = w.iterator();
        Iterator<Value> xi = x.iterator();
        for (int i = 0; i < w.ia; i++) {
          arr[i] = ncbaD(n, b, c, def, wi.next(), xi.next());
        }
        return new HArr(arr, w.shape);
        
      }
    }
  }
  
  
  
  
  
  
  public abstract String repr();
  
  public String toString() {
    return repr();
  }
  
  public Fun asFun() {
    return this;
  }
  public boolean notIdentity() { return true; }
  
  // functions are equal on a per-object basis
  public int hashCode() {
    return actualHashCode();
  }
  public boolean equals(Obj o) {
    return this == o;
  }
}