package APL.errors;

import APL.types.Tokenable;

public class IncorrectArgsError extends APLError {
  public IncorrectArgsError(String s, Tokenable blame) {
    super(s, blame);
  }
}