package APL.errors;

import APL.types.Tokenable;

public class RankError extends APLError {
  public RankError(String s) {
    super(s);
  }
  
  public RankError(String s, Tokenable blame) {
    super(s, blame);
  }
}