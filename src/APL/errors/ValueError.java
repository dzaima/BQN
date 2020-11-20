package APL.errors;

import APL.types.Tokenable;

public class ValueError extends APLError {
  public ValueError(String s) {
    super(s);
  }
  
  public ValueError(String s, Tokenable blame) {
    super(s, blame);
  }
}