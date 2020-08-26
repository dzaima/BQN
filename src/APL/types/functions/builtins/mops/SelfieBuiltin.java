package APL.types.functions.builtins.mops;

import APL.errors.DomainError;
import APL.types.*;
import APL.types.functions.*;

public class SelfieBuiltin extends Mop {
  @Override public String repr() {
    return "˜";
  }
  
  
  
  public Value call(Value f, Value x, DerivedMop derv) {
    return f.call(x, x);
  }
  public Value call(Value f, Value w, Value x, DerivedMop derv) {
    return f.call(x, w);
  }
  
  @Override public Value callInvW(Value f, Value w, Value x) {
    if (f instanceof Fun) return f.callInvA(x, w);
    throw new DomainError("A˜ cannot be inverted", this);
  }
  
  @Override public Value callInvA(Value f, Value w, Value x) {
    if (f instanceof Fun) return f.callInvW(x, w);
    throw new DomainError("A˜ cannot be inverted", this);
  }
}