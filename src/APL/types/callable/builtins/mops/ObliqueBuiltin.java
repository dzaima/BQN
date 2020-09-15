package APL.types.callable.builtins.mops;

import APL.errors.*;
import APL.types.Value;
import APL.types.arrs.*;
import APL.types.callable.DerivedMop;
import APL.types.callable.builtins.Md1Builtin;
import APL.types.callable.builtins.fns.GTBuiltin;

public class ObliqueBuiltin extends Md1Builtin {
  @Override public String repr() {
    return "⍁";
  }
  
  
  
  public Value call(Value f, Value x, DerivedMop derv) {
    if (x.rank != 2) throw new DomainError("⍁: 𝕩 must be a rank 2 array", this, x);
    int[] sz = x.shape;
    int H = sz[0];
    int W = sz[1];
    int szM = Math.max(H, W);
    int szm = Math.min(H, W);
    int ram = H + W - 1;
    if (ram <= 0) return new EmptyArr(EmptyArr.SHAPE0, x.safePrototype());
    
    Value[] res = new Value[ram];
    
    if (x.quickDoubleArr()) {
      double[] vals = x.asDoubleArr();
      double[][] rows = new double[ram][];
      for (int i = 0; i < ram; i++) {
        rows[i] = new double[i < szm? i + 1 : i >= szM? szm + szM - i - 1 : szm];
      }
      int p = 0;
      for (int cy = 0; cy < H; cy++) {
        for (int cx = 0; cx < W; cx++) {
          double v = vals[p++];
          int ri = cx + cy;
          int s = ri > W - 2? cy + W - ri - 1 : cy;
          rows[ri][s] = v;
        }
      }
      res[0] = f.call(new DoubleArr(rows[0]));
      int rrank = res[0].rank; // required rank
      for (int i = 0; i < ram; i++) {
        Value v = f.call(new DoubleArr(rows[i]));
        if (v.rank != rrank) throw new RankError("⍁: 𝔽 must return equal rank arrays", this, f);
        res[i] = v;
      }
    } else {
      Value[] vals = x.values();
      Value[][] rows = new Value[ram][];
      for (int i = 0; i < ram; i++) {
        rows[i] = new Value[i < szm? i + 1 : i >= szM? szm + szM - i - 1 : szm];
      }
      int p = 0;
      for (int cy = 0; cy < H; cy++) {
        for (int cx = 0; cx < W; cx++) {
          Value v = vals[p++];
          int ri = cx + cy;
          int s = ri > W - 2? cy + W - ri - 1 : cy;
          rows[ri][s] = v;
        }
      }
      res[0] = f.call(new HArr(rows[0]));
      int rrank = res[0].rank; // required rank
      for (int i = 0; i < ram; i++) {
        Value v = f.call(new HArr(rows[i]));
        if (v.rank != rrank) throw new DomainError("⍁: 𝔽 must return equal rank arrays", this, f);
        res[i] = v;
      }
    }
    
    return GTBuiltin.merge(res, new int[]{res.length}, this);
  }
}