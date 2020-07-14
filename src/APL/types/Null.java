package APL.types;

public class Null extends Primitive {
  public static final Null NULL = new Null();
  private Null() { }
  
  
  public String toString() {
    return "•NULL";
  }
  public int hashCode() {
    return 387678968; // random yay
  }
  
}