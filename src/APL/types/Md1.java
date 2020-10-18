package APL.types;

import APL.errors.*;
import APL.types.callable.Md1Derv;

public abstract class Md1 extends Callable {
  
  protected Md1() { }
  
  public Value call(         Value x) { throw new SyntaxError("Cannot interpret a 1-modifier as a function", this, x); }
  public Value call(Value w, Value x) { throw new SyntaxError("Cannot interpret a 1-modifier as a function", this, x); }
  
  
  public Value derive(Value f) {
    return new Md1Derv(f, this);
  }
  public Value call(Value f, Value x, Md1Derv derv) {
    throw new IncorrectArgsError(repr()+" can't be called monadically", derv, x);
  }
  public Value call(Value f, Value w, Value x, Md1Derv derv) {
    throw new IncorrectArgsError(repr()+" can't be called dyadically", derv, w);
  }
  
  public Value callInv(Value f, Value x) {
    throw new DomainError(this+" doesn't support monadic inverting", this, x);
  }
  public Value callInvX(Value f, Value w, Value x) {
    throw new DomainError(this+" doesn't support dyadic inverting of 𝕩", this, x);
  }
  public Value callInvW(Value f, Value w, Value x) {
    throw new DomainError(this+" doesn't support dyadic inverting of 𝕨", this, x);
  }
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
  
  // functions are equal per-object basis
  public int hashCode() {
    return actualHashCode();
  }
  public boolean eq(Value o) {
    return this == o;
  }
}