package APL.types.functions.builtins.mops;

import APL.types.*;
import APL.types.arrs.Rank0Arr;
import APL.types.functions.*;

public class EachRight extends Mop {
  @Override public String repr() {
    return "ᑈ";
  }
  
  public Value call(Value f, Value a, Value w, DerivedMop derv) {
    Fun ff = f.asFun();
    Value[] n = new Value[w.ia];
    for (int i = 0; i < n.length; i++) {
      n[i] = ff.call(a, w.get(i)).squeeze();
    }
    return Arr.create(n, w.shape);
  }
  
  public Value underW(Value aa, Value o, Value a, Value w, DerivedMop derv) {
    return EachBuiltin.underW(aa.asFun(), o, new Rank0Arr(a), w, this);
  }
}