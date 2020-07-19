package APL.types.functions.builtins.fns2;

import APL.Main;
import APL.errors.*;
import APL.tools.*;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.functions.Builtin;
import APL.types.functions.builtins.mops.CellBuiltin;

import java.util.Arrays;

public class LBoxBuiltin extends Builtin {
  public String repr() {
    return "⊏";
  }
  
  
  
  public Value call(Value x) {
    if (x.shape.length==0) throw new RankError("⊏: scalar argument isn't allowed", this, x);
    if (x.shape[0]==0) throw new LengthError("⊏: argument shape cannot start with 0 (had shape "+Main.formatAPL(x.shape)+")", this, x);
    int[] nsh = new int[x.rank-1];
    System.arraycopy(x.shape, 1, nsh, 0, nsh.length);
    return MutVal.cut(x, 0, Arr.prod(nsh), nsh);
  }
  
  public Value call(Value w, Value x) {
    if (x.rank==0) throw new RankError("⊏: scalar 𝕩 isn't allowed", this, x);
    if (w instanceof Num) return getCell(w.asInt(), x, this);
    
    int wr = w.shape.length;
    int xr = x.shape.length;
    if (w.ia==0 || w.quickDepth1() || w.first() instanceof Num) {
      int[] sh = new int[wr+xr-1];
      System.arraycopy(w.shape, 0, sh, 0, wr);
      System.arraycopy(x.shape, 1, sh, wr, xr-1);
      int[] wi = w.asIntArr();
      if (w.rank==1 && x.rank==1) {
        if (x.quickIntArr()) {
          int[] xi = x.asIntArr();
          int[] res = new int[w.ia];
          for (int i = 0; i < wi.length; i++) {
            res[i] = xi[wi[i]];
          }
          return new IntArr(res, sh);
        }
        if (x.quickDoubleArr()) {
          double[] xd = x.asDoubleArr();
          double[] res = new double[w.ia];
          for (int i = 0; i < wi.length; i++) {
            res[i] = xd[wi[i]];
          }
          return new DoubleArr(res, sh);
        }
      }
      MutVal res = new MutVal(sh);
      int csz = CellBuiltin.csz(x);
      for (int i = 0; i < wi.length; i++) res.copy(getCell(wi[i], x, this), 0, csz*i, csz);
      return res.get();
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