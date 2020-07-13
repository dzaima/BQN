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
  
  public Value call(Value f, Value x, DerivedMop derv) {
    Fun ff = f.asFun();
    if (x.rank == 0) return ff.call(x);
    //if (w.rank == 0) throw new RankError(f+"˘: scalar 𝕩 isn't allowed", this, w);
    
    Value[] cells = cells(x);
    if (f instanceof LTBuiltin) return Arr.create(cells);
    
    for (int i = 0; i < cells.length; i++) cells[i] = ff.call(cells[i]);
    return GTBuiltin.merge(cells, new int[]{cells.length}, this);
  }
  
  public Value call(Value f, Value w, Value x, DerivedMop derv) {
    Fun ff = f.asFun();
    // if (a.rank == 0) throw new RankError(f+"˘: scalar 𝕨 isn't allowed", this, w);
    // if (w.rank == 0) throw new RankError(f+"˘: scalar 𝕩 isn't allowed", this, w);
    // Value[] ac = cells(a);
    // Value[] wc = cells(w);
    if (w.rank==0 && x.rank==0) return ff.call(w, x);
    Value[] wc = w.rank==0? ext(w, x.shape[0]) : cells(w);
    Value[] xc = x.rank==0? ext(x, w.shape[0]) : cells(x);
    if (wc.length != xc.length) throw new LengthError("˘: expected first item of shape to match (shapes "+Main.formatAPL(w.shape)+" vs "+Main.formatAPL(x.shape)+")", this);
    
    Value[] res = new Value[wc.length];
    for (int i = 0; i < res.length; i++) res[i] = ff.call(wc[i], xc[i]);
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
    
    Value[] res = new Value[cam];
    for (int i = 0; i < cam; i++) res[i] = MutVal.cut(x, i*csz, csz, csh);
    return res;
  }
  
  
  public static int csz(Value x) {
    int csz = 1;
    for (int i = 1; i < x.shape.length; i++) csz*= x.shape[i];
    return csz;
  }
}