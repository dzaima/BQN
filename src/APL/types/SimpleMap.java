package APL.types;

import APL.errors.SyntaxError;

public abstract class SimpleMap extends APLMap {
  @Override public Value getRaw(Value k) {
    return getv(k.asString());
  }
  
  public abstract Value getv(String s);
  public abstract void setv(String s, Obj v);
  
  @Override public void set(Value k, Value v) {
    setv(k.asString(), v);
  }
  
  @Override public Arr allValues() {
    throw new SyntaxError("getting list of values of "+this);
  }
  
  @Override public Arr allKeys() {
    throw new SyntaxError("getting list of keys of "+this);
  }
  
  @Override public Arr kvPair() {
    throw new SyntaxError("getting entries of "+this);
  }
  
  @Override public int size() {
    throw new SyntaxError("getting size of "+this);
  }
}