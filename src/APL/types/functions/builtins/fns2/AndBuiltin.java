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
  
  public Value call(Value x) { // valuecopy
    if (x.rank==0) throw new RankError("∧: argument cannot be scalar", this, x);
    Integer[] order = x.gradeUp();
    Value[] vs = x.values();
    Value[] res = new Value[x.ia];
    int csz = CellBuiltin.csz(x);
    for (int i = 0; i < order.length; i++) System.arraycopy(vs, order[i]*csz, res, i*csz, csz);
    return Arr.create(res, x.shape);
  }
  
  public Value call(Value w, Value x) {
    return bitD(MulBuiltin.DNF, MulBuiltin.DBF, w, x);
  }
}