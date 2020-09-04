package APL.errors;

import APL.types.functions.builtins.fns2.AssertBuiltin;

public class AssertionError extends APLError {
  public AssertionError(String msg, AssertBuiltin b) {
    super(msg, b);
  }
}
