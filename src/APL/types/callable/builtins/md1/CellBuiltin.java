package APL.types.callable.builtins.md1;

import APL.Main;
import APL.errors.LengthError;
import APL.tools.MutVal;
import APL.types.*;
import APL.types.arrs.EmptyArr;
import APL.types.callable.Md1Derv;
import APL.types.callable.builtins.Md1Builtin;
import APL.types.callable.builtins.fns.*;

import java.util.Arrays;

public class CellBuiltin extends Md1Builtin {
  
  public String repr() {
    return "˘";
  }
  
  public Value call(Value f, Value x, Md1Derv derv) {
    if (x.rank == 0) return f.call(x);
    //if (w.rank == 0) throw new RankError(f+"˘: scalar 𝕩 isn't allowed", this, w);
    if (x.shape[0] == 0) return EmptyArr.SHAPE0Q;
    
    Value[] cells = cells(x);
    if (f instanceof LTBuiltin) return Arr.create(cells);
    
    for (int i = 0; i < cells.length; i++) cells[i] = f.call(cells[i]);
    return GTBuiltin.merge(cells, new int[]{cells.length}, this);
  }
  
  public Value call(Value f, Value w, Value x, Md1Derv derv) {
    // if (a.rank == 0) throw new RankError(f+"˘: scalar 𝕨 isn't allowed", this, w);
    // if (w.rank == 0) throw new RankError(f+"˘: scalar 𝕩 isn't allowed", this, w);
    // Value[] ac = cells(a);
    // Value[] wc = cells(w);
    if (w.rank==0 && x.rank==0) return f.call(w, x);
    Value[] wc = w.rank==0? ext(w, x.shape[0]) : cells(w);
    Value[] xc = x.rank==0? ext(x, w.shape[0]) : cells(x);
    if (wc.length != xc.length) throw new LengthError("˘: expected first item of shape to match (shapes "+Main.formatAPL(w.shape)+" vs "+Main.formatAPL(x.shape)+")", this);
    if (wc.length == 0) return EmptyArr.SHAPE0Q;
    
    Value[] res = new Value[wc.length];
    for (int i = 0; i < res.length; i++) res[i] = f.call(wc[i], xc[i]);
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