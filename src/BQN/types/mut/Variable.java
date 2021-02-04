package BQN.types.mut;

import BQN.Scope;
import BQN.errors.*;
import BQN.types.*;

public class Variable extends Settable {
  
  public final String name;
  
  public Variable(String name) {
    this.name = name;
  }
  
  public Value get(Scope sc) {
    Value got = sc.get(name);
    if (got == null) throw new ValueError("getting value of non-existing variable \""+name+"\"");
    return got;
  }
  
  public void set(Value x, boolean update, Scope sc, Callable blame) {
    if (update) {
      sc.update(name, x);
    } else {
      if (sc.varMap().containsKey(name)) throw new SyntaxError("Cannot redefine \""+name+"\"", blame);
      sc.set(name, x);
    }
  }
  
  public boolean seth(Value x, Scope sc) {
    sc.set(name, x);
    return true;
  }
  
  public String name(Scope sc) {
    return name;
  }
  protected boolean hasName() {
    return true;
  }
  
  public String toString() {
    return "var("+name+")";
  }
}