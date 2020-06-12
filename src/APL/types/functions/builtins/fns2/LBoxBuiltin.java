package APL.types.functions.builtins.fns2;

import APL.errors.RankError;
import APL.types.*;
import APL.types.functions.Builtin;

public class LBoxBuiltin extends Builtin {
  public String repr() {
    return "⊏";
  }
  
  
  
  public Value call(Value w) { // TODO getting cells
    if (w.rank==0) throw new RankError("⊏: \uD835\uDD68 was of rank 0", this, w);
    int ia = 1;
    int[] nsh = new int[w.rank-1];
    System.arraycopy(w.shape, 1, nsh, 0, nsh.length);
    for (int i = 1; i < w.shape.length; i++) ia *= w.shape[i];
    Value[] res = new Value[ia];
    for (int i = 0; i < ia; i++) {
      res[i] = w.get(i);
    }
    return Arr.create(res, nsh);
  }
  
  
  
}
