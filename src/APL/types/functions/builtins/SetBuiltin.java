package APL.types.functions.builtins;

import APL.errors.SyntaxError;
import APL.types.*;

public class SetBuiltin extends AbstractSet {
  public final static SetBuiltin inst = new SetBuiltin();
  
  public String toString() {
    return "←";
  }
  
  
  
  
  public Value call(Obj a, Value w, boolean update) {
    set(a, w, update);
    return w;
  }
  
  public static void set(Obj k, Value v, boolean update) {
    if (!(k instanceof Settable)) throw new SyntaxError("Cannot set non-settable "+k);
    ((Settable) k).set(v, update, null);
  }
}