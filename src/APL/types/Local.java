package APL.types;

import APL.*;
import APL.errors.*;

public class Local extends Settable {
  
  private final Scope sc;
  public final int index;
  
  public Local(Scope sc, int depth, int index) {
    this(sc.owner(depth), index);
  }
  public Local(Scope sc, int index) {
    super(sc.vars[index]);
    this.sc = sc;
    this.index = index;
  }
  
  public Value get() {
    if (v == null) throw new ValueError("Getting value of non-existing variable \""+this+"\"", this);
    return v;
  }
  
  public void set(Value v, boolean update, Callable blame) {
    if (update ^ v!=null) {
      if (update) throw new ValueError("no variable \""+this+"\" to update", blame);
      else        throw new ValueError("←: cannot redefine \""+this+"\"", blame);
    }
    sc.vars[index] = v;
  }
  
  public String toString() {
    return sc.varNames[index];
  }
}