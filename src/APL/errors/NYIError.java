package APL.errors;

import APL.types.Tokenable;

public class NYIError extends APLError { // AKA LazyError
  
  public NYIError(String s) {
    super(s);
  }
  
  public NYIError(String s, Tokenable blame) {
    super(s, blame);
  }
  
  public NYIError(String s, Tokenable blame, Tokenable cause) {
    super(s, blame, cause);
  }
}