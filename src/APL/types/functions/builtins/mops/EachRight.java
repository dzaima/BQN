package APL.types.functions.builtins.mops;

import APL.types.*;
import APL.types.arrs.SingleItemArr;
import APL.types.functions.*;

public class EachRight extends Mop {
  @Override public String repr() {
    return "ᑈ";
  }
  
  public Value call(Value f, Value w, Value x, DerivedMop derv) {
    Value[] n = new Value[x.ia];
    for (int i = 0; i < n.length; i++) {
      n[i] = f.call(w, x.get(i)).squeeze();
    }
    return Arr.create(n, x.shape);
  }
  
  public Value underW(Value f, Value o, Value w, Value x, DerivedMop derv) {
    return EachBuiltin.underW(f, o, SingleItemArr.r0(w), x, this);
  }
}