package BQN.errors;

import BQN.types.Tokenable;

public class IncorrectArgsError extends BQNError {
  public IncorrectArgsError(String s, Tokenable blame) {
    super(s, blame);
  }
}