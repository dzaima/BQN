package APL.types.functions.builtins.fns2;

import APL.errors.RankError;
import APL.types.*;
import APL.types.functions.Builtin;
import APL.types.functions.builtins.mops.CellBuiltin;

public class AndBuiltin extends Builtin {
  @Override public String repr() {
    return "∧";
  }
  
  
  
  public Value identity() {
    return Num.ONE;
  }
  
  public Value call(Value w) { // valuecopy
    if (w.rank==0) throw new RankError("∧: argument cannot be scalar", this, w);
    Integer[] order = w.gradeUp();
    Value[] vs = w.values();
    Value[] res = new Value[w.ia];
    int csz = CellBuiltin.csz(w);
    for (int i = 0; i < order.length; i++) System.arraycopy(vs, order[i]*csz, res, i*csz, csz);
    return Arr.create(res, w.shape);
  }
  
  public Value call(Value a, Value w) {
    return bitD(MulBuiltin.DNF, MulBuiltin.DBF, a, w);
  }
}