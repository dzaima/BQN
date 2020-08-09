package APL.errors;

import APL.types.*;

public class DomainError extends APLError {
  
  public DomainError(String s) {
    super(s);
  }
  
  public DomainError(String s, Tokenable blame) {
    super(s, blame);
  }
  
  public DomainError(String s, Tokenable blame, Tokenable cause) {
    super(s, blame, cause);
  }
}