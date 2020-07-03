package APL.types.functions.builtins.mops;

import APL.errors.RankError;
import APL.types.*;
import APL.types.functions.*;
import APL.types.functions.builtins.fns2.*;

import java.util.Arrays;

public class CellBuiltin extends Mop {
  public String repr() {
    return "˘";
  }
  
  public Value call(Value f, Value w, DerivedMop derv) {
    Fun ff = f.asFun();
    if (w.rank == 0) throw new RankError(f+"˘: scalar 𝕩 isn't allowed", this, w);
  
    Value[] cells = cells(w);
    if (f instanceof LTBuiltin) return Arr.create(cells);
    
    Value[] res = new Value[cells.length];
    for (int i = 0; i < cells.length; i++) res[i] = ff.call(cells[i]);
    return GTBuiltin.merge(res, new int[]{res.length}, this);
  }
  
  public static Value[] cells(Value x) {
    assert x.rank != 0;
    int cam = x.shape[0];
    int csz = cam==0? 0 : x.ia/cam;
    int[] csh = Arrays.copyOfRange(x.shape, 1, x.shape.length);
    
    Value[] xv = x.values();
    Value[] res = new Value[cam];
    for (int i = 0; i < cam; i++) {
      Value[] c = new Value[csz];
      System.arraycopy(xv, i*csz, c, 0, csz);
      res[i] = Arr.create(c, csh);
    }
    return res;
  }
}
