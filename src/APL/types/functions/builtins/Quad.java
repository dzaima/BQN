package APL.types.functions.builtins;

import APL.*;
import APL.types.*;

public class Quad extends Settable {
  private final Scope sc;
  public Quad(Scope sc) {
    super(null);
    this.sc = sc;
  }
  
  public void set(Value v, Callable blame) {
    Main.println((Main.debug? "[log] " : "")+v);
  }
  
  @Override
  public Value get() {
    return Main.exec(Main.console.nextLine(), sc);
  }
  public Type type() {
    return Type.gettable;
  }
  
  public String toString() {
    return "•";
  }
}