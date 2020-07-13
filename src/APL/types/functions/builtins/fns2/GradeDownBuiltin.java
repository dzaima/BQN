package APL.types.functions.builtins.fns2;

import APL.types.Value;
import APL.types.arrs.*;
import APL.types.functions.Builtin;

public class GradeDownBuiltin extends Builtin {
  @Override public String repr() {
    return "⍒";
  }
  
  public Value call(Value x) {
    if (x instanceof BitArr && x.rank==1 && x.ia>0) {
      long[] xb = ((BitArr) x).arr;
      int[] res = new int[x.ia]; int rp = 0;
      for (int i = 0; i < res.length; i++) if ((xb[i>>6]>>(i&63)&1)!=0) res[rp++] = i;
      for (int i = 0; i < res.length; i++) if ((xb[i>>6]>>(i&63)&1)==0) res[rp++] = i;
      return new IntArr(res, x.shape);
    }
    
    Integer[] na = x.gradeDown();
    int[] res = new int[na.length];
    for (int i = 0; i < na.length; i++) res[i] = na[i];
    return new IntArr(res);
  }
}