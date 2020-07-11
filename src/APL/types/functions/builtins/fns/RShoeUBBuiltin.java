package APL.types.functions.builtins.fns;

import APL.Indexer;
import APL.types.*;
import APL.types.functions.Builtin;
import APL.types.functions.builtins.fns2.LBoxUBBuiltin;

public class RShoeUBBuiltin extends Builtin {
  @Override public String repr() {
    return "⊇";
  }
  
  public Value call(Value w, Value x) {
    return LBoxUBBuiltin.on(w, x, this);
  }
  
  public Value underW(Value o, Value w, Value x) {
    Value v = o instanceof Fun? ((Fun) o).call(call(w, x)) : o;
    Value[] vs = x.valuesClone();
    for (int i = 0; i < w.ia; i++) {
      vs[Indexer.fromShape(x.shape, w.get(i).asIntVec())] = v.get(i);
    }
    return Arr.create(vs, x.shape);
  }
}