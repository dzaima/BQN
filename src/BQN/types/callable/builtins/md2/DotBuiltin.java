package BQN.types.callable.builtins.md2;

import BQN.tools.FmtInfo;
import BQN.types.Value;
import BQN.types.callable.Md2Derv;
import BQN.types.callable.builtins.Md2Builtin;
import BQN.types.callable.builtins.md1.FoldBuiltin;

public class DotBuiltin extends Md2Builtin {
  public String ln(FmtInfo f) { return "."; }
  
  public Value call(Value f, Value g, Value w, Value x, Md2Derv derv) {
    return new FoldBuiltin().derive(f).call(g.call(w, x)); // TODO not lazy
  }
}