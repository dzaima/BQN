package APL.types.functions.builtins.fns;

import APL.types.*;
import APL.types.functions.Builtin;
import APL.types.functions.builtins.fns2.MinusBuiltin;

public class FlipBuiltin extends Builtin {
  @Override public String repr() {
    return "⊖";
  }
  
  
  @Override public Value call(Value x) {
    if (x instanceof Primitive) return x;
    return ((Arr) x).reverseOn(0);
  }
  @Override public Value callInv(Value x) {
    return call(x);
  }
  
  @Override public Value call(Value w, Value x) {
    // if (a instanceof Primitive) return ReverseBuiltin.on(a.asInt(), 0, w);
    // throw new DomainError("A⊖B not implemented for non-scalar A", this);
    throw new AssertionError();
  }
  
  @Override public Value callInvX(Value w, Value x) {
    return call(numM(MinusBuiltin.NF, w), x);
  }
}