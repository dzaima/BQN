package APL.types.callable.builtins.fns;

import APL.errors.RankError;
import APL.tools.*;
import APL.types.Value;
import APL.types.callable.builtins.FnBuiltin;
import APL.types.callable.builtins.md1.CellBuiltin;

public class ShBBuiltin extends FnBuiltin {
  public String ln(FmtInfo f) { return "»"; }
  
  public Value call(Value w, Value x) {
    if (x.scalar()) throw new RankError("»: 𝕩 cannot be scalar", this);
    if (w.r() > x.r()) throw new RankError("»: rank of 𝕨 cannot exceed =𝕩", this);
    JoinBuiltin.check(w, x, this);
    MutVal res = new MutVal(x.shape, x);
    int mid = Math.min(w.ia, x.ia);
    res.copy(w, 0, 0, mid);
    res.copy(x, 0, mid, x.ia-mid);
    return res.get();
  }
  
  public Value call(Value x) {
    if (x.scalar()) throw new RankError("»: argument cannot be scalar", this);
    if (x.ia==0) return x;
    MutVal res = new MutVal(x.shape, x);
    int csz = CellBuiltin.csz(x);
    res.fill(x.fItem(), 0, csz);
    res.copy(x, 0, csz, x.ia-csz);
    return res.get();
  }
}
