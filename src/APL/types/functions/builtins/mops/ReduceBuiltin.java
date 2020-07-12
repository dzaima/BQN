package APL.types.functions.builtins.mops;

import APL.Main;
import APL.errors.DomainError;
import APL.types.*;
import APL.types.functions.*;
import APL.types.functions.builtins.fns2.*;

public class ReduceBuiltin extends Mop {
  @Override public String repr() {
    return "´";
  }
  
  
  
  
  public Value call(Value f, Value x, DerivedMop derv) {
    Fun ff = f.asFun();
    if (x.rank != 1) throw new DomainError("´: argument must have rank 1 (shape ≡ "+Main.formatAPL(x.shape)+")", this, f);
    if (x.ia==0) {
      Value id = ff.identity();
      if (id == null) throw new DomainError("no identity defined for "+f.name(), this, f);
      return id;
    }
    
    if (x.quickDoubleArr()) {
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
    if (f instanceof JoinBuiltin) {
      Value joined = JoinBuiltin.joinVec(x);
      if (joined != null) return joined;
    }
    
    Value[] a = x.values();
    return foldr(ff, a, a[a.length-1], 1);
  }
  
  public Value call(Value f, Value w, Value x, DerivedMop derv) {
    if (x.rank != 1) throw new DomainError("´: argument must have rank 1 (shape ≡ "+Main.formatAPL(x.shape)+")", this, f);
    return foldr(f.asFun(), x.values(), w, 0);
  }
  
  static Value foldr(Fun ff, Value[] a, Value init, int skip) {
    for (int i = a.length-skip-1; i >= 0; i--) {
      init = ff.call(a[i], init);
    }
    return init;
  }
}