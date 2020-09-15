package APL.errors;

import APL.types.callable.builtins.fns.AssertBuiltin;

public class AssertionError extends APLError {
  public AssertionError(String msg, AssertBuiltin b) {
    super(msg, b);
  }
}
