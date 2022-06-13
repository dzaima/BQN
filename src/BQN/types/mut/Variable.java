package BQN.types.mut;

import BQN.Scope;
import BQN.errors.*;
import BQN.types.*;

public class Variable extends Settable {
  
  public final String nameF;
  
  public Variable(String nameF) {
    this.nameF = nameF;
  }
  
  public Value get(Scope sc) {
    Value got = sc.get(nameF);
    if (got == null) throw new ValueError("getting value of non-existing variable \""+nameF+"\"");
    return got;
  }
  
  public void set(Value x, boolean update, Scope sc, Callable blame) {
    if (update) {
      sc.update(nameF, x);
    } else {
      if (sc.varMap().containsKey(nameF)) throw new SyntaxError("Cannot redefine \""+nameF+"\"", blame);
      sc.set(nameF, x);
    }
  }
  
  public boolean seth(Value x, Scope sc) {
    sc.set(nameF, x);
    return true;
  }
  
  public String name(Scope sc) {
    return nameF;
  }
  public boolean hasName() {
    return true;
  }
  
  public String toString() {
    return "var("+nameF+")";
  }
}