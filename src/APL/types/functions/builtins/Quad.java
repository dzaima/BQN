package APL.types.functions.builtins;

import APL.*;
import APL.types.*;

public class Quad extends Settable {
  public Quad() {
    super(null);
  }
  
  public void set(Value v, boolean update, Callable blame) { // don't care about updating
    Main.println((Main.debug? "[log] " : "")+v);
  }
  
  @Override
  public Value get() {
    return Main.toAPL(Main.console.nextLine());
  }
  public Type type() {
    return Type.gettable;
  }
  
  public String toString() {
    return "•";
  }
}