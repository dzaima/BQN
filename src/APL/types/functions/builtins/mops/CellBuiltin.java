package APL.types.functions.builtins.mops;

import APL.errors.RankError;
import APL.types.*;
import APL.types.functions.*;
import APL.types.functions.builtins.fns2.GTBuiltin;

public class CellBuiltin extends Mop {
  public String repr() {
    return "˘";
  }
  
  public Value call(Value f, Value w, DerivedMop derv) {
    Fun ff = f.asFun();
    if (w.rank == 0) throw new RankError(f+"˘: scalar 𝕩 isn't allowed", this, w);
    int am = w.shape[0];
    
    int csz = 1;
    for (int i = 1; i < w.shape.length; i++) csz*= w.shape[i];
    int[] csh = new int[w.rank-1];
    System.arraycopy(w.shape, 1, csh, 0, csh.length);
    
    Value[] res = new Value[am];
    Value[] wv = w.values();
    for (int i = 0; i < am; i++) {
      Value[] c = new Value[csz];
      System.arraycopy(wv, i*csz, c, 0, csz);
      Arr a = Arr.create(c, csh);
      res[i] = ff.call(a);
    }
    return GTBuiltin.merge(res, new int[]{am}, this);
  }
}
