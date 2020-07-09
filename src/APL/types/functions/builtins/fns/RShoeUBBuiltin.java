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
  
  public Value underW(Value o, Value a, Value w) {
    Value v = o instanceof Fun? ((Fun) o).call(call(a, w)) : o;
    Value[] vs = w.valuesCopy();
    for (int i = 0; i < a.ia; i++) {
      vs[Indexer.fromShape(w.shape, a.get(i).asIntVec())] = v.get(i);
    }
    return Arr.create(vs, w.shape);
  }
}