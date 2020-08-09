package APL.errors;

import APL.types.*;

public class ValueError extends APLError {
  
  public ValueError(String s, Tokenable blame) {
    super(s, blame);
  }
  
  public ValueError(String s, Tokenable blame, Tokenable cause) {
    super(s, blame, cause);
  }
}