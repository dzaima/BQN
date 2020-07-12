package APL.types.functions.builtins.fns2;

import APL.types.Value;
import APL.types.arrs.*;
import APL.types.functions.Builtin;

public class GradeUpBuiltin extends Builtin {
  @Override public String repr() {
    return "⍋";
  }
  
  public Value call(Value x) {
    Integer[] na = x.gradeUp();
    int[] res = new int[na.length];
    for (int i = 0; i < na.length; i++) res[i] = na[i];
    return new IntArr(res);
  }
}