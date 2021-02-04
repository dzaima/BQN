package BQN.errors;

import BQN.types.Tokenable;

public class LengthError extends APLError {
  public LengthError(String s) {
    super(s);
  }
  
  public LengthError(String s, Tokenable blame) {
    super(s, blame);
  }
}