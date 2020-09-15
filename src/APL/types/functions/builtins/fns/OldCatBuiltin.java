package APL.types.functions.builtins.fns;

import APL.errors.*;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.functions.builtins.FnBuiltin;
import APL.types.functions.builtins.fns2.*;

public class OldCatBuiltin extends FnBuiltin {
  @Override public String repr() {
    return ",";
  }
  
  
  public Value call(Value x) {
    if (x instanceof Primitive) {
      if (x instanceof Num) return new DoubleArr(new double[]{((Num) x).num});
      if (x instanceof Char) return new ChrArr(String.valueOf(((Char) x).chr));
      return SingleItemArr.sh1(x);
    }
    return x.ofShape(new int[]{x.ia});
  }
  public Value call(Value w, Value x) {
    int dim = Math.max(w.rank, x.rank) - 1;
    return JoinBuiltin.cat(w, x, dim, this);
  }
  // public Value call(Value a, Value w, DervDimFn dims) {
  //   int dim = dims.singleDim();
  //   if (dim < 0 || dim >= Math.max(a.rank, w.rank)) throw new DomainError("dimension "+dim+" is out of range", this);
  //   return JoinBuiltin.cat(a, w, dim, this);
  // }
  
  
  public Value under(Value o, Value x) {
    Value v = o instanceof Fun? o.call(call(x)) : o;
    if (v.ia != x.ia) throw new DomainError("⌾, expected equal amount of output & output items", this);
    return v.ofShape(x.shape);
  }
  
  public Value underW(Value o, Value w, Value x) {
    Value v = o instanceof Fun? o.call(call(w, x)) : o;
    if (w.rank>1) throw new NYIError(", inverted on rank "+w.rank+" 𝕨", this);
    if (v.rank>1) throw new NYIError(", inverted on rank "+v.rank+" 𝕩", this);
    for (int i = 0; i < w.ia; i++) {
      if (w.get(i) != v.get(i)) throw new DomainError("inverting , received non-equal prefixes", this);
    }
    if (x.rank==0) {
      if (w.ia+1 != v.ia) throw new DomainError("original 𝕩 was of rank 0, which is not satisfiable", this);
      return v.get(v.ia-1);
    }
    return UpArrowBuiltin.on(new int[]{v.ia-w.ia}, new int[]{w.ia}, v);
  }
}