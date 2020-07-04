package APL.types.functions.builtins.mops;

import APL.Main;
import APL.errors.LengthError;
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
    if (w.rank == 0) return ff.call(w);
    //if (w.rank == 0) throw new RankError(f+"˘: scalar 𝕩 isn't allowed", this, w);
    
    Value[] cells = cells(w);
    if (f instanceof LTBuiltin) return Arr.create(cells);
    
    Value[] res = new Value[cells.length];
    for (int i = 0; i < cells.length; i++) res[i] = ff.call(cells[i]);
    return GTBuiltin.merge(res, new int[]{res.length}, this);
  }
  
  public Value call(Value f, Value a, Value w, DerivedMop derv) {
    Fun ff = f.asFun();
    // if (a.rank == 0) throw new RankError(f+"˘: scalar 𝕨 isn't allowed", this, w);
    // if (w.rank == 0) throw new RankError(f+"˘: scalar 𝕩 isn't allowed", this, w);
    // Value[] ac = cells(a);
    // Value[] wc = cells(w);
    if (a.rank==0 && w.rank==0) return ff.call(a, w);
    Value[] ac = a.rank==0? ext(a, w.shape[0]) : cells(a);
    Value[] wc = w.rank==0? ext(w, a.shape[0]) : cells(w);
    if (ac.length != wc.length) throw new LengthError("˘: expected first item of shape to match (shapes "+ Main.formatAPL(a.shape)+" vs "+Main.formatAPL(w.shape)+")", this);
    
    Value[] res = new Value[ac.length];
    for (int i = 0; i < res.length; i++) res[i] = ff.call(ac[i], wc[i]);
    return GTBuiltin.merge(res, new int[]{res.length}, this);
  }
  
  private Value[] ext(Value x, int am) {
    Value[] vs = new Value[am];
    for (int i = 0; i < am; i++) vs[i] = x;
    return vs;
  }
  
  public static Value[] cells(Value x) {
    assert x.rank != 0;
    int cam = x.shape[0];
    int csz = csz(x);
    int[] csh = Arrays.copyOfRange(x.shape, 1, x.shape.length);
    
    Value[] xv = x.values();
    Value[] res = new Value[cam];
    for (int i = 0; i < cam; i++) {
      Value[] c = new Value[csz];
      System.arraycopy(xv, i*csz, c, 0, csz); // valuecopy
      res[i] = Arr.create(c, csh);
    }
    return res;
  }
  
  
  public static int csz(Value x) {
    int csz = 1;
    for (int i = 1; i < x.shape.length; i++) csz*= x.shape[i];
    return csz;
  }
}
