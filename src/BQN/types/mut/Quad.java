package BQN.types.mut;

import BQN.*;
import BQN.errors.SyntaxError;
import BQN.types.*;
import BQN.types.arrs.ChrArr;

public class Quad extends Settable {
  
  public void set(Value x, boolean update, Scope sc, Callable blame) { // don't care about updating
    if (Main.debug) sc.sys.println("[log] "+x);
    else sc.sys.println(x);
  }
  
  public Value get(Scope sc) {
    return new ChrArr(sc.sys.input());
  }
  
  public boolean seth(Value x, Scope sc) {
    throw new SyntaxError("• cannot be a part of a header");
  }
  
  public String toString() {
    return "•";
  }
}