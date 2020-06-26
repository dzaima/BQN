package APL.types.functions.builtins.fns;

import APL.errors.*;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.dimensions.*;
import APL.types.functions.Builtin;
import APL.types.functions.builtins.fns2.*;

public class OldCatBuiltin extends Builtin implements DimDFn {
  @Override public String repr() {
    return ",";
  }
  
  
  public Value call(Value w) {
    if (w instanceof Primitive) {
      if (w instanceof Num) return new DoubleArr(new double[]{((Num) w).num});
      if (w instanceof Char) return new ChrArr(String.valueOf(((Char) w).chr));
      return new Shape1Arr(w);
    }
    return w.ofShape(new int[]{w.ia});
  }
  public Value call(Value a, Value w) {
    int dim = Math.max(a.rank, w.rank) - 1;
    return JoinBuiltin.cat(a, w, dim, this);
  }
  public Value call(Value a, Value w, DervDimFn dims) {
    int dim = dims.singleDim();
    if (dim < 0 || dim >= Math.max(a.rank, w.rank)) throw new DomainError("dimension "+dim+" is out of range", this);
    return JoinBuiltin.cat(a, w, dim, this);
  }
  
  
  public Value under(Value o, Value w) {
    Value v = o instanceof Fun? ((Fun) o).call(call(w)) : (Value) o;
    if (v.ia != w.ia) throw new DomainError("⌾, expected equal amount of output & output items", this);
    return v.ofShape(w.shape);
  }
  
  public Value underW(Value o, Value a, Value w) {
    Value v = o instanceof Fun? ((Fun) o).call(call(a, w)) : (Value) o;
    if (a.rank>1) throw new NYIError(", inverted on rank "+a.rank+" ⍺", this);
    if (v.rank>1) throw new NYIError(", inverted on rank "+v.rank+" ⍵", this);
    for (int i = 0; i < a.ia; i++) {
      if (a.get(i) != v.get(i)) throw new DomainError("inverting , received non-equal prefixes", this);
    }
    if (w.rank==0) {
      if (a.ia+1 != v.ia) throw new DomainError("original ⍵ was of rank ⍬, which is not satisfiable", this);
      return v.get(v.ia-1);
    }
    return UpArrowBuiltin.on(new int[]{v.ia-a.ia}, new int[]{a.ia}, v, this);
  }
}