package APL.types.callable.builtins.mops;

import APL.errors.RankError;
import APL.types.Value;
import APL.types.callable.DerivedMop;
import APL.types.callable.builtins.MopBuiltin;

public class InsertBuiltin extends MopBuiltin {
  public String repr() {
    return "˝";
  }
  
  public Value call(Value f, Value x, DerivedMop derv) {
    if (x.rank==0) throw new RankError("˝: argument cannot be a scalar", this, x);
    Value[] vs = CellBuiltin.cells(x);
    Value c = vs[vs.length-1];
    for (int i = vs.length-2; i >= 0; i--) c = f.call(vs[i], c);
    return c;
  }
  
  public Value call(Value f, Value w, Value x, DerivedMop derv) {
    if (x.rank==0) throw new RankError("˝: 𝕩 cannot be a scalar", this, x);
    Value[] vs = CellBuiltin.cells(x);
    Value c = w;
    for (int i = vs.length-1; i >= 0; i--) c = f.call(vs[i], c);
    return c;
  }
}
