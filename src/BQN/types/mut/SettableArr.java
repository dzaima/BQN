package BQN.types.mut;

import BQN.Scope;
import BQN.errors.LengthError;
import BQN.types.*;

public class SettableArr extends Settable {
  public final Settable[] arr;
  public final int ia;
  public SettableArr(Settable[] arr) {
    ia = arr.length;
    this.arr = arr;
  }
  
  public Value get(Scope sc) {
    Value[] res = new Value[arr.length];
    for (int i = 0; i < ia; i++) res[i] = arr[i].get(sc);
    return Arr.create(res);
  }
  
  
  public String toString() {
    return "vararr";
  }
  
  
  public void set(Value x, boolean update, Scope sc, Callable blame) {
    if (x.r() != 1) {
      if (x instanceof BQNObj) {
        BQNObj ns = (BQNObj) x;
        for (Settable c : arr) c.set(ns.getChk(c.name(sc)), update, sc, blame);
        return;
      }
      throw new LengthError((update?'↩':'←')+": scatter rank ≠1");
    }
    if (x.ia != ia) throw new LengthError((update?'↩':'←')+": scatter argument lengths not equal");
    
    for (int i = 0; i < ia; i++) arr[i].set(x.get(i), update, sc, null);
  }
  
  public boolean seth(Value x, Scope sc) {
    if (x.r() != 1) {
      if (x instanceof BQNObj) {
        BQNObj ns = (BQNObj) x;
        for (Settable c : arr) {
          Value val = ns.get(c.name(sc));
          if (val==null) return false;
          if (!c.hasName() || !c.seth(val, sc)) return false;
        }
        return true;
      }
      return false;
    }
    if (x.ia != ia) return false;
    
    for (int i = 0; i < ia; i++) if (!arr[i].seth(x.get(i), sc)) return false;
    return true;
  }
}