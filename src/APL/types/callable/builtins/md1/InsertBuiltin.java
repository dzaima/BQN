package APL.types.callable.builtins.md1;

import APL.errors.RankError;
import APL.tools.FmtInfo;
import APL.types.Value;
import APL.types.callable.Md1Derv;
import APL.types.callable.builtins.Md1Builtin;

public class InsertBuiltin extends Md1Builtin {
  public String ln(FmtInfo f) { return "˝"; }
  
  public Value call(Value f, Value x, Md1Derv derv) {
    if (x.r()==0) throw new RankError("˝: argument cannot be a scalar", this);
    Value[] vs = CellBuiltin.cells(x);
    Value c = vs[vs.length-1];
    for (int i = vs.length-2; i >= 0; i--) c = f.call(vs[i], c);
    return c;
  }
  
  public Value call(Value f, Value w, Value x, Md1Derv derv) {
    if (x.r()==0) throw new RankError("˝: 𝕩 cannot be a scalar", this);
    Value[] vs = CellBuiltin.cells(x);
    Value c = w;
    for (int i = vs.length-1; i >= 0; i--) c = f.call(vs[i], c);
    return c;
  }
}
