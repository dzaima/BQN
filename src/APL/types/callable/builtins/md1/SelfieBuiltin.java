package APL.types.callable.builtins.md1;

import APL.errors.DomainError;
import APL.types.Value;
import APL.types.callable.DerivedMop;
import APL.types.callable.builtins.Md1Builtin;
import APL.types.callable.builtins.fns.*;

public class SelfieBuiltin extends Md1Builtin {
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