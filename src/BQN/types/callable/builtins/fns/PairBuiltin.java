package BQN.types.callable.builtins.fns;

import BQN.Main;
import BQN.errors.DomainError;
import BQN.tools.FmtInfo;
import BQN.types.*;
import BQN.types.arrs.*;
import BQN.types.callable.builtins.FnBuiltin;
import BQN.types.callable.builtins.md1.CellBuiltin;

import java.util.Arrays;

public class PairBuiltin extends FnBuiltin {
  public String ln(FmtInfo f) { return "⋈"; }
  
  public Value call(Value w, Value x) {
    if (w instanceof Num && x instanceof Num) {
      double wd = w.asDouble();
      double xd = x.asDouble();
      return wd==(int)wd && xd==(int)xd? new IntArr(new int[]{(int)wd,(int)xd}) : new DoubleArr(new double[]{wd,xd});
    }
    if (w instanceof Char && x instanceof Char) return new ChrArr(w.asChar()+""+x.asChar());
    return Arr.create(new Value[]{w, x});
  }
  
  public Value call(Value x) {
    if (x instanceof Num) {
      double xd = x.asDouble();
      return xd==(int)xd? new IntArr(new int[]{(int)xd}) : new DoubleArr(new double[]{xd});
    }
    if (x instanceof Char) return new ChrArr(Character.toString(x.asChar()));
    return Arr.create(new Value[]{x});
  }
  
  public Value callInv(Value x) {
    if (x.r()!=1 || x.shape[0]!=1) throw new DomainError("⋈⁼: Argument should be a length 1 vector, but had shape "+Main.fArr(x.shape), this);
    return x.get(0);
  }
  
  public Value callInvX(Value w, Value x) {
    if (x.r()!=1 || x.shape[0]!=2) throw new DomainError("⋈⁼: 𝕩 must be a length 2 vector, but had shape "+Main.fArr(x.shape), this);
    if (!x.get(0).eq(w)) throw new DomainError("⋈⁼: 𝕨 didn't match expected", this);
    return x.get(1);
  }
  
  public Value callInvW(Value w, Value x) {
    if (w.r()!=1 || w.shape[0]!=2) throw new DomainError("⋈˜⁼: 𝕨 must be a length 2 vector, but had shape "+Main.fArr(w.shape), this);
    if (!w.get(1).eq(x)) throw new DomainError("⋈˜⁼: 𝕩 didn't match expected", this);
    return w.get(0);
  }
}