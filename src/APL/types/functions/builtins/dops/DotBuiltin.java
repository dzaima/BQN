package APL.types.functions.builtins.dops;

import APL.types.Value;
import APL.types.functions.*;
import APL.types.functions.builtins.mops.FoldBuiltin;

public class DotBuiltin extends Dop {
  @Override public String repr() {
    return ".";
  }
  
  public Value call(Value f, Value g, Value w, Value x, DerivedDop derv) {
    return new FoldBuiltin().derive(f).call(g.call(w, x)); // TODO not lazy
  }
}