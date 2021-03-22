package BQN.errors;

public class SystemError extends BQNError {
  public SystemError(String msg, Throwable p) {
    super(msg);
    initCause(p);
  }
}
