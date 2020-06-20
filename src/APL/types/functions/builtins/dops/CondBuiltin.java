package APL.types.functions.builtins.dops;

import APL.errors.*;
import APL.types.*;
import APL.types.functions.*;

public class CondBuiltin extends Dop {
  
  public Value call(Value aa, Value ww, Value w, DerivedDop derv) {
    return get(aa.asFun().call(w), ww).call(w);
  }
  public Value call(Value aa, Value ww, Value a, Value w, DerivedDop derv) {
    return get(aa.asFun().call(a, w), ww).call(a, w);
  }
  
  private Fun get(Value F, Value g) {
    int f = F.asInt();
    if (g.rank != 1) throw new RankError("◶: Expected 𝕘 to be a vector, had rank "+g.rank, this, g);
    if (f>=g.rank || f<0) throw new LengthError("◶: 𝔽 out of bounds of 𝕘 (𝔽 = "+f+")", this, F);
    return g.get(f).asFun();
  }
  
  public String repr() {
    return "◶";
  }
}
