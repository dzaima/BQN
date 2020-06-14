package APL.types.functions.builtins.mops;

import APL.errors.NYIError;
import APL.types.*;
import APL.types.functions.*;

public class InvBuiltin extends Mop {
  
  public Value call(Value f, Value w, DerivedMop derv) {
    Fun ff = isFn(f);
    return ff.callInv(w);
  }
  public Value call(Value f, Value a, Value w, DerivedMop derv) {
    Fun ff = isFn(f);
    return ff.callInvW(a, w);
  }
  public Value callInvW(Value f, Value a, Value w) {
    Fun ff = isFn(f);
    return ff.call(a, w);
  }
  public Value callInvA(Value f, Value a, Value w) {
    throw new NYIError("⁼ inverting \uD835\uDD68", this);
  }
  
  public String repr() {
    return "⁼";
  }
}