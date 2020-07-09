package APL.types.functions.builtins.dops;

import APL.types.*;
import APL.types.functions.*;
import APL.types.functions.builtins.mops.ReduceBuiltin;

public class DotBuiltin extends Dop {
  @Override public String repr() {
    return ".";
  }
  
  public Value call(Value f, Value g, Value w, Value x, DerivedDop derv) {
    return new ReduceBuiltin().derive(f).call(g.asFun().call(w, x)); // TODO not lazy
  }
}