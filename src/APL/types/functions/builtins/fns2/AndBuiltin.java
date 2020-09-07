package APL.types.functions.builtins.fns2;

import APL.errors.RankError;
import APL.tools.Pervasion;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.functions.Builtin;
import APL.types.functions.builtins.mops.CellBuiltin;

import java.util.Arrays;

public class AndBuiltin extends Builtin {
  @Override public String repr() {
    return "∧";
  }
  
  
  
  public Value identity() {
    return Num.ONE;
  }
  
  public Value call(Value x) {
    if (x.rank==0) throw new RankError("∧: argument cannot be scalar", this, x);
    if (x.ia==0) return x;
    if (x instanceof IntArr && x.rank==1) {
      int[] is = x.asIntArrClone();
      Arrays.sort(is);
      return new IntArr(is, x.shape);
    }
    Value[] cells = x.rank==1? x.valuesClone() : CellBuiltin.cells(x);
    Arrays.sort(cells);
    return x.rank==1? Arr.create(cells, x.shape) : GTBuiltin.merge(cells, new int[]{x.shape[0]}, this);
  }
  
  public Pervasion.NN2N dyNum() { return MulBuiltin.DF; }
  public Value call(Value w, Value x) {
    return MulBuiltin.DF.call(w, x);
  }
  
  public Value callInvW(Value w, Value x) {
    return DivBuiltin.DF.call(x, w);
  }
  public Value callInvA(Value w, Value x) {
    return callInvW(x, w);
  }
  
  public static Num reduce(BitArr x) {
    x.setEnd(true);
    for (long l : x.arr) if (l != ~0L) return Num.ZERO;
    return Num.ONE;
  }
}