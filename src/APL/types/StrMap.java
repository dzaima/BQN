package APL.types;

import APL.Main;
import APL.types.arrs.HArr;

import java.util.*;

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
  
  
  @Override
  public Value getRaw(Value k) {
    return getRaw(k.asString());
  }
  @Override
  public Value getRaw(String k) {
    Value v = vals.get(k);
    if (v == null) return Null.NULL;
    return v;
  }
  
  @Override
  public void set(Value k, Value v) {
    if (v == Null.NULL) vals.remove(k.asString());
    else vals.put(k.asString(), v);
  }
  
  public void setStr(String k, Value v) {
    if (v == Null.NULL) vals.remove(k);
    else vals.put(k, v);
  }
  
  @Override
  public Arr allValues() {
    ArrayList<Value> items = new ArrayList<>();
    for (Obj o : vals.values()) {
      if (o instanceof Value) items.add((Value) o);
    }
    return Arr.create(items);
  }
  
  @Override public Arr allKeys() {
    ArrayList<Value> items = new ArrayList<>();
    for (String o : vals.keySet()) {
      items.add(Main.toAPL(o));
    }
    return Arr.create(items);
  }
  
  @Override public Arr kvPair() {
    ArrayList<Value> ks = new ArrayList<>();
    ArrayList<Value> vs = new ArrayList<>();
    vals.forEach((k, v) -> {
      ks.add(Main.toAPL(k));
      vs.add(v);
    });
    return new HArr(new Value[]{
      HArr.create(ks),
       Arr.create(vs)});
  }
  
  @Override
  public int size() {
    return vals.size();
  }
  
  @Override
  public boolean equals(Obj o) {
    return o instanceof StrMap && vals.equals(((StrMap) o).vals);
  }
  
  @Override
  public String toString() {
    StringBuilder res = new StringBuilder("(");
    vals.forEach((key, value) -> {
      if (res.length() != 1) res.append(" ⋄ ");
      res.append(key).append(":").append(value);
    });
    return res + ")";
  }
}