package APL.types.functions.builtins.fns2;

import APL.errors.DomainError;
import APL.types.Value;
import APL.types.arrs.*;
import APL.types.functions.Builtin;
import APL.types.functions.builtins.mops.CellBuiltin;

import java.util.Arrays;

public class GradeDownBuiltin extends Builtin {
  @Override public String repr() {
    return "⍒";
  }
  
  public Value call(Value x) {
    return new IntArr(gradeDown(x));
  }
  
  
  public static int[] gradeDown(Value x) {
    if (x.rank==1 && x.ia>0) {
      if (x instanceof BitArr) {
        long[] xb = ((BitArr) x).arr;
        int[] res = new int[x.ia]; int rp = 0;
        for (int i = 0; i < res.length; i++) if ((xb[i>>6]>>(i&63)&1)!=0) res[rp++] = i;
        for (int i = 0; i < res.length; i++) if ((xb[i>>6]>>(i&63)&1)==0) res[rp++] = i;
        return res;
      }
      if (x.quickIntArr()) {
        int[] xi = x.asIntArrClone();
        int[] ri = UDBuiltin.on(xi.length);
        if (tmp.length < ri.length) tmp = new int[ri.length];
        System.arraycopy(ri, 0, tmp, 0, ri.length);
        rec(xi, tmp, ri, 0, ri.length);
        return ri;
      }
    }
    
    if (x.rank == 0) throw new DomainError("cannot grade rank 0", x);
    if (x.rank != 1) return gradeDown(new HArr(CellBuiltin.cells(x)));
    
    Integer[] na = new Integer[x.ia];
    for (int i = 0; i < na.length; i++) na[i] = i;
    Arrays.sort(na, (a, b) -> x.get(b).compareTo(x.get(a)));
    
    int[] res = new int[na.length];
    for (int i = 0; i < na.length; i++) res[i] = na[i];
    return res;
  }
  private static int[] tmp = new int[100];
  private static void rec(int[] b, int[] I, int[] O, int s, int e) {
    if (e-s<=1) return;
    int m = (s+e)/2;
    rec(b, O, I, s, m);
    rec(b, O, I, m, e);
    
    int i1 = s;
    int i2 = m;
    for (int i = s; i < e; i++) {
      if (i1<m && (i2>=e || b[I[i1]]>=b[I[i2]])) {
        O[i] = I[i1]; i1++;
      } else {
        O[i] = I[i2]; i2++;
      }
    }
  }
}