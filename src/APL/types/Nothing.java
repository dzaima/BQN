package APL.types;

import APL.errors.SyntaxError;

public class Nothing extends Primitive {
  public static final Nothing inst = new Nothing();
  
  
  SyntaxError gotten() {
    return new SyntaxError("Using ·", this);
  }
  
  
  
  public String asString() {
    throw gotten();
  }
  
  public Value safePrototype() {
    throw gotten();
  }
  
  public Value ofShape(int[] sh) {
    throw gotten();
  }
  
  public Fun asFun() {
    throw new SyntaxError("didn't expect · here", this);
  }
  
  public String toString() {
    return "·";
  }
}