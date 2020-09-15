package APL.types.functions.builtins.mops;

import APL.Main;
import APL.errors.DomainError;
import APL.tools.Pervasion;
import APL.types.*;
import APL.types.arrs.BitArr;
import APL.types.functions.DerivedMop;
import APL.types.functions.builtins.MopBuiltin;
import APL.types.functions.builtins.fns2.*;

public class FoldBuiltin extends MopBuiltin {
  @Override public String repr() {
    return "´";
  }
  
  
  
  
  public Value call(Value f, Value x, DerivedMop derv) {
    if (x.rank != 1) throw new DomainError("´: argument must have rank 1 (shape ≡ "+Main.formatAPL(x.shape)+")", this, f);
    if (x.ia==0) {
      Value id = f.identity();
      if (id == null) throw new DomainError("no identity defined for "+f, this, f);
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
        }
        double r = 1;
        for (double d : x.asDoubleArr()) r*= d;
        return new Num(r);
      }
      if (f instanceof FloorBuiltin) {
        if (x.quickIntArr()) {
          int r = Integer.MAX_VALUE;
          for (int c : x.asIntArr()) r = Math.min(r, c);
          return new Num(r);
        }
        double r = Double.POSITIVE_INFINITY;
        for (double d : x.asDoubleArr()) r = Math.min(r, d);
        return new Num(r);
      }
      if (f instanceof CeilingBuiltin) {
        if (x.quickIntArr()) {
          int r = Integer.MIN_VALUE;
          for (int c : x.asIntArr()) r = Math.max(r, c);
          return new Num(r);
        }
        double r = Double.NEGATIVE_INFINITY;
        for (double d : x.asDoubleArr()) r = Math.max(r, d);
        return new Num(r);
      }
    }
    join: if (f instanceof JoinBuiltin) {
      for (Value c : x) if (c.rank!=1) break join;
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
        }
        double[] xd = x.asDoubleArr();
        double c = xd[xd.length-1];
        for (int i = x.ia-2; i >= 0; i--) c = fd.on(xd[i], c);
        return new Num(c);
      }
    }
    Value[] a = x.values();
    return foldr(f, a, a[a.length-1], 1);
  }
  
  public Value call(Value f, Value w, Value x, DerivedMop derv) {
    if (x.rank != 1) throw new DomainError("´: argument must have rank 1 (shape ≡ "+Main.formatAPL(x.shape)+")", this, f);
  
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