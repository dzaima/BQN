package APL.types.functions.builtins.dops;

import APL.errors.*;
import APL.types.*;
import APL.types.functions.*;
import APL.types.functions.builtins.fns2.LBoxUBBuiltin;

public class CondBuiltin extends Dop {
  
  public Value call(Value aa, Value ww, Value w, DerivedDop derv) {
    return get(aa.asFun().call(w), ww).call(w);
  }
  public Value call(Value aa, Value ww, Value a, Value w, DerivedDop derv) {
    return get(aa.asFun().call(a, w), ww).call(a, w);
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
