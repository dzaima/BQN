package BQN.types;

public abstract class Obj { // union of Settable and Value
  final protected int actualHashCode() {
    return super.hashCode();
  }
}