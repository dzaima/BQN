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
  
  public Value[][] kvPair() {
    throw new SyntaxError("getting entries of "+this);
  }
  
  public int size() {
    throw new SyntaxError("getting size of "+this);
  }
  
  public boolean eq(Value o) {
    return this==o;
  }
  
  public int hashCode() {
    return 0;
  }
}