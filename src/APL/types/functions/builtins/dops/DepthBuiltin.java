package APL.types.functions.builtins.dops;

import APL.errors.DomainError;
import APL.types.*;
import APL.types.functions.*;
import APL.types.functions.builtins.fns2.MatchBuiltin;

public class DepthBuiltin extends Dop {
  public String repr() {
    return "⚇";
  }
  
  public Value call(Obj aa, Obj ww, Value a, Value w, DerivedDop derv) {
    Fun aaf = isFn(aa, '⍶'); Fun wwf = isFn(ww, '⍹');
    return aaf.call(wwf.call(a), wwf.call(w));
  }
  
  public static Value on(Fun caller, Fun f, int d, Value w) {
    int ld = MatchBuiltin.lazy(w);
    if (ld==d || ld <= -d) {
      int fd = MatchBuiltin.full(w);
      if (d>0 && d!=fd) throw new DomainError(caller+": can't match a depth " + fd + " array", caller, w);
      if (d <= fd) {
        return f.call(w);
      }
    }
    if (d>0 && ld < d) throw new DomainError(caller+": can't match a depth "+ MatchBuiltin.full(w)+" array", caller, w);
    Value[] res = new Value[w.ia];
    for (int i = 0; i < res.length; i++) {
      res[i] = on(caller, f, d, w.get(i));
    }
    return Arr.createL(res, w.shape);
  }
}
