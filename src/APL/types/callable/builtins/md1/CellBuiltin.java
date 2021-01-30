package APL.types.callable.builtins.md1;

import APL.Main;
import APL.errors.*;
import APL.tools.*;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.callable.Md1Derv;
import APL.types.callable.builtins.Md1Builtin;
import APL.types.callable.builtins.fns.*;

import java.util.Arrays;

public class CellBuiltin extends Md1Builtin {
  
  public String ln(FmtInfo f) { return "˘"; }
  
  public Value call(Value f, Value x, Md1Derv derv) {
    if (x.r() == 0) return rq1(f.call(x));
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
    if (w.r()==0 && x.r()==0) return rq1(f.call(w, x));
    Value[] wc = w.r()==0? ext(w, x.shape[0]) : cells(w);
    Value[] xc = x.r()==0? ext(x, w.shape[0]) : cells(x);
    if (wc.length != xc.length) throw new LengthError("˘: expected first item of shape to match (shapes "+Main.formatAPL(w.shape)+" vs "+Main.formatAPL(x.shape)+")", this);
    if (wc.length == 0) return EmptyArr.SHAPE0Q;
    
    Value[] res = new Value[wc.length];
    for (int i = 0; i < res.length; i++) res[i] = f.call(wc[i], xc[i]);
    return GTBuiltin.merge(res, new int[]{res.length}, this);
  }
  
  private static Value rq1(Value x) { // require depth ≥1
    if (x instanceof Primitive) return SingleItemArr.r0(x);
    return x;
  }
  
  public Value callInv(Value f, Value x) {
    if (x.r()==0) throw new DomainError("F˘⁼: argument had rank 0", this);
    Value[] cells = cells(x);
    for (int i = 0; i < cells.length; i++) {
      Value c = f.callInv(cells[i]);
      if (c instanceof Primitive) throw new DomainError("F˘⁼: F returned an atom", this);
      cells[i] = c;
    }
    return GTBuiltin.merge(cells, new int[]{cells.length}, this);
  }
  public Value callInvX(Value f, Value w, Value x) {
    if (x.r()==0) throw new DomainError("F˘⁼: 𝕩 had rank 0", this);
    Value[] wc = w.r()==0? ext(w, x.shape[0]) : cells(w);
    Value[] xc = cells(x);
    if (wc.length != xc.length) throw new LengthError("˘: expected first item of shape to match (shapes "+Main.formatAPL(w.shape)+" vs "+Main.formatAPL(x.shape)+")", this);
    if (wc.length == 0) return EmptyArr.SHAPE0Q;
    
    Value[] res = new Value[wc.length];
    for (int i = 0; i < res.length; i++) {
      Value c = f.callInvX(wc[i], xc[i]);
      if (c instanceof Primitive) throw new DomainError("F˘⁼: F returned an atom", this);
      res[i] = c;
    }
    return GTBuiltin.merge(res, new int[]{res.length}, this);
  }
  public Value callInvW(Value f, Value w, Value x) {
    if (w.r()==0) throw new DomainError("F˘˜⁼: 𝕨 had rank 0", this);
    Value[] wc = cells(w);
    Value[] xc = x.r()==0? ext(x, w.shape[0]) : cells(x);
    if (wc.length != xc.length) throw new LengthError("˘: expected first item of shape to match (shapes "+Main.formatAPL(w.shape)+" vs "+Main.formatAPL(x.shape)+")", this);
    if (wc.length == 0) return EmptyArr.SHAPE0Q;
    
    Value[] res = new Value[wc.length];
    for (int i = 0; i < res.length; i++) {
      Value c = f.callInvW(wc[i], xc[i]);
      if (c instanceof Primitive) throw new DomainError("F˘˜⁼: F returned an atom", this);
      res[i] = c;
    }
    return GTBuiltin.merge(res, new int[]{res.length}, this);
  }
  
  private Value[] ext(Value x, int am) {
    Value[] vs = new Value[am];
    for (int i = 0; i < am; i++) vs[i] = x;
    return vs;
  }
  
  public static Value[] cells(Value x) {
    assert x.r() != 0;
    int cam = x.shape[0];
    int csz = csz(x);
    int[] csh = Arrays.copyOfRange(x.shape, 1, x.r());
    
    Value[] res = new Value[cam];
    for (int i = 0; i < cam; i++) res[i] = MutVal.cut(x, i*csz, csz, csh);
    return res;
  }
  
  
  public static int csz(Value x) {
    int csz = 1;
    int[] sh = x.shape;
    for (int i = 1; i < sh.length; i++) csz*= sh[i];
    return csz;
  }
}