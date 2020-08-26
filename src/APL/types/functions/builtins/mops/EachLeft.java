package APL.types.functions.builtins.mops;

import APL.types.*;
import APL.types.arrs.SingleItemArr;
import APL.types.functions.*;

public class EachLeft extends Mop {
  @Override public String repr() {
    return "ᐵ";
  }
  
  public Value call(Value f, Value w, Value x, DerivedMop derv) {
    Value[] n = new Value[w.ia];
    for (int i = 0; i < n.length; i++) {
      n[i] = f.call(w.get(i), x).squeeze();
    }
    return Arr.create(n, w.shape);
  }
  
  public Value underW(Value f, Value o, Value w, Value x, DerivedMop derv) {
    return EachBuiltin.underW(f, o, w, SingleItemArr.r0(x), this);
  }
}