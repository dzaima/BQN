package APL.errors;

import APL.types.*;

public class SyntaxError extends APLError {
  
  public SyntaxError(String s) {
    super(s);
  }
  
  public SyntaxError(String s, Tokenable blame) {
    super(s, blame);
  }
  
  public SyntaxError(String s, Tokenable blame, Tokenable cause) {
    super(s, blame, cause);
  }
}