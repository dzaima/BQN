package APL.types;

import APL.*;
import APL.errors.*;

public class Variable extends Settable {
  
  private final Scope sc;
  public final String name;
  
  public Variable(Scope sc, String name) {
    super(sc.get(name));
    this.sc = sc;
    this.name = name;
  }
  
  public Value get() {
    if (v == null) throw new ValueError("getting value of non-existing variable \""+name+"\"", this);
    return v;
  }
  
  public void set(Value v, boolean update, Callable blame) {
    if (update) {
      sc.update(name, v);
    } else {
      if (sc.varMap().containsKey(name)) throw new SyntaxError("←: cannot redefine \""+name+"\"", blame, this);
      sc.set(name, v);
    }
  }
  
  @Override
  public String toString() {
    if (Main.debug) return v == null? "var:"+name : "var:"+v;
    return v == null? "var:"+name : v.toString();
  }
}