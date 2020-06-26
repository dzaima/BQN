package APL.types.functions.builtins.fns;

import APL.*;
import APL.types.*;
import APL.types.functions.Builtin;
import APL.types.functions.builtins.fns2.LBoxUBBuiltin;

public class RShoeUBBuiltin extends Builtin {
  @Override public String repr() {
    return "⊇";
  }
  
  public RShoeUBBuiltin(Scope sc) {
    super(sc);
  }
  
  public Value call(Value a, Value w) {
    return LBoxUBBuiltin.on(a, w, this);
  }
  
  public Value underW(Value o, Value a, Value w) {
    Value v = o instanceof Fun? ((Fun) o).call(call(a, w)) : (Value) o;
    Value[] vs = w.valuesCopy();
    for (int i = 0; i < a.ia; i++) {
      vs[Indexer.fromShape(w.shape, a.get(i).asIntVec())] = v.get(i);
    }
    return Arr.create(vs, w.shape);
  }
}