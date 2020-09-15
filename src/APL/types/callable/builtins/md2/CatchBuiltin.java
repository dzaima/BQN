package APL.types.callable.builtins.md2;

import APL.types.Value;
import APL.types.callable.Md2Derv;
import APL.types.callable.builtins.Md2Builtin;

public class CatchBuiltin extends Md2Builtin {
  public String repr() {
    return "⎊";
  }
  
  public Value call(Value f, Value g, Value x, Md2Derv derv) {
    try {
      return f.call(x);
    } catch (Throwable e) {
      return g.call(x);
    }
  }
  public Value call(Value f, Value g, Value w, Value x, Md2Derv derv) {
    try {
      return f.call(w, x);
    } catch (Throwable e) {
      return g.call(w, x);
    }
  }
}