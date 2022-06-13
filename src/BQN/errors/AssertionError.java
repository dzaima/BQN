package BQN.errors;

import BQN.types.callable.builtins.fns.AssertBuiltin;

public class AssertionError extends BQNError {
  public AssertionError(String msg, AssertBuiltin b) {
    super(msg, b);
  }
  public AssertionError() {
    super("Assertion error");
  }
}