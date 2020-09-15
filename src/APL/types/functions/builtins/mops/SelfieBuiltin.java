package APL.types.functions.builtins.mops;

import APL.errors.DomainError;
import APL.types.Value;
import APL.types.functions.DerivedMop;
import APL.types.functions.builtins.MopBuiltin;
import APL.types.functions.builtins.fns2.*;

public class SelfieBuiltin extends MopBuiltin {
  public String repr() {
    return "˜";
  }
  
  
  
  public Value call(Value f, Value x, DerivedMop derv) {
    return f.call(x, x);
  }
  public Value call(Value f, Value w, Value x, DerivedMop derv) {
    return f.call(x, w);
  }
  
  public static RootBuiltin rb = new RootBuiltin();
  public Value callInv(Value f, Value x) {
    if (f instanceof PlusBuiltin) return DivBuiltin.DF.scalarX(x, 2);
    if (f instanceof MulBuiltin || f instanceof AndBuiltin) {
      rb.token = token; return rb.call(x);
    }
    if (f instanceof OrBuiltin) {
      rb.token = token; return NotBuiltin.on(rb.call(NotBuiltin.on(x, this)), this);
    }
    throw new DomainError(f+"˜: cannot invert", this);
  }
  
  public Value callInvX(Value f, Value w, Value x) {
    return f.callInvW(x, w);
  }
  
  public Value callInvW(Value f, Value w, Value x) {
    return f.callInvX(x, w);
  }
}