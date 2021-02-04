package BQN.types;

import BQN.errors.*;
import BQN.tools.*;
import BQN.types.arrs.*;

public abstract class Fun extends Callable {
  
  protected Fun() { }
  
  public Value call(Value x) {
    throw new IncorrectArgsError("function "+ln(FmtInfo.def)+" called monadically", this);
  }
  public Value call(Value w, Value x) {
    throw new IncorrectArgsError("function "+ln(FmtInfo.def)+" called dyadically", this);
  }
  
  public Value callInv (         Value x) { throw new DomainError(ln(FmtInfo.def)+" doesn't support monadic inverting", this); }
  public Value callInvX(Value w, Value x) { throw new DomainError(ln(FmtInfo.def)+" doesn't support dyadic inverting of 𝕩", this); }
  public Value callInvW(Value w, Value x) { throw new DomainError(ln(FmtInfo.def)+" doesn't support dyadic inverting of 𝕨", this); }
  
  public Value under(Value o, Value x) {
    Value v = o instanceof Fun? o.call(call(x)) : o;
    return callInv(v);
  }
  public Value underW(Value o, Value w, Value x) { // TODO rename
    Value v = o instanceof Fun? o.call(call(w, x)) : o;
    return callInvX(w, v);
  }
  public Value underA(Value o, Value w, Value x) {
    Value v = o instanceof Fun? o.call(call(w, x)) : o;
    return callInvW(v, x);
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
      throw new DomainError("bigintegers not allowed here");
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
    throw new DomainError("Expected number, got "+x.humanType(false), this);
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
    throw new DomainError("Expected either number or character argument, got "+x.humanType(false), this);
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
    throw new DomainError("Expected either number/char/map, got "+x.humanType(false), this);
  }
  
  
  // functions in general are equal on a per-object basis
  public int hashCode() {
    return actualHashCode();
  }
  public boolean eq(Value o) {
    return this == o;
  }
  
  public Value pretty(FmtInfo f) { return Format.str(ln(f)); }
  public abstract String ln(FmtInfo f);
}