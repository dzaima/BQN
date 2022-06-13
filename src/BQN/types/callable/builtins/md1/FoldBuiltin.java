package BQN.types.callable.builtins.md1;

import BQN.Main;
import BQN.errors.DomainError;
import BQN.tools.*;
import BQN.types.*;
import BQN.types.arrs.BitArr;
import BQN.types.callable.Md1Derv;
import BQN.types.callable.builtins.Md1Builtin;
import BQN.types.callable.builtins.fns.*;

public class FoldBuiltin extends Md1Builtin {
  public String ln(FmtInfo f) { return "´"; }
  
  public Value call(Value f, Value x, Md1Derv derv) {
    if (x.r() != 1) throw new DomainError("´: argument must have rank 1 (shape ≡ "+Main.fArr(x.shape)+")", this);
    if (x.ia==0) {
      Value id = f.identity();
      if (id == null) throw new DomainError("no identity defined for "+f, this);
      return id;
    }
    
    if (x.quickDoubleArr()) {
      if (x instanceof BitArr) {
        if (f instanceof AndBuiltin) return AndBuiltin.reduce((BitArr) x);
        if (f instanceof OrBuiltin) return OrBuiltin.reduce((BitArr) x);
      }
      if (f instanceof PlusBuiltin) return new Num(x.sum());
      if (f instanceof MulBuiltin) {
        if (x.quickIntArr()) {
          double r = 1;
          for (int c : x.asIntArr()) r*= c;
          return new Num(r);
        } else {
          double r = 1;
          for (double d : x.asDoubleArr()) r*= d;
          return new Num(r);
        }
      }
      if (f instanceof FloorBuiltin) {
        if (x.quickIntArr()) {
          int r = Integer.MAX_VALUE;
          for (int c : x.asIntArr()) r = Math.min(r, c);
          return new Num(r);
        } else {
          double r = Double.POSITIVE_INFINITY;
          for (double d : x.asDoubleArr()) r = Math.min(r, d);
          return new Num(r);
        }
      }
      if (f instanceof CeilingBuiltin) {
        if (x.quickIntArr()) {
          int r = Integer.MIN_VALUE;
          for (int c : x.asIntArr()) r = Math.max(r, c);
          return new Num(r);
        } else {
          double r = Double.NEGATIVE_INFINITY;
          for (double d : x.asDoubleArr()) r = Math.max(r, d);
          return new Num(r);
        }
      }
    }
    join: if (f instanceof JoinBuiltin) {
      for (Value c : x) if (c.r()!=1) break join;
      return JoinBuiltin.joinVec(x);
    }
    
    if (x.quickDoubleArr()) {
      Pervasion.NN2N fd = f.dyNum();
      if (fd != null) {
        if (x.quickIntArr()) {
          int[] xi = x.asIntArr();
          double c = xi[xi.length-1];
          for (int i = x.ia-2; i >= 0; i--) c = fd.on(xi[i], c);
          return new Num(c);
        } else {
          double[] xd = x.asDoubleArr();
          double c = xd[xd.length-1];
          for (int i = x.ia-2; i >= 0; i--) c = fd.on(xd[i], c);
          return new Num(c);
        }
      }
    }
    Value[] a = x.values();
    return foldr(f, a, a[a.length-1], 1);
  }
  
  public Value call(Value f, Value w, Value x, Md1Derv derv) {
    if (x.r() != 1) throw new DomainError("´: argument must have rank 1 (shape ≡ "+Main.fArr(x.shape)+")", this);
    
    if (x.quickDoubleArr() && w instanceof Num) {
      Pervasion.NN2N fd = f.dyNum();
      double c = w.asDouble();
      if (fd != null) {
        if (x.quickIntArr()) {
          if (x instanceof BitArr && Num.isBool(c)) {
            if (f instanceof AndBuiltin) { if(c==0)return Num.ZERO; return AndBuiltin.reduce((BitArr) x); }
            if (f instanceof  OrBuiltin) { if(c==1)return  Num.ONE; return  OrBuiltin.reduce((BitArr) x); }
          }
          int[] xi = x.asIntArr();
          for (int i = x.ia-1; i >= 0; i--) c = fd.on(xi[i], c);
          return new Num(c);
        }
        double[] xd = x.asDoubleArr();
        for (int i = x.ia-1; i >= 0; i--) c = fd.on(xd[i], c);
        return new Num(c);
      }
    }
    
    return foldr(f, x.values(), w, 0);
  }
  
  static Value foldr(Value f, Value[] a, Value init, int skip) {
    for (int i = a.length-skip-1; i >= 0; i--) {
      init = f.call(a[i], init);
    }
    return init;
  }
}