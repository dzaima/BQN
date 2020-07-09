package APL.types.functions.builtins.fns;

import APL.errors.DomainError;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.functions.Builtin;

public class SemiUBBuiltin extends Builtin {
  @Override public String repr() {
    return "⍮";
  }
  
  
  
  @Override
  public Value call(Value x) {
    return new Shape1Arr(x);
  }
  
  public Value call(Value w, Value x) {
    if (w instanceof Num && x instanceof Num) {
      return new DoubleArr(new double[]{((Num) w).num, ((Num) x).num});
    }
    if (w instanceof Char && x instanceof Char) {
      return new ChrArr(((Char) w).chr+""+((Char) x).chr);
    }
    return Arr.create(new Value[]{w, x});
  }
  
  public Value callInv(Value x) {
    if (x.rank!=1 || x.shape[0]!=1) throw new DomainError("⍮⁼: argument must be a length 1 vector", this, x);
    return x.first();
  }
  
  public Value callInvW(Value w, Value x) {
    if (x.rank!=1 || x.shape[0]!=2) throw new DomainError("⍮⁼: 𝕩 must be a length 2 vector", this, x);
    if (!x.get(0).equals(w)) throw new DomainError("⍮⁼: expected 𝕨≡⊑𝕩", this, x);
    return x.get(1);
  }
  public Value callInvA(Value w, Value x) {
    if (w.rank!=1 || w.shape[0]!=2) throw new DomainError("⍮˜⁼: 𝕨 must be a length 2 vector", this, w);
    if (!w.get(1).equals(x)) throw new DomainError("⍮˜⁼: expected 𝕩≡1⊑𝕨", this, w);
    return w.get(0);
  }
}