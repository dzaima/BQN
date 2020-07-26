package APL.types.functions.builtins;

import APL.*;
import APL.types.*;

public class Quad extends Settable {
  
  public void set(Value v, boolean update, Scope sc, Callable blame) { // don't care about updating
    sc.sys.println((Main.debug? "[log] " : "")+v);
  }
  public Value get(Scope sc) {
    return Main.toAPL(sc.sys.input());
  }
  
  public String toString() {
    return "•";
  }
}