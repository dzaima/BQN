package APL.types.functions.builtins.dops;

import APL.errors.*;
import APL.types.*;
import APL.types.functions.*;
import APL.types.functions.builtins.fns2.LBoxUBBuiltin;

public class CondBuiltin extends Dop {
  
  public Value call(Value f, Value g, Value x, DerivedDop derv) {
    return get(f.asFun().call(x), g).call(x);
  }
  public Value call(Value f, Value g, Value w, Value x, DerivedDop derv) {
    return get(f.asFun().call(w, x), g).call(w, x);
  }
  
  private Fun get(Value F, Value g) {
    if (F instanceof Num) {
      int f = F.asInt();
      if (g.rank != 1) throw new RankError("◶: Expected 𝕘 to be a vector, had rank "+g.rank, this, g);
      if (f>=g.ia || f<0) throw new LengthError("◶: 𝔽 out of bounds of 𝕘 (𝔽 = "+f+")", this, F);
      return g.get(f).asFun();
    }
    return LBoxUBBuiltin.on(F, g, this).asFun();
  }
  
  public String repr() {
    return "◶";
  }
}
