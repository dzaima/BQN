package BQN.errors;

import BQN.types.Tokenable;

public class RankError extends BQNError {
  public RankError(String s) {
    super(s);
  }
  
  public RankError(String s, Tokenable blame) {
    super(s, blame);
  }
}