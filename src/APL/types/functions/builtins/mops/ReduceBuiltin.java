package APL.types.functions.builtins.mops;

import APL.Main;
import APL.errors.*;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.dimensions.DimMMop;
import APL.types.functions.*;
import APL.types.functions.builtins.fns2.*;

public class ReduceBuiltin extends Mop implements DimMMop {
  @Override public String repr() {
    return "´";
  }
  
  
  
  @Override
  public Value call(Obj f, Value w, int dim) {
    throw new DomainError("reduce with axis doesn't exist", this);
  }
  
  public Value call(Value f, Value w, DerivedMop derv) {
    Fun ff = f.asFun();
    if (w.rank != 1) throw new DomainError("argument must have rank 1 (shape ≡ "+Main.formatAPL(w.shape)+")", this, f);
    if (w.quickDoubleArr()) {
      if (f instanceof PlusBuiltin) return new Num(w.sum());
      if (f instanceof MulBuiltin) {
        double p = 1;
        for (double d : w.asDoubleArr()) p*= d;
        return new Num(p);
      }
      if (f instanceof FloorBuiltin) {
        double p = Double.POSITIVE_INFINITY;
        for (double d : w.asDoubleArr()) p = Math.min(p, d);
        return new Num(p);
      }
      if (f instanceof CeilingBuiltin) {
        double p = Double.NEGATIVE_INFINITY;
        for (double d : w.asDoubleArr()) p = Math.max(p, d);
        return new Num(p);
      }
    }
    if (f instanceof JoinBuiltin) {
      Value joined = JoinBuiltin.joinVec(w);
      if (joined != null) return joined;
    }
    Value[] a = w.values();
    if (a.length == 0) {
      Value id = ff.identity();
      if (id == null) throw new DomainError("no identity defined for "+f.name(), this, f);
      return id;
    }
    return foldr(ff, a, a[a.length-1], 1);
  }
  
  public Value call(Value f, Value a, Value w, DerivedMop derv) {
    if (w.rank != 1) throw new DomainError("argument must have rank 1 (shape ≡ "+Main.formatAPL(w.shape)+")", this, f);
    return foldr(f.asFun(), w.values(), a, 0);
  }

  static Value foldr(Fun ff, Value[] a, Value init, int skip) {
    for (int i = a.length-skip-1; i >= 0; i--) {
      init = ff.call(a[i], init);
    }
    return init.squeeze();
  }
}
