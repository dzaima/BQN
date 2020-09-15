package APL.types.callable.builtins.fns2;

import APL.errors.DomainError;
import APL.types.*;
import APL.types.arrs.IntArr;
import APL.types.callable.builtins.FnBuiltin;

public class TallyBuiltin extends FnBuiltin {
  
  public String repr() {
    return "≢";
  }
  
  public Value call(Value x) {
    return new IntArr(x.shape);
  }
  
  public Value call(Value w, Value x) {
    return w.eq(x)? Num.ZERO : Num.ONE;
  }
  
  public Value under(Value o, Value x) {
    Value v = o instanceof Fun? o.call(call(x)) : o;
    int[] sh = v.asIntVec();
    
    if (Arr.prod(sh) != x.ia) throw new DomainError("⌾≢ expected equal amount of output & output items", this);
    return x.ofShape(sh);
  }
}