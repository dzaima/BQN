package APL.types.functions;

import APL.*;
import APL.errors.*;
import APL.types.*;

public abstract class Dop extends Callable {
  
  protected Dop(Scope sc) {
    super(sc);
  }
  protected Dop() {
    super(null);
  }
  
  @Override
  public Type type() {
    return Type.dop;
  }
  
  public Fun derive(Value aa, Value ww) {
    return new DerivedDop(aa, ww, this);
  }
  public Value call(Value aa, Value ww, Value w, DerivedDop derv) {
    throw new IncorrectArgsError(repr()+" can't be called monadically", derv, w);
  }
  public Value call(Value aa, Value ww, Value a, Value w, DerivedDop derv) {
    throw new IncorrectArgsError(repr()+" can't be called dyadically", derv, a);
  }
  
  public Value callInv(Value aa, Value ww, Value w) {
    throw new DomainError(this+" doesn't support monadic inverting", this, w);
  }
  public Value callInvW(Value aa, Value ww, Value a, Value w) {
    throw new DomainError(this+" doesn't support dyadic inverting of ⍵", this, w);
  }
  public Value callInvA(Value aa, Value ww, Value a, Value w) {
    throw new DomainError(this+" doesn't support dyadic inverting of ⍺", this, w);
  }
  public Value under(Value aa, Value ww, Obj o, Value w, DerivedDop derv) {
    Value v = o instanceof Fun? ((Fun) o).call(call(aa, ww, w, derv)) : (Value) o;
    return callInv(aa, ww, v);
  }
  public Value underW(Value aa, Value ww, Obj o, Value a, Value w, DerivedDop derv) {
    Value v = o instanceof Fun? ((Fun) o).call(call(aa, ww, a, w, derv)) : (Value) o;
    return callInvW(aa, ww, a, v);
  }
  public Value underA(Value aa, Value ww, Obj o, Value a, Value w, DerivedDop derv) {
    Value v = o instanceof Fun? ((Fun) o).call(call(aa, ww, a, w, derv)) : (Value) o;
    return callInvA(aa, ww, v, w);
  }
  
  public String toString() {
    return repr();
  }
  public abstract String repr();
  
  protected Fun isFn(Obj o, char c) {
    if (!(o instanceof Fun)) throw new SyntaxError(repr()+": "+c+" must be a function", this);
    return (Fun) o;
  }
  
  
  public Fun asFun() {
    throw new Error("can't do");
  }
  
  // functions are equal per-object basis
  @Override public int hashCode() {
    return actualHashCode();
  }
  @Override public boolean equals(Obj o) {
    return this == o;
  }
}