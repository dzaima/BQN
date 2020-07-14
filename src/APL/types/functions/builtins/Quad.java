package APL.types.functions.builtins;

import APL.*;
import APL.types.*;

public class Quad extends Settable {
  private final Scope sc;
  
  public Quad(Scope sc) {
    super(null);
    this.sc = sc;
  }
  
  public void set(Value v, boolean update, Callable blame) { // don't care about updating
    sc.sys.println((Main.debug? "[log] " : "")+v);
  }
  public Value get() {
    return Main.toAPL(sc.sys.input());
  }
  
  public String toString() {
    return "•";
  }
}