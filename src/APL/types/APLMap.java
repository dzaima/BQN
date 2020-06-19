package APL.types;

import APL.Main;
import APL.errors.SyntaxError;
import APL.types.arrs.SingleItemArr;

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
      super(map.getRaw(k));
      this.map = map;
      this.k = k;
    }
    
    public void set(Value v, boolean update, Callable blame) {
      boolean prev = map.getRaw(k) != Null.NULL;
      if (prev && !update) throw new SyntaxError("←: Cannot redefine map key '"+k+"'", blame, k);
      if (!prev && update) throw new SyntaxError("↩: Cannot update non-existing key '"+k+"'", blame, k);
      map.set(k, v);
    }
  
    public String toString() {
      if (Main.debug) return v == null? "map@"+k : "ptr@"+k+":"+v;
      return v == null? "map@"+k : v.toString();
    }
  }
  
  @Override
  public Value ofShape(int[] sh) {
    if (sh.length == 0 && Main.enclosePrimitives) return this;
    assert ia == Arr.prod(sh);
    return new SingleItemArr(this, sh);
  }
}