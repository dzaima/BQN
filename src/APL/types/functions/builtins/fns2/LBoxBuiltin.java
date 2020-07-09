package APL.types.functions.builtins.fns2;

import APL.*;
import APL.errors.*;
import APL.types.*;
import APL.types.functions.Builtin;

import java.util.Arrays;

public class LBoxBuiltin extends Builtin {
  public String repr() {
    return "⊏";
  }
  
  
  
  public Value call(Value x) {
    if (x.rank==0) throw new RankError("⊏: scalar 𝕩 isn't allowed", this, x);
    int ia = 1;
    int[] nsh = new int[x.rank-1];
    System.arraycopy(x.shape, 1, nsh, 0, nsh.length);
    for (int i = 1; i < x.shape.length; i++) ia*= x.shape[i];
    Value[] res = new Value[ia];
    for (int i = 0; i < ia; i++) { // valuecopy
      res[i] = x.get(i);
    }
    return Arr.create(res, nsh);
  }
  
  public Value call(Value w, Value x) {
    if (x.rank==0) throw new RankError("⊏: scalar 𝕩 isn't allowed", this, x);
    if (w instanceof Num) return getCell(w.asInt(), x, this);
  
    int ar = w.shape.length;
    int wr = x.shape.length;
    if (w.ia==0) {
      int[] sh = new int[ar+wr-1];
      System.arraycopy(w.shape, 0, sh, 0, ar);
      System.arraycopy(x.shape, 1, sh, ar, wr-1);
      return Arr.create(new Value[0], sh);
    } else if (w.get(0) instanceof Num) {
      double[] ds = w.asDoubleArr();
      Value[] res = new Value[ds.length];
      for (int i = 0; i < ds.length; i++) res[i] = getCell(Num.toInt(ds[i]), x, this);
      return GTBuiltin.merge(res, w.shape, this);
    } else {
      if (ar > 1) throw new RankError("⊏: depth 2 𝕨 must be of rank 0 or 1 (shape ≡ "+Main.formatAPL(w.shape)+")", this, w);
      
      int shl = 0;
      Value[] av = w.values();
      for (Value c : av) shl+= c.rank;
      int[] sh = new int[shl + wr-w.ia];
      System.arraycopy(x.shape, w.ia, sh, shl, wr-w.ia);
      
      int cp = 0;
      for (Value c : av) {
        System.arraycopy(c.shape, 0, sh, cp, c.shape.length);
        cp+= c.rank;
      }
      Value[] res = new Value[Arr.prod(sh)];
      int[] c = new int[w.ia];
      int csz =1;
      for (int i = shl; i < sh.length; i++) csz*= sh[i];
      cellRec(res, c, 0, w, x, csz, 0);
      return Arr.create(res, sh);
    }
  }
  
  private int cellRec(Value[] res, int[] c, int i, Value w, Value x, int csz, int rp) {
    if (i==c.length) {
      int p = 0;
      for (int j = 0; j < c.length; j++) { // +todo not
        int a = x.shape[j];
        int o = c[j];
        p*= a;
        p+= Indexer.scal(o, a, this);
      }
      p*= csz;
      System.arraycopy(x.values(), p, res, rp, csz); // valuecopy, and a bad one at that
      rp+= csz;
    } else {
      for (double d : w.get(i).asDoubleArr()) {
        c[i] = Num.toInt(d);
        rp = cellRec(res, c, i+1, w, x, csz, rp);
      }
    }
    return rp;
  }
  
  public Value underW(Value o, Value w, Value x) {
    Value res = call(w, x);
    Value v = o instanceof Fun? ((Fun) o).call(res) : o;
    if (MatchBuiltin.full(w) > 1) throw new NYIError("⌾⊏ 1<≠≢𝕨", this, w);
    if (!Arrays.equals(res.shape, v.shape)) throw new DomainError("F⌾⊏: F didn't return equal shape array (was "+Main.formatAPL(res.shape)+", got "+Main.formatAPL(v.shape)+")");
    int[] is = w.asIntArr();
    Value[] vs = x.valuesCopy();
    for (int i = 0; i < is.length; i++) {
      vs[is[i]] = v.get(i);
    }
    return Arr.create(vs, x.shape);
  }
  
  public static Value getCell(int a, Value x, Callable blame) { // expects non-scalar x
    int cam = x.shape[0]; // cell amount
    int csz = x.ia/cam;   // cell size
    int start = csz*Indexer.scal(a, cam, blame);
    
    int[] sh = new int[x.rank-1];
    System.arraycopy(x.shape, 1, sh, 0, sh.length);
    Value[] res = new Value[csz];
    for (int i = 0; i < csz; i++) res[i] = x.get(i + start); // valuecopy
    return Arr.create(res, sh);
  }
}
