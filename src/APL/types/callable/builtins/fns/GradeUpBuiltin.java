package APL.types.callable.builtins.fns;

import APL.Main;
import APL.errors.DomainError;
import APL.tools.FmtInfo;
import APL.types.Value;
import APL.types.arrs.*;
import APL.types.callable.builtins.FnBuiltin;
import APL.types.callable.builtins.md1.CellBuiltin;
import APL.types.callable.builtins.md2.NCellBuiltin;

import java.util.Arrays;

public class GradeUpBuiltin extends FnBuiltin {
  public String ln(FmtInfo f) { return "⍋"; }
  
  public Value call(Value x) {
    return new IntArr(gradeUp(x));
  }
  
  
  public static int[] gradeUp(Value x) {
    if (x.ia==0) return EmptyArr.NOINTS;
    if (x.r()==1) {
      if (x instanceof BitArr) {
        long[] xl = ((BitArr) x).arr;
        int[] res = new int[x.ia]; int rp = 0;
        for (int i = 0; i < res.length; i++) if ((xl[i>>6]>>(i&63)&1)==0) res[rp++] = i;
        for (int i = 0; i < res.length; i++) if ((xl[i>>6]>>(i&63)&1)!=0) res[rp++] = i;
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
    
    if (x.r() == 0) throw new DomainError("cannot grade rank 0", x);
    if (x.r() != 1) return gradeUp(new HArr(CellBuiltin.cells(x)));
    
    Integer[] na = new Integer[x.ia];
    for (int i = 0; i < na.length; i++) na[i] = i;
    Arrays.sort(na, (a, b) -> x.get(a).compareTo(x.get(b)));
  
    int[] res = new int[na.length];
    for (int i = 0; i < na.length; i++) res[i] = na[i];
    return res;
  }
  private static int[] tmp = new int[100];
  private static void rec(int[] b, int[] I, int[] O, int s, int e) { // todo do this for ∧∨
    if (e-s<=1) return;
    int m = (s+e)/2;
    rec(b, O, I, s, m);
    rec(b, O, I, m, e);
    
    int i1 = s;
    int i2 = m;
    for (int i = s; i < e; i++) {
      if (i1<m && (i2>=e || b[I[i1]]<=b[I[i2]])) {
        O[i] = I[i1]; i1++;
      } else {
        O[i] = I[i2]; i2++;
      }
    }
  }
  
  
  
  public Value call(Value w, Value x) {
    if (w.r() > x.r()+1) throw new DomainError("⍋: =𝕨 cannot be greater than =𝕩 ("+Main.formatAPL(w.shape)+"≡≢𝕨; "+Main.formatAPL(x.shape)+"≡≢𝕩)", this);
    if (w.r() == 0) throw new DomainError("⍋: 𝕨 cannot be a scalar", this, w);
    if (w.r() > 1) {
      int xr = x.r()-w.r()+1;
      x = new HArr(NCellBuiltin.cells(x, xr), Arrays.copyOf(x.shape, xr));
      w = new HArr(CellBuiltin.cells(w));
    }
    
    if (w.quickIntArr() && x.quickIntArr()) {
      int[] wi = w.asIntArr();
      int[] xi = x.asIntArr();
  
      for (int i = 0; i < w.ia-1; i++) {
        if (wi[i] > wi[i+1]) throw new DomainError("⍋: 𝕨 must be sorted", this);
      }
      
      int[] res = new int[x.ia];
      for (int i = 0; i < res.length; i++) {
        int c = xi[i];
        int s = -1, e = wi.length;
        while (e-s > 1) {
          int m = (s+e) / 2;
          if (c < wi[m]) e = m;
          else s = m;
        }
        res[i] = s+1;
      }
      return new IntArr(res, x.shape);
    }
    
    for (int i = 0; i < w.ia-1; i++) {
      if (w.get(i).compareTo(w.get(i+1)) > 0) throw new DomainError("⍋: 𝕨 must be sorted", this);
    }
    Value[] wv = w.values();
    int[] res = new int[x.ia];
    for (int i = 0; i < res.length; i++) {
      Value c = x.get(i);
      int s = -1, e = wv.length;
      while (e-s > 1) {
        int m = (s+e) / 2;
        if (c.compareTo(wv[m]) < 0) e = m;
        else s = m;
      }
      res[i] = s+1;
    }
    return new IntArr(res, x.shape);
  }
}

