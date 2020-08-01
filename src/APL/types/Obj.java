package APL.types;

import APL.tokenizer.Token;

public abstract class Obj implements Tokenable { // union of Settable and Value
  public Token token;
  
  
  final protected int actualHashCode() {
    return super.hashCode();
  }
  
  public Token getToken() {
    return token;
  }
}