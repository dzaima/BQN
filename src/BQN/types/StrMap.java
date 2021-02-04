package BQN.types;

import BQN.tools.FmtInfo;
import BQN.types.arrs.ChrArr;

import java.util.HashMap;

public class StrMap extends APLMap {
  public final HashMap<String, Value> vals;
  // public final Scope sc;
  
  public StrMap(HashMap<String, Value> vals) {
    this.vals = vals;
  }
  
  public StrMap() {
    this.vals = new HashMap<>();
    // this.sc = null;
  }
  
  // public StrMap(Scope sc, HashMap<String, Obj> vals) {
  //   this.sc = sc;
  //   this.vals = vals;
  // }
  
  
  public Value get(Value k) {
    return get(k.asString());
  }
  public Value get(String k) {
    return vals.get(k);
  }
  
  public void set(Value k, Value v) {
    if (v == null) vals.remove(k.asString());
    else vals.put(k.asString(), v);
  }
  
  public void setStr(String k, Value v) {
    if (v == null) vals.remove(k);
    else vals.put(k, v);
  }
  
  public Value[][] kvPair() {
    Value[] ks = new Value[vals.size()];
    Value[] vs = new Value[vals.size()];
    final int[] i = {0};
    vals.forEach((k, v) -> {
      ks[i[0]++] = new ChrArr(k);
      vs[i[0]++] = v;
    });
    return new Value[][]{ks, vs};
  }
  
  public int size() {
    return vals.size();
  }
  
  public boolean eq(Value o) {
    return o instanceof StrMap && vals.equals(((StrMap) o).vals);
  }
  public int hashCode() {
    final int[] res = {0};
    vals.forEach((k, v) -> res[0]+= k.hashCode()*387678968 + v.hashCode());
    return res[0];
  }
  
  public Value pretty(FmtInfo f) { return new ChrArr("[StrMap]"); }
  public String ln(FmtInfo f) { return "[StrMap]"; }
}