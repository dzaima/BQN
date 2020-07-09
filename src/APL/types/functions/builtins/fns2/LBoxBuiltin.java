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
  
  
  
  public Value call(Value w) {
    if (w.rank==0) throw new RankError("⊏: scalar 𝕩 isn't allowed", this, w);
    int ia = 1;
    int[] nsh = new int[w.rank-1];
    System.arraycopy(w.shape, 1, nsh, 0, nsh.length);
    for (int i = 1; i < w.shape.length; i++) ia*= w.shape[i];
    Value[] res = new Value[ia];
    for (int i = 0; i < ia; i++) { // valuecopy
      res[i] = w.get(i);
    }
    return Arr.create(res, nsh);
  }
  
  public Value call(Value a, Value w) {
    if (w.rank==0) throw new RankError("⊏: scalar 𝕩 isn't allowed", this, w);
    if (a instanceof Num) return getCell(a.asInt(), w, this);
  
    int ar = a.shape.length;
    int wr = w.shape.length;
    if (a.ia==0) {
      int[] sh = new int[ar+wr-1];
      System.arraycopy(a.shape, 0, sh, 0, ar);
      System.arraycopy(w.shape, 1, sh, ar, wr -1);
      return Arr.create(new Value[0], sh);
    } else if (a.get(0) instanceof Num) {
      double[] ds = a.asDoubleArr();
      Value[] res = new Value[ds.length];
      for (int i = 0; i < ds.length; i++) res[i] = getCell(Num.toInt(ds[i]), w, this);
      return GTBuiltin.merge(res, a.shape, this);
    } else {
      if (ar > 1) throw new RankError("⊏: depth 2 𝕨 must be of rank 0 or 1 (shape ≡ "+Main.formatAPL(a.shape)+")", this, a);
      
      int shl = 0;
      Value[] av = a.values();
      for (Value c : av) shl+= c.rank;
      int[] sh = new int[shl + wr-a.ia];
      System.arraycopy(w.shape, a.ia, sh, shl, wr-a.ia);
      
      int cp = 0;
      for (Value c : av) {
        System.arraycopy(c.shape, 0, sh, cp, c.shape.length);
        cp+= c.rank;
      }
      Value[] res = new Value[Arr.prod(sh)];
      int[] c = new int[a.ia];
      int csz =1;
      for (int i = shl; i < sh.length; i++) csz*= sh[i];
      cellRec(res, c, 0, a, w, csz, 0);
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
  
  public Value underW(Value o, Value a, Value w) {
    Value res = call(a, w);
    Value v = o instanceof Fun? ((Fun) o).call(res) : o;
    if (MatchBuiltin.full(a) > 1) throw new NYIError("⌾⊏ 1<≠≢𝕨", this, a);
    if (!Arrays.equals(res.shape, v.shape)) throw new DomainError("F⌾⊏: F didn't return equal shape array (was "+Main.formatAPL(res.shape)+", got "+Main.formatAPL(v.shape)+")");
    int[] is = a.asIntArr();
    Value[] vs = w.valuesCopy();
    for (int i = 0; i < is.length; i++) {
      vs[is[i]] = v.get(i);
    }
    return Arr.create(vs, w.shape);
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
