package APL.types.functions.builtins.fns2;

import APL.Main;
import APL.errors.*;
import APL.types.*;
import APL.types.functions.Builtin;

public class LBoxBuiltin extends Builtin {
  public String repr() {
    return "⊏";
  }
  
  
  
  public Value call(Value w) {
    if (w.rank==0) throw new RankError("⊏: 𝕨 was of rank 0", this, w);
    int ia = 1;
    int[] nsh = new int[w.rank-1];
    System.arraycopy(w.shape, 1, nsh, 0, nsh.length);
    for (int i = 1; i < w.shape.length; i++) ia *= w.shape[i];
    Value[] res = new Value[ia];
    for (int i = 0; i < ia; i++) { // valuecopy
      res[i] = w.get(i);
    }
    return Arr.create(res, nsh);
  }
  
  public Value call(Value a, Value w) {
    if (!(a instanceof Num)) throw new NYIError("⊏ with non-integer 𝕨", this, a);
    return on(a.asInt(), w, this);
  }
  public static Value on(int a, Value w, Callable blame) {
    if (w.rank == 0) throw new RankError(blame+": scalar 𝕩 isn't allowed", blame, w);
    int ca = w.shape[0];
    int len = w.ia/ca;
    int start = len*a;
    if (start<0 || start>=w.ia) throw new LengthError(blame+": indexing out of bounds (accessing cell "+a+" in a shape "+ Main.formatAPL(w.shape)+" array)", blame);
    int[] sh = new int[w.rank-1];
    System.arraycopy(w.shape, 1, sh, 0, sh.length);
    Value[] res = new Value[len];
    for (int i = 0; i < len; i++) { // valuecopy
      res[i] = w.get(i+start);
    }
    return Arr.create(res, sh);
  }
}
