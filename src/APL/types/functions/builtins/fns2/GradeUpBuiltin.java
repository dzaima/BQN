package APL.types.functions.builtins.fns2;

import APL.errors.DomainError;
import APL.types.Value;
import APL.types.arrs.*;
import APL.types.functions.Builtin;
import APL.types.functions.builtins.mops.CellBuiltin;

import java.util.Arrays;

public class GradeUpBuiltin extends Builtin {
  @Override public String repr() {
    return "⍋";
  }
  
  public Value call(Value x) {
    return new IntArr(gradeUp(x));
  }
  
  
  public static int[] gradeUp(Value x) {
    if (x.rank==1 && x.ia>0) {
      if (x instanceof BitArr) {
        long[] xb = ((BitArr) x).arr;
        int[] res = new int[x.ia]; int rp = 0;
        for (int i = 0; i < res.length; i++) if ((xb[i>>6]>>(i&63)&1)==0) res[rp++] = i;
        for (int i = 0; i < res.length; i++) if ((xb[i>>6]>>(i&63)&1)!=0) res[rp++] = i;
        return res;
      }
      if (x.quickIntArr()) {
        int[] xi = x.asIntArr();
        Integer[] na = new Integer[x.ia];
        for (int i = 0; i < na.length; i++) na[i] = i;
        Arrays.sort(na, (a, b) -> Integer.compare(xi[a], xi[b]));

        int[] res = new int[na.length];
        for (int i = 0; i < na.length; i++) res[i] = na[i];
        return res;
      }
    }
    
    if (x.rank == 0) throw new DomainError("cannot grade rank 0", x);
    if (x.rank != 1) return gradeUp(new HArr(CellBuiltin.cells(x)));
    
    Integer[] na = new Integer[x.ia];
    for (int i = 0; i < na.length; i++) na[i] = i;
    Arrays.sort(na, (a, b) -> x.get(a).compareTo(x.get(b)));
  
    int[] res = new int[na.length];
    for (int i = 0; i < na.length; i++) res[i] = na[i];
    return res;
  }
}

