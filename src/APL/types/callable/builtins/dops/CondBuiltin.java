package APL.types.callable.builtins.dops;

import APL.errors.*;
import APL.types.*;
import APL.types.callable.Md2Derv;
import APL.types.callable.builtins.Md2Builtin;
import APL.types.callable.builtins.fns.LBoxUBBuiltin;

public class CondBuiltin extends Md2Builtin {
  public String repr() {
    return "◶";
  }
  
  public Value call(Value f, Value g, Value x, Md2Derv derv) {
    return get(f.call(x), g).call(x);
  }
  public Value call(Value f, Value g, Value w, Value x, Md2Derv derv) {
    return get(f.call(w, x), g).call(w, x);
  }
  
  private Value get(Value F, Value g) {
    if (F instanceof Num) {
      int f = F.asInt();
      if (g.rank != 1) throw new RankError("◶: Expected 𝕘 to be a vector, had rank "+g.rank, this, g);
      if (f>=g.ia || f<0) throw new LengthError("◶: 𝔽 out of bounds of 𝕘 (𝔽 = "+f+")", this, F);
      return g.get(f);
    }
    return LBoxUBBuiltin.on(F, g, this);
  }
}