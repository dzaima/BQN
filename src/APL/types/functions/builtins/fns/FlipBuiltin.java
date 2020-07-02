package APL.types.functions.builtins.fns;

import APL.errors.DomainError;
import APL.types.*;
import APL.types.functions.Builtin;
import APL.types.functions.builtins.fns2.*;

public class FlipBuiltin extends Builtin {
  @Override public String repr() {
    return "⊖";
  }
  
  
  @Override public Value call(Value w) {
    if (w instanceof Primitive) return w;
    return ((Arr) w).reverseOn(0);
  }
  @Override public Value callInv(Value w) {
    return call(w);
  }
  
  @Override public Value call(Value a, Value w) {
    if (a instanceof Primitive) return ReverseBuiltin.on(a.asInt(), 0, w);
    throw new DomainError("A⊖B not implemented for non-scalar A", this);
  }
  
  @Override public Value callInvW(Value a, Value w) {
    return call(numM(MinusBuiltin.NF, a), w);
  }
}