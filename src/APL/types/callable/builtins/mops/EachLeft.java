package APL.types.callable.builtins.mops;

import APL.types.*;
import APL.types.arrs.SingleItemArr;
import APL.types.callable.DerivedMop;
import APL.types.callable.builtins.MopBuiltin;

public class EachLeft extends MopBuiltin {
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