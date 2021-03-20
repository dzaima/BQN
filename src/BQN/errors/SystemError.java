package BQN.errors;

public class SystemError extends APLError {
  public SystemError(String msg, Throwable p) {
    super(msg);
    initCause(p);
  }
}
