package BQN.errors;

import BQN.types.Tokenable;

public class ValueError extends APLError {
  public ValueError(String s) {
    super(s);
  }
  
  public ValueError(String s, Tokenable blame) {
    super(s, blame);
  }
}