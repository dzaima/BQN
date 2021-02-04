package BQN.types.callable.builtins.md2;

import BQN.errors.*;
import BQN.tools.FmtInfo;
import BQN.types.*;
import BQN.types.callable.Md2Derv;
import BQN.types.callable.builtins.Md2Builtin;
import BQN.types.callable.builtins.fns.MatchBuiltin;

public class DepthBuiltin extends Md2Builtin {
  public String ln(FmtInfo f) { return "⚇"; }
  
  public Value call(Value f, Value g, Value x, Md2Derv derv) {
    int d = g.asInt();
    if (d < 0) throw new NYIError("negative 𝕘 for ⚇", this);
    return on(f, d, x, derv);
  }
  
  public static Value on(Value f, int d, Value w, Fun blame) {
    int ld = MatchBuiltin.lazy(w);
    if (ld==d || ld <= -d) {
      int fd = MatchBuiltin.full(w);
      if (d>0 && d!=fd) throw new DomainError(blame+": can't match a depth "+fd+" array", blame);
      if (d <= fd) {
        return f.call(w);
      }
    }
    if (d>0 && ld < d) throw new DomainError(blame+": can't match a depth "+MatchBuiltin.full(w)+" array", blame);
    Value[] res = new Value[w.ia];
    for (int i = 0; i < res.length; i++) {
      res[i] = on(f, d, w.get(i), blame);
    }
    return Arr.create(res, w.shape);
  }
}