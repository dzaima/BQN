package APL.types.functions.builtins.dops;

import APL.Main;
import APL.errors.*;
import APL.types.*;
import APL.types.functions.*;
import APL.types.functions.builtins.fns2.*;

import java.util.Arrays;

public class NCellBuiltin extends Dop {
  public String repr() {
    return "⎉";
  }
  
  public Value call(Value aa, Value ww, Value x, DerivedDop derv) {
    Fun aaf = aa.asFun();
    Value ra = ww.asFun().call(x);
    if (ra.rank>1) throw new RankError("⎉: rank of 𝕘 must be ≤1 (shape ≡ "+Main.formatAPL(ra.shape), this, ww);
    if (ra.ia<1 || ra.ia>3) throw new LengthError("⎉: 𝕘 must have 1 to 3 items (had "+ra.ia+")", this, ww);
    int rx = dim(ra.get(ra.ia==2? 1 : 0), x.rank);
    int[] rsh = Arrays.copyOf(x.shape, rx);
    
    Value[] cs = cells(x, rx);
    if (aaf instanceof LTBuiltin) return Arr.create(cs, rsh);
    for (int i = 0; i < cs.length; i++) cs[i] = aaf.call(cs[i]);
    return GTBuiltin.merge(cs, rsh, this);
  }
  
  public Value call(Value aa, Value ww, Value w, Value x, DerivedDop derv) {
    Fun ff = aa.asFun();
    Value ra = ww.asFun().call(w, x);
    if (ra.rank>1) throw new RankError("⎉: rank of 𝕘 must be ≤1 (shape ≡ "+ Main.formatAPL(ra.shape), this, ww);
    if (ra.ia<1 || ra.ia>3) throw new LengthError("⎉: 𝕘 must have 1 to 3 items (had "+ra.ia+")", this, ww);
    int rw = dim(ra.get(ra.ia==1? 0 : ra.ia-2), w.rank);
    int rx = dim(ra.get(ra.ia==1? 0 : ra.ia-1), x.rank);
    
    int min = Math.min(rw, rx);
    int max = Math.max(rw, rx);
    if (!Arrays.equals(x.shape, 0, min, w.shape, 0, min)) throw new LengthError("Array prefixes don't match (first "+min+" of "+Main.formatAPL(x.shape)+" vs "+Main.formatAPL(w.shape)+")", this);
    Value[] wv = cells(w, rw);
    Value[] xv = cells(x, rx);
    boolean we = rw<rx; // w is expanded
    int ext = Arr.prod((we? x : w).shape, min, max);
    int[] rsh = Arrays.copyOf((we? x : w).shape, max);
    int msz = Arr.prod(rsh, 0, min);
    Value[] n = new Value[msz*Arr.prod(rsh, min, max)];
    int r = 0;
    if (we) for (int i = 0; i < msz; i++) { Value c = wv[i]; for (int j = 0; j < ext; j++) { n[r] = ff.call(c, xv[r]); r++; } }
    else    for (int i = 0; i < msz; i++) { Value c = xv[i]; for (int j = 0; j < ext; j++) { n[r] = ff.call(wv[r], c); r++; } }
    return GTBuiltin.merge(n, rsh, this);
  }
  
  
  private int dim(Value v, int rank) {
    return rank-dim0(v, rank);
  }
  
  private int dim0(Value v, int rank) {
    int k;
    if (!(v instanceof Num)) throw new DomainError("Expected number, got "+v.humanType(false), this, v);
    double d = ((Num) v).num;
    if (d >=  rank) return rank;
    if (d <= -rank) return 0;
    if (d%1 != 0) throw new DomainError("Expected integer, got "+d, this, v);
    k = (int) d;
    if (k<0) return Math.max(k+rank, 0);
    else return Math.min(k, rank);
  }
  
  
  public static Value[] cells(Value x, int k) {
    assert x.rank != 0;
    int cam = Arr.prod(x.shape, 0, k);
    int csz = Arr.prod(x.shape, k, x.shape.length);
    int[] csh = Arrays.copyOfRange(x.shape, k, x.shape.length);
    
    Value[] xv = x.values();
    Value[] res = new Value[cam];
    for (int i = 0; i < cam; i++) {
      Value[] c = new Value[csz];
      System.arraycopy(xv, i*csz, c, 0, csz); // valuecopy
      res[i] = Arr.create(c, csh);
    }
    return res;
  }
  
}
