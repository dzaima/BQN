package APL.types.functions.builtins.fns2;

import APL.*;
import APL.errors.*;
import APL.types.*;
import APL.types.arrs.IntArr;
import APL.types.functions.Builtin;
import APL.types.functions.builtins.mops.CellBuiltin;

import java.util.Arrays;

public class LBoxBuiltin extends Builtin {
  public String repr() {
    return "⊏";
  }
  
  
  
  public Value call(Value x) {
    if (x.rank==0) throw new RankError("⊏: scalar argument isn't allowed", this, x);
    if (x.ia==0) throw new LengthError("⊏: argument cannot be empty", this, x);
    int[] nsh = new int[x.rank-1];
    System.arraycopy(x.shape, 1, nsh, 0, nsh.length);
    return MutVal.cut(x, 0, Arr.prod(nsh), nsh);
  }
  
  public Value call(Value w, Value x) {
    if (x.rank==0) throw new RankError("⊏: scalar 𝕩 isn't allowed", this, x);
    if (w instanceof Num) return getCell(w.asInt(), x, this);
    
    int wr = w.shape.length;
    int xr = x.shape.length;
    if (w.ia==0) {
      int[] sh = new int[wr+xr-1];
      System.arraycopy(w.shape, 0, sh, 0, wr);
      System.arraycopy(x.shape, 1, sh, wr, xr-1);
      return Arr.create(new Value[0], sh);
    } else if (w.first() instanceof Num) {
      int[] ds = w.asIntArr();
      Value[] res = new Value[ds.length];
      for (int i = 0; i < ds.length; i++) res[i] = getCell(ds[i], x, this);
      return GTBuiltin.merge(res, w.shape, this);
    } else {
      if (wr > 1) throw new RankError("⊏: depth 2 𝕨 must be of rank 0 or 1 (shape ≡ "+Main.formatAPL(w.shape)+")", this, w);
      
      int shl = 0;
      Value[] av = w.values();
      for (Value c : av) shl+= c.rank;
      int[] sh = new int[shl + xr-w.ia];
      System.arraycopy(x.shape, w.ia, sh, shl, xr-w.ia);
      
      int cp = 0;
      for (Value c : av) {
        System.arraycopy(c.shape, 0, sh, cp, c.shape.length);
        cp+= c.rank;
      }
      int[] c = new int[w.ia];
      int csz = 1;
      for (int i = shl; i < sh.length; i++) csz*= sh[i];
      
      MutVal res = new MutVal(sh, x);
      cellRec(res, c, 0, w, x, csz, 0);
      return res.get();
    }
  }
  
  private int cellRec(MutVal res, int[] c, int i, Value w, Value x, int csz, int rp) {
    if (i==c.length) {
      int ip = 0;
      for (int j = 0; j < c.length; j++) { // +todo not
        int a = x.shape[j];
        int o = c[j];
        ip*= a;
        ip+= Indexer.scal(o, a, this);
      }
      ip*= csz;
      res.copy(x, ip, rp, csz);
      rp+= csz;
    } else {
      for (int d : w.get(i).asIntArr()) {
        c[i] = d;
        rp = cellRec(res, c, i+1, w, x, csz, rp);
      }
    }
    return rp;
  }
  
  public Value underW(Value o, Value w, Value x) {
    Value call = call(w, x);
    Value v = o instanceof Fun? ((Fun) o).call(call) : o;
    if (MatchBuiltin.full(w) > 1) throw new NYIError("⌾⊏ 1<≠≢𝕨", this, w);
    if (!Arrays.equals(call.shape, v.shape)) throw new DomainError("F⌾⊏: F didn't return equal shape array (was "+Main.formatAPL(call.shape)+", got "+Main.formatAPL(v.shape)+")");
    int[] is = w.asIntArr();
    if (x.quickIntArr() && v.quickIntArr()) {
      int[] res = x.asIntArrClone(); int[] vi = v.asIntArr();
      for (int i = 0; i < is.length; i++) res[is[i]] = vi[i];
      return new IntArr(res, x.shape);
    }
    Value[] res = x.valuesClone();
    for (int i = 0; i < is.length; i++) res[is[i]] = v.get(i);
    return Arr.create(res, x.shape);
  }
  
  public static Value getCell(int a, Value x, Callable blame) { // expects non-scalar x
    int cam = x.shape[0];        // cell amount
    int csz = CellBuiltin.csz(x);// cell size
    int start = csz*Indexer.scal(a, cam, blame);
    
    int[] sh = new int[x.rank-1];
    System.arraycopy(x.shape, 1, sh, 0, sh.length);
    return MutVal.cut(x, start, csz, sh);
  }
}