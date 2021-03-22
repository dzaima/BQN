package BQN.errors;

import BQN.types.Tokenable;

public class ImplementationError extends BQNError {
  public ImplementationError(String s) {
    super(s);
  }
  
  public ImplementationError(String s, Tokenable blame) {
    super(s, blame);
  }
  
  public ImplementationError(Throwable orig) {
    super(orig.toString());
    initCause(orig);
  }
}