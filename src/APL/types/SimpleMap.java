package APL.types;

import APL.errors.SyntaxError;

public abstract class SimpleMap extends APLMap {
  public Value getRaw(Value k) {
    return getv(k.asString());
  }
  
  public abstract Value getv(String s);
  public abstract void setv(String s, Value v);
  
  public void set(Value k, Value v) {
    setv(k.asString(), v);
  }
  
  public Arr allValues() {
    throw new SyntaxError("getting list of values of "+this);
  }
  
  public Arr allKeys() {
    throw new SyntaxError("getting list of keys of "+this);
  }
  
  public Arr kvPair() {
    throw new SyntaxError("getting entries of "+this);
  }
  
  public int size() {
    throw new SyntaxError("getting size of "+this);
  }
}