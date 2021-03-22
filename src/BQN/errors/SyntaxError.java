package BQN.errors;

import BQN.types.Tokenable;

public class SyntaxError extends BQNError {
  public SyntaxError(String s) {
    super(s);
  }
  
  public SyntaxError(String s, Tokenable blame) {
    super(s, blame);
  }
}