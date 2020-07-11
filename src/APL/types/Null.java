package APL.types;

import APL.Type;
import APL.types.arrs.SingleItemArr;

public class Null extends Primitive {
  public static final Null NULL = new Null();
  private Null() { }
  
  
  public String toString() {
    return "•NULL";
  }
  public int hashCode() {
    return 387678968; // random yay
  }
  
  public Type type() {
    return Type.nul;
  }
}