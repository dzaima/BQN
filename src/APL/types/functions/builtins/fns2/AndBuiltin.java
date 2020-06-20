package APL.types.functions.builtins.fns2;

import APL.types.*;
import APL.types.functions.Builtin;

import java.util.Arrays;

public class AndBuiltin extends Builtin {
  @Override public String repr() {
    return "∧";
  }
  
  
  
  public Value identity() {
    return Num.ONE;
  }
  
  public Value call(Value w) {
    var order = w.gradeUp();
    Value[] res = new Value[order.length];
    Arrays.setAll(res, i -> w.get(order[i]));
    return Arr.create(res);
  }
  
  public Value call(Value a, Value w) {
    return bitD(MulBuiltin.DNF, MulBuiltin.DBF, a, w);
  }
}