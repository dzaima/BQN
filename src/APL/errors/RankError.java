package APL.errors;

import APL.types.Tokenable;

public class RankError extends APLError {
  
  public RankError(String s, Tokenable blame) {
    super(s, blame);
  }
  
  public RankError(String s, Tokenable blame, Tokenable cause) {
    super(s, blame, cause);
  }
}