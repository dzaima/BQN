package APL.types.callable.builtins.md2;

import APL.Main;
import APL.errors.*;
import APL.tools.MutVal;
import APL.types.*;
import APL.types.arrs.EmptyArr;
import APL.types.callable.Md2Derv;
import APL.types.callable.builtins.Md2Builtin;
import APL.types.callable.builtins.fns.*;

import java.util.Arrays;

public class NCellBuiltin extends Md2Builtin {
  public String repr() {
    return "⎉";
  }
  
  public Value call(Value f, Value g, Value x, Md2Derv derv) {
    Value ra = g.call(x);
    if (ra.rank>1) throw new RankError("⎉: rank of 𝕘 must be ≤1 (shape ≡ "+Main.formatAPL(ra.shape), this, g);
    if (ra.ia<1 || ra.ia>3) throw new LengthError("⎉: 𝕘 must have 1 to 3 items (had "+ra.ia+")", this, g);
    int rx = dim(ra.get(ra.ia==2? 1 : 0), x.rank);
    int[] rsh = Arrays.copyOf(x.shape, rx);
    
    Value[] cs = cells(x, rx);
    if (cs.length==0) return new EmptyArr(rsh, null);
    if (f instanceof LTBuiltin) return Arr.create(cs, rsh);
    for (int i = 0; i < cs.length; i++) cs[i] = f.call(cs[i]);
    return GTBuiltin.merge(cs, rsh, this);
  }
  
  public Value call(Value f, Value g, Value w, Value x, Md2Derv derv) {
    Value ra = g.call(w, x);
    if (ra.rank>1) throw new RankError("⎉: rank of 𝕘 must be ≤1 (shape ≡ "+Main.formatAPL(ra.shape), this, g);
    if (ra.ia<1 || ra.ia>3) throw new LengthError("⎉: 𝕘 must have 1 to 3 items (had "+ra.ia+")", this, g);
    int rw = dim(ra.get(ra.ia==1? 0 : ra.ia-2), w.rank);
    int rx = dim(ra.get(ra.ia==1? 0 : ra.ia-1), x.rank);
    
    int min = Math.min(rw, rx);
    int max = Math.max(rw, rx);
    if (!Arr.eqPrefix(x.shape, w.shape, min)) throw new LengthError("Array prefixes don't match (first "+min+" of "+Main.formatAPL(x.shape)+" vs "+Main.formatAPL(w.shape)+")", this);
    Value[] wv = cells(w, rw);
    Value[] xv = cells(x, rx);
    boolean we = rw<rx; // w is expanded
    int ext = Arr.prod((we? x : w).shape, min, max);
    int[] rsh = Arrays.copyOf((we? x : w).shape, max);
    
    int msz = Arr.prod(rsh, 0, min);
    Value[] n = new Value[msz*Arr.prod(rsh, min, max)];
    if (n.length==0) return new EmptyArr(rsh, null);
    int r = 0;
    if (we) for (int i = 0; i < msz; i++) { Value c = wv[i]; for (int j = 0; j < ext; j++) { n[r] = f.call(c, xv[r]); r++; } }
    else    for (int i = 0; i < msz; i++) { Value c = xv[i]; for (int j = 0; j < ext; j++) { n[r] = f.call(wv[r], c); r++; } }
    return GTBuiltin.merge(n, rsh, this);
  }
  
  
  private int dim(Value v, int rank) {
    if (!(v instanceof Num)) throw new DomainError("Expected number, got "+v.humanType(false), this, v);
    double d = ((Num) v).num;
    if (d==0 && Double.doubleToRawLongBits(d)!=0) return 0;
    if (d >=  rank) return 0;
    if (d <= -rank) return rank;
    if (d%1 != 0) throw new DomainError("Expected integer, got "+d, this, v);
    int k = (int) d;
    if (k<0) return Math.min(-k, rank);
    else return Math.max(rank-k, 0);
  }
  
  
  public static Value[] cells(Value x, int k) { // k is amount of leading dimensions to squash
    int cam = Arr.prod(x.shape, 0, k);
    int[] csh = Arrays.copyOfRange(x.shape, k, x.shape.length);
    int csz = Arr.prod(csh, 0, csh.length);
    
    Value[] res = new Value[cam];
    for (int i = 0; i < cam; i++) res[i] = MutVal.cut(x, i*csz, csz, csh);
    return res;
  }
  
}