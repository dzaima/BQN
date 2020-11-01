package APL.types;

import APL.Scope;
import APL.errors.ValueError;
import APL.tools.*;
import APL.types.arrs.ChrArr;

import java.util.HashMap;

public class Namespace extends APLMap {
  private final Scope sc;
  private final HashMap<String, Integer> exports;
  public Namespace(Scope sc, HashMap<String, Integer> exports) {
    this.sc = sc;
    this.exports = exports;
  }
  
  public Value get(Value k) {
    return get(k.asString());
  }
  
  public Value get(String k) {
    Integer kn = exports.get(k);
    if (kn==null) return null;
    return sc.vars[kn];
  }
  
  public void set(Value ko, Value v) {
    String k = ko.asString();
    Integer kn = exports.get(k);
    if (kn==null) throw new ValueError("Setting non-defined namespace field "+ko, ko);
    sc.vars[kn] = v;
  }
  
  public Value[][] kvPair() {
    Value[] ks = new Value[exports.size()];
    Value[] vs = new Value[exports.size()];
    final int[] i = {0};
    exports.forEach((k, v) -> {
      ks[i[0]++] = new ChrArr(k);
      vs[i[0]++] = sc.vars[v];
    });
    return new Value[][]{ks, vs};
  }
  
  public int size() {
    return exports.size();
  }
  
  public boolean eq(Value o) {
    return o instanceof Namespace && ((Namespace) o).sc == sc;
  }
  
  public int hashCode() {
    return sc.hashCode();
  }
  
  public String ln(FmtInfo f) {
    if (f.v.contains(this)) return "{...}";
    f.v.add(this);
    StringBuilder res = new StringBuilder("{");
    exports.forEach((key, value) -> {
      if (res.length() != 1) res.append(" ⋄ ");
      Value v = sc.vars[value].pretty(f);
      res.append(key).append("⇐").append(v);
    });
    f.v.remove(this);
    return res + "}";
  }
  public Value pretty(FmtInfo f) {
    return Format.str(ln(f));
  }
}
