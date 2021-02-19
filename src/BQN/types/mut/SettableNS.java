package BQN.types.mut;

import BQN.Scope;
import BQN.errors.*;
import BQN.types.*;

public class SettableNS extends Settable {
  Value[] keys;
  Settable[] vals;
  public SettableNS(Settable[] vals, Value keys) {
    this.keys = keys.values();
    this.vals = vals;
  }
  
  public Value get(Scope sc) {
    throw new SyntaxError("Modified assignment on namespace not allowed");
  }
  
  public void set(Value x, boolean update, Scope sc, Callable blame) {
    if (!(x instanceof APLMap)) throw new DomainError("Expected to assign to a namespace", blame);
    APLMap ns = (APLMap) x;
    for (int i = 0; i < keys.length; i++) {
      vals[i].set(ns.getChk(keys[i].asString()), update, sc, blame);
    }
  }
  
  public boolean seth(Value x, Scope sc) {
    if (!(x instanceof APLMap)) throw new DomainError("Expected to assign to a namespace");
    APLMap ns = (APLMap) x;
    for (int i = 0; i < keys.length; i++) {
      Value key = keys[i];
      Value val = ns.get(key);
      if (val == null) return false;
      if (!vals[i].seth(val, sc)) return false;
    }
    return true;
  }
}
