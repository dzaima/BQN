package APL.types.functions.builtins.fns2;

import APL.types.Value;
import APL.types.arrs.DoubleArr;
import APL.types.functions.Builtin;

public class GradeDownBuiltin extends Builtin {
  @Override public String repr() {
    return "⍒";
  }
  
  public Value call(Value w) {
    double[] res = new double[w.ia];
    Integer[] na = w.gradeDown();
    for (int i = 0; i < na.length; i++) {
      res[i] = na[i];
    }
    return new DoubleArr(res);
  }
}