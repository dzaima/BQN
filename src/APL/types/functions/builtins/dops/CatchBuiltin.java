package APL.types.functions.builtins.dops;

import APL.types.Value;
import APL.types.functions.*;

public class CatchBuiltin extends Dop {
  public String repr() {
    return "⎊";
  }
  
  public Value call(Value f, Value g, Value x, DerivedDop derv) {
    try {
      return f.asFun().call(x);
    } catch (Throwable e) {
      return g.asFun().call(x);
    }
  }
  public Value call(Value f, Value g, Value w, Value x, DerivedDop derv) {
    try {
      return f.asFun().call(w, x);
    } catch (Throwable e) {
      return g.asFun().call(w, x);
    }
  }
}