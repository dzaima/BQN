package BQN.types;

import BQN.errors.*;
import BQN.tools.*;
import BQN.types.callable.Md1Derv;

public abstract class Md1 extends Callable {
  
  protected Md1() { }
  
  public Value call(         Value x) { throw new SyntaxError("Cannot interpret a 1-modifier as a function", this); }
  public Value call(Value w, Value x) { throw new SyntaxError("Cannot interpret a 1-modifier as a function", this); }
  
  public Value call(Value f,          Value x, Md1Derv derv) { throw new IncorrectArgsError(ln(FmtInfo.def)+" can't be called monadically", derv); }
  public Value call(Value f, Value w, Value x, Md1Derv derv) { throw new IncorrectArgsError(ln(FmtInfo.def)+" can't be called dyadically", derv); }
  
  public Value callInv (Value f,          Value x) { throw new DomainError(ln(FmtInfo.def)+" doesn't support monadic inverting", this); }
  public Value callInvX(Value f, Value w, Value x) { throw new DomainError(ln(FmtInfo.def)+" doesn't support dyadic inverting of 𝕩", this); }
  public Value callInvW(Value f, Value w, Value x) { throw new DomainError(ln(FmtInfo.def)+" doesn't support dyadic inverting of 𝕨", this); }
  public Value under(Value f, Value o, Value x, Md1Derv derv) {
    Value v = o instanceof Fun? o.call(call(f, x, derv)) : o;
    return callInv(f, v);
  }
  public Value underW(Value f, Value o, Value w, Value x, Md1Derv derv) {
    Value v = o instanceof Fun? o.call(call(f, w, x, derv)) : o;
    return callInvX(f, w, v);
  }
  public Value underA(Value f, Value o, Value w, Value x, Md1Derv derv) {
    Value v = o instanceof Fun? o.call(call(f, w, x, derv)) : o;
    return callInvW(f, v, x);
  }
  
  public Value derive(Value f) {
    return new Md1Derv(f, this);
  }
  
  // functions in general are equal on a per-object basis
  public int hashCode() {
    return actualHashCode();
  }
  public boolean eq(Value o) {
    return this == o;
  }
  
  public Value pretty(FmtInfo f) { return Format.str(ln(f)); }
  public abstract String ln(FmtInfo f);
}