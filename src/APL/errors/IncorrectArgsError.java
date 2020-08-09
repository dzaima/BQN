package APL.errors;

import APL.types.*;

public class IncorrectArgsError extends APLError {
  
  public IncorrectArgsError(String s, Tokenable blame, Tokenable cause) {
    super(s, blame, cause);
  }
}