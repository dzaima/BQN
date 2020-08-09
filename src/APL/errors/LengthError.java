package APL.errors;

import APL.types.Tokenable;

public class LengthError extends APLError {
  
  public LengthError(String s, Tokenable blame) {
    super(s, blame);
  }
  
  public LengthError(String s, Tokenable blame, Tokenable cause) {
    super(s, blame, cause);
  }
}