package APL.types.functions;

import APL.Type;
import APL.errors.*;
import APL.types.*;

public abstract class Mop extends Callable {
  
  protected Mop() { }
  
  @Override
  public Type type() {
    return Type.mop;
  }
  
  public Fun derive(Value f) {
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
  public Value under(Value aa, Value o, Value x, DerivedMop derv) {
    Value v = o instanceof Fun? ((Fun) o).call(call(aa, x, derv)) : o;
    return callInv(aa, v);
  }
  public Value underW(Value aa, Value o, Value w, Value x, DerivedMop derv) {
    Value v = o instanceof Fun? ((Fun) o).call(call(aa, w, x, derv)) : o;
    return callInvW(aa, w, v);
  }
  public Value underA(Value aa, Value o, Value w, Value x, DerivedMop derv) {
    Value v = o instanceof Fun? ((Fun) o).call(call(aa, w, x, derv)) : o;
    return callInvA(aa, v, x);
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
  @Override public int hashCode() {
    return actualHashCode();
  }
  @Override public boolean equals(Obj o) {
    return this == o;
  }
}