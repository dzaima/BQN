package BQN.errors;

import BQN.types.callable.builtins.fns.AssertBuiltin;

public class AssertionError extends APLError {
  public AssertionError(String msg, AssertBuiltin b) {
    super(msg, b);
  }
}
