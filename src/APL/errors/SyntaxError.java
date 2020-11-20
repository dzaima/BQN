package APL.errors;

import APL.types.Tokenable;

public class SyntaxError extends APLError {
  public SyntaxError(String s) {
    super(s);
  }
  
  public SyntaxError(String s, Tokenable blame) {
    super(s, blame);
  }
}