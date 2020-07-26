package APL.types;

import APL.*;
import APL.errors.*;

public class Variable extends Settable {
  
  public final String name;
  
  public Variable(String name) {
    this.name = name;
  }
  
  public Value get(Scope sc) {
    Value got = sc.get(name);
    if (got == null) throw new ValueError("getting value of non-existing variable \""+name+"\"", this);
    return got;
  }
  
  public void set(Value v, boolean update, Scope sc, Callable blame) {
    if (update) {
      sc.update(name, v);
    } else {
      if (sc.varMap().containsKey(name)) throw new SyntaxError("←: cannot redefine \""+name+"\"", blame, this);
      sc.set(name, v);
    }
  }
  
  public String toString() {
    return "var("+name+")";
  }
}