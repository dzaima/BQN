package APL.types.functions.builtins;

import APL.*;
import APL.types.*;

public class QuoteQuad extends Settable {
  public QuoteQuad() {
    super(null);
  }
  
  public void set(Value v, boolean update, Callable blame) {
    Main.print(v.toString());
  }
  
  @Override
  public Value get() {
    return Main.toAPL(Main.console.nextLine());
  }
  public Type type() {
    return Type.gettable;
  }
  
  public String toString() {
    return "⍞";
  }
}