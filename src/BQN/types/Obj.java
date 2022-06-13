package BQN.types;

public abstract class Obj { // union of Settable and Value
  protected int actualHashCode() {
    return super.hashCode();
  }
}