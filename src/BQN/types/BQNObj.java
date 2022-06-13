package BQN.types;

import BQN.*;
import BQN.errors.*;
import BQN.tools.FmtInfo;
import BQN.types.arrs.ChrArr;
import BQN.types.mut.Settable;

public abstract class BQNObj extends Primitive {
  
  public MapPointer getMut(Value k) {
    return new MapPointer(this, k);
  }
  public MapPointer getMut(String k) {
    return getMut(new ChrArr(k));
  }
  
  public abstract Value get(Value k); // returns null if doesn't have
  public Value get(String k) {
    return get(new ChrArr(k));
  }
  public Value getChk(String k) {
    Value v = get(k);
    if (v == null) throw new ValueError("Reading non-defined key "+k);
    return v;
  }
  
  abstract public void set(Value k, Value v);
  abstract public Value[][] kvPair();
  abstract public int size();
  public int Count { get { return size(); } }
  
  public static class MapPointer extends Settable {
    private final BQNObj map;
    private final Value k;
    
    MapPointer(BQNObj map, Value k) {
      this.map = map;
      this.k = k;
    }
    
    public Value get(Scope sc) {
      return map.get(k);
    }
    
    public void set(Value x, boolean update, Scope sc, Callable blame) {
      boolean prev = map.get(k) != null;
      if (prev && !update) throw new SyntaxError("←: Cannot redefine map key '"+k+"'", blame);
      if (!prev && update) throw new SyntaxError("↩: Cannot update non-existing key '"+k+"'", blame);
      map.set(k, x);
    }
    
    public boolean seth(Value x, Scope sc) {
      throw new SyntaxError("map key cannot be a part of a header");
    }
    
    public String toString() {
      Value v = get(null);
      if (Main.debug) return v == null? "map@"+k : "ptr@"+k+":"+v;
      return v == null? "map@"+k : v.ln(FmtInfo.def);
    }
  }
}