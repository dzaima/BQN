package APL.types.callable.builtins.md2;

import APL.tools.FmtInfo;
import APL.types.Value;
import APL.types.callable.Md2Derv;
import APL.types.callable.builtins.Md2Builtin;
import APL.types.callable.builtins.md1.FoldBuiltin;

public class DotBuiltin extends Md2Builtin {
  public String ln(FmtInfo f) { return "."; }
  
  public Value call(Value f, Value g, Value w, Value x, Md2Derv derv) {
    return new FoldBuiltin().derive(f).call(g.call(w, x)); // TODO not lazy
  }
}