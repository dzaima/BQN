package BQN.errors;

import BQN.types.Tokenable;

public class DomainError extends BQNError {
  public DomainError(String s) {
    super(s);
  }
  
  public DomainError(String s, Tokenable blame) {
    super(s, blame);
  }
}