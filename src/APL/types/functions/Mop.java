package APL.types.functions;

import APL.errors.*;
import APL.types.*;

public abstract class Mop extends Callable {
  
  protected Mop() { }
  
  
  public Value derive(Value f) {
    return new DerivedMop(f, this);
  }
  public Value call(Value f, Value x, DerivedMop derv) {
    throw new IncorrectArgsError(repr()+" can't be called monadically", derv, x);
  }
  public Value call(Value f, Value w, Value x, DerivedMop derv) {
    throw new IncorrectArgsError(repr()+" can't be called dyadically", derv, w);
  }
  
  public Value callInv(Value f, Value x) {
    throw new DomainError(this+" doesn't support monadic inverting", this, x);
  }
  public Value callInvW(Value f, Value w, Value x) {
    throw new DomainError(this+" doesn't support dyadic inverting of 𝕩", this, x);
  }
  public Value callInvA(Value f, Value w, Value x) {
    throw new DomainError(this+" doesn't support dyadic inverting of 𝕨", this, x);
  }
  public Value under(Value f, Value o, Value x, DerivedMop derv) {
    Value v = o instanceof Fun? ((Fun) o).call(call(f, x, derv)) : o;
    return callInv(f, v);
  }
  public Value underW(Value f, Value o, Value w, Value x, DerivedMop derv) {
    Value v = o instanceof Fun? ((Fun) o).call(call(f, w, x, derv)) : o;
    return callInvW(f, w, v);
  }
  public Value underA(Value f, Value o, Value w, Value x, DerivedMop derv) {
    Value v = o instanceof Fun? ((Fun) o).call(call(f, w, x, derv)) : o;
    return callInvA(f, v, x);
  }
  
  public String toString() {
    return repr();
  }
  public abstract String repr();
  
  
  public Fun asFun() {
    throw new SyntaxError("Cannot interpret a modifier as a function");
  }
  public boolean notIdentity() { return true; }
  
  // functions are equal per-object basis
  public int hashCode() {
    return actualHashCode();
  }
  public boolean eq(Value o) {
    return this == o;
  }
}