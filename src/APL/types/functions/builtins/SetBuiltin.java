package APL.types.functions.builtins;

import APL.errors.SyntaxError;
import APL.types.*;

public class SetBuiltin extends AbstractSet {
  public final static SetBuiltin inst = new SetBuiltin();
  
  public String toString() {
    return "←";
  }
  
  
  
  
  public Value call(Obj w, Value x, boolean update) {
    set(w, x, update);
    return x;
  }
  
  public static void set(Obj k, Value v, boolean update) {
    if (!(k instanceof Settable)) throw new SyntaxError("Cannot set non-settable "+k);
    ((Settable) k).set(v, update, null);
  }
}