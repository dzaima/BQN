package APL.types;

import APL.errors.SyntaxError;

public class Nothing extends Primitive {
  public static final Nothing inst = new Nothing();
  
  
  SyntaxError used() {
    return new SyntaxError("didn't expect ·", this);
  }
  
  
  
  public String asString() { throw used(); }
  public Value safePrototype() { throw used(); }
  public Value ofShape(int[] sh) { throw used(); }
  
  
  public String toString() { return "·"; }
  
  
  public int hashCode() { return 0; }
  public boolean eq(Value o) {
    return o instanceof Nothing;
  }
}