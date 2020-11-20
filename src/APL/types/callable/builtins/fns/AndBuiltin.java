package APL.types.callable.builtins.fns;

import APL.errors.RankError;
import APL.tools.*;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.callable.builtins.FnBuiltin;
import APL.types.callable.builtins.md1.CellBuiltin;

import java.util.Arrays;

public class AndBuiltin extends FnBuiltin {
  public String ln(FmtInfo f) { return "∧"; }
  
  public Value identity() {
    return Num.ONE;
  }
  
  public Value call(Value x) {
    if (x.r()==0) throw new RankError("∧: argument cannot be scalar", this);
    if (x.ia==0) return x;
    if (x.quickIntArr() && x.r()==1) {
      int[] is = x.asIntArrClone();
      Arrays.sort(is);
      return new IntArr(is, x.shape);
    }
    Value[] cells = x.r()==1? x.valuesClone() : CellBuiltin.cells(x);
    Arrays.sort(cells);
    return x.r()==1? Arr.create(cells, x.shape) : GTBuiltin.merge(cells, new int[]{x.shape[0]}, this);
  }
  
  public Pervasion.NN2N dyNum() { return MulBuiltin.DF; }
  public Value call(Value w, Value x) {
    return MulBuiltin.DF.call(w, x);
  }
  
  public Value callInvX(Value w, Value x) {
    return DivBuiltin.DF.call(x, w);
  }
  public Value callInvW(Value w, Value x) {
    return callInvX(x, w);
  }
  
  public static Num reduce(BitArr x) {
    x.setEnd(true);
    for (long l : x.arr) if (l != ~0L) return Num.ZERO;
    return Num.ONE;
  }
}