package APL.types;

import APL.*;
import APL.errors.SyntaxError;
import APL.types.mut.Settable;

public abstract class APLMap extends Primitive {
  
  public MapPointer get(Value k) {
    return new MapPointer(this, k);
  }
  
  public abstract Value getRaw(Value k);
  
  public Value getRaw(String k) {
    return getRaw(Main.toAPL(k));
  }
  public MapPointer get(String k) {
    return get(Main.toAPL(k));
  }
  
  abstract public void set(Value k, Value v);
  abstract public Arr allValues();
  abstract public Arr allKeys();
  abstract public Arr kvPair();
  abstract public int size();
  
  public static class MapPointer extends Settable {
    private final APLMap map;
    private final Value k;
    
    MapPointer(APLMap map, Value k) {
      this.map = map;
      this.k = k;
    }
  
    public Value get(Scope sc) {
      return map.getRaw(k);
    }
  
    public void set(Value x, boolean update, Scope sc, Callable blame) {
      boolean prev = map.getRaw(k) != Null.NULL;
      if (prev && !update) throw new SyntaxError("←: Cannot redefine map key '"+k+"'", blame, k);
      if (!prev && update) throw new SyntaxError("↩: Cannot update non-existing key '"+k+"'", blame, k);
      map.set(k, x);
    }
  
    public boolean seth(Value x, Scope sc) {
      throw new SyntaxError("map key cannot be a part of a header");
    }
  
    public String toString() {
      Value v = get(null);
      if (Main.debug) return v == null? "map@"+k : "ptr@"+k+":"+v;
      return v == null? "map@"+k : v.toString();
    }
  }
}