package APL.types;

import APL.*;
import APL.errors.*;

public class Local extends Settable {
  
  public final int depth, index;
  
  public Local(int depth, int index) {
    this.depth = depth;
    this.index = index;
  }
  
  public Value get(Scope sc) {
    Value got = sc.getL(depth, index);
    if (got == null) throw new ValueError("Getting value of non-existing variable \""+this+"\"", this);
    return got;
  }
  
  public void set(Value v, boolean update, Scope sc, Callable blame) {
    sc = sc.owner(depth);
    if (update ^ v!=null) {
      if (update) throw new ValueError("no variable \""+sc.varNames[index]+"\" to update", blame);
      else        throw new ValueError("←: cannot redefine \""+this+"\"", blame);
    }
    sc.vars[index] = v;
  }
  
  public String toString() {
    return "loc("+depth+","+index+")";
  }
}