package APL.types.functions.builtins.mops;

import APL.errors.*;
import APL.types.*;
import APL.types.functions.*;

public class InsertBuiltin extends Mop {
  
  public Value call(Value f, Value x, DerivedMop derv) {
    if (x.rank==0) throw new RankError("˝: argument cannot be a scalar", this, x);
    Fun ff = (Fun) f;
    Value[] vs = CellBuiltin.cells(x);
    Value c = vs[vs.length-1];
    for (int i = vs.length-2; i >= 0; i--) c = ff.call(vs[i], c);
    return c;
  }
  
  public Value call(Value f, Value w, Value x, DerivedMop derv) {
    if (x.rank==0) throw new RankError("˝: 𝕩 cannot be a scalar", this, x);
    Fun ff = (Fun) f;
    Value[] vs = CellBuiltin.cells(x);
    Value c = w;
    for (int i = vs.length-1; i >= 0; i--) c = ff.call(vs[i], c);
    return c;
  }
  
  public String repr() {
    return "˝";
  }
}
