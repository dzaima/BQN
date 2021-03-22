package BQN.errors;

import BQN.types.Tokenable;

public class NYIError extends BQNError { // AKA LazyError
  public NYIError(String s) {
    super(s);
  }
  
  public NYIError(String s, Tokenable blame) {
    super(s, blame);
  }
}