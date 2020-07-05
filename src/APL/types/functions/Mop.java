package APL.types.functions;

import APL.*;
import APL.errors.*;
import APL.types.*;

public abstract class Mop extends Callable {
  
  protected Mop(Scope sc) {
    super(sc);
  }
  protected Mop() {
    super(null);
  }
  
  @Override
  public Type type() {
    return Type.mop;
  }
  
  public Fun derive(Value aa) {
    return new DerivedMop(aa, this);
  }
  public Value call(Value f, Value w, DerivedMop derv) {
    throw new IncorrectArgsError(repr()+" can't be called monadically", derv, w);
  }
  public Value call(Value f, Value a, Value w, DerivedMop derv) {
    throw new IncorrectArgsError(repr()+" can't be called dyadically", derv, a);
  }
  
  public Value callInv(Value f, Value w) {
    throw new DomainError(this+" doesn't support monadic inverting", this, w);
  }
  public Value callInvW(Value f, Value a, Value w) {
    throw new DomainError(this+" doesn't support dyadic inverting of 𝕩", this, w);
  }
  public Value callInvA(Value f, Value a, Value w) {
    throw new DomainError(this+" doesn't support dyadic inverting of 𝕨", this, w);
  }
  public Value under(Value aa, Value o, Value w, DerivedMop derv) {
    Value v = o instanceof Fun? ((Fun) o).call(call(aa, w, derv)) : o;
    return callInv(aa, v);
  }
  public Value underW(Value aa, Value o, Value a, Value w, DerivedMop derv) {
    Value v = o instanceof Fun? ((Fun) o).call(call(aa, a, w, derv)) : o;
    return callInvW(aa, a, v);
  }
  public Value underA(Value aa, Value o, Value a, Value w, DerivedMop derv) {
    Value v = o instanceof Fun? ((Fun) o).call(call(aa, a, w, derv)) : o;
    return callInvA(aa, v, w);
  }
  
  public String toString() {
    return repr();
  }
  public abstract String repr();
  
  protected Fun isFn(Obj o) {
    if (!(o instanceof Fun)) throw new SyntaxError(repr()+": ⍶ must be a function", this);
    return (Fun) o;
  }
  
  
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