package APL.types.functions.builtins.mops;

import APL.errors.RankError;
import APL.types.*;
import APL.types.functions.*;
import APL.types.functions.builtins.fns.OldUpArrowBuiltin;
import APL.types.functions.builtins.fns2.GTBuiltin;

public class CellBuiltin extends Mop {
  public String repr() {
    return "˘";
  }
  
  public Value call(Value f, Value w, DerivedMop derv) {
    Fun ff = f.asFun();
    if (w.rank == 0) throw new RankError(f+"˘: executed on a rank 0 array", this, w);
    int am = w.shape[0];
    int csz = w.ia/am;
    Value[] res = new Value[am];
    Value[] wv = w.values();
    int[] csh = new int[w.rank-1];
    System.arraycopy(w.shape, 1, csh, 0, csh.length);
    for (int i = 0; i < am; i++) {
      Value[] c = new Value[csz];
      System.arraycopy(wv, i*csz, c, 0, csz);
      res[i] = ff.call(Arr.create(c, csh));
    }
    return GTBuiltin.merge(res, new int[]{am}, this);
  }
}
