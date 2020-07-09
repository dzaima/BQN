package APL.types.functions.builtins.fns2;

import APL.Indexer;
import APL.errors.RankError;
import APL.types.Value;
import APL.types.arrs.BitArr;
import APL.types.functions.Builtin;

public class FindBuiltin extends Builtin {
  @Override public String repr() {
    return "⍷";
  }
  
  
  
  public Value call(Value w, Value x) {
    if (w.rank != x.rank) throw new RankError("⍷: argument ranks should be equal ("+w.rank+" ≠ "+x.rank+")", this, x);
    BitArr.BC bc = new BitArr.BC(x.shape);
    if (w.rank == 1) {
      if (w instanceof BitArr && x instanceof BitArr) {
        long[] ab = ((BitArr) w).arr;
        long[] wb = ((BitArr) x).arr;
        w: for (int ir = 0; ir < x.ia- w.ia+1; ir++) {
          for (int ia = 0; ia < w.ia; ia++) {
            int iw = ia + ir;
            long la = ab[ia>>6] >> (ia & 63);
            long lw = wb[iw>>6] >> (iw & 63);
            if ((la&1) != (lw&1)) continue w;
          }
          bc.set(ir);
        }
      } else if (w.quickDoubleArr() && x.quickDoubleArr()) {
        double[] wd = w.asDoubleArr();
        double[] xd = x.asDoubleArr();
        w: for (int ir = 0; ir < x.ia-w.ia+1; ir++) {
          for (int ia = 0; ia < w.ia; ia++) {
            if (wd[ia] != xd[ia + ir]) continue w;
          }
          bc.set(ir);
        }
      } else {
        w: for (int ir = 0; ir < x.ia-w.ia+1; ir++) {
          for (int ia = 0; ia < w.ia; ia++) {
            if (!w.get(ia).equals(x.get(ia + ir))) continue w;
          }
          bc.set(ir);
        }
      }
    } else {
      Indexer ind = new Indexer(Indexer.add(Indexer.sub(x.shape, w.shape), 1));
      w: for (int[] inW : ind) {
        for (int[] inA : new Indexer(w.shape)) {
          Value vA = w.simpleAt(inA);
          Value vW = x.simpleAt(Indexer.add(inA, inW));
          if (!vA.equals(vW)) continue w;
        }
        bc.set(Indexer.fromShape(x.shape, inW));
      }
    }
    return bc.finish();
  }
}