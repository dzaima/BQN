package APL.types.functions.builtins.mops;

import APL.errors.DomainError;
import APL.types.*;
import APL.types.functions.*;

public class SelfieBuiltin extends Mop {
  @Override public String repr() {
    return "˜";
  }
  
  
  
  public Value call(Value f, Value w, DerivedMop derv) {
    if (f instanceof Fun) return ((Fun)f).call(w, w);
    return (Value) f;
  }
  public Value call(Value f, Value a, Value w, DerivedMop derv) {
    if (f instanceof Fun) return ((Fun)f).call(w, a);
    return (Value) f;
  }
  
  @Override public Value callInvW(Value f, Value a, Value w) {
    if (f instanceof Fun) return ((Fun) f).callInvA(w, a);
    throw new DomainError("A˜ cannot be inverted", this);
  }
  
  @Override public Value callInvA(Value f, Value a, Value w) {
    if (f instanceof Fun) return ((Fun) f).callInvW(w, a);
    throw new DomainError("A˜ cannot be inverted", this);
  }
}