package APL.types.mut;

import APL.Scope;
import APL.errors.ValueError;
import APL.types.*;

public class Local extends Settable {
  
  public final int depth, index;
  
  public Local(int depth, int index) {
    this.depth = depth;
    this.index = index;
  }
  
  public Value get(Scope sc) {
    Value got = sc.getL(depth, index);
    if (got == null) throw new ValueError("Getting value of non-existing variable \""+name(sc)+"\"", this);
    return got;
  }
  
  public void set(Value x, boolean update, Scope sc, Callable blame) {
    sc = sc.owner(depth);
    if (update ^ sc.vars[index]!=null) {
      if (update) throw new ValueError("no variable \""+name(sc)+"\" to update", blame);
      else {
        if (sc.parent!=null) throw redefine(name(sc), blame); // allow top-level redeclarations 
      }
    }
    sc.vars[index] = x;
  }
  
  public boolean seth(Value x, Scope sc) {
    sc.owner(depth).vars[index] = x;
    return true;
  }
  
  public static ValueError redefine(String name, Tokenable blame) {
    return new ValueError("Cannot redefine \""+name+"\"", blame);
  }
  
  @Override public String name(Scope sc) {
    return sc.owner(depth).varNames[index];
  }
  @Override protected boolean hasName() {
    return true;
  }
  
  public String toString() {
    return "loc("+depth+","+index+")";
  }
}