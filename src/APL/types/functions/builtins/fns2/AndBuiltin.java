package APL.types.functions.builtins.fns2;

import APL.errors.RankError;
import APL.types.*;
import APL.types.functions.Builtin;
import APL.types.functions.builtins.mops.CellBuiltin;

import java.util.Arrays;

public class AndBuiltin extends Builtin {
  @Override public String repr() {
    return "∧";
  }
  
  
  
  public Value identity() {
    return Num.ONE;
  }
  
  public Value call(Value x) { // valuecopy
    if (x.rank==0) throw new RankError("∧: argument cannot be scalar", this, x);
    Value[] cells = x.rank==1? x.valuesClone() : CellBuiltin.cells(x);
    Arrays.sort(cells);
    return x.rank==1? Arr.create(cells, x.shape) : GTBuiltin.merge(cells, new int[]{x.shape[0]}, this);
  }
  
  public Value call(Value w, Value x) {
    return MulBuiltin.DF.call(w, x);
  }
}