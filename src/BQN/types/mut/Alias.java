package BQN.types.mut;

import BQN.Scope;
import BQN.errors.*;
import BQN.tools.Body;
import BQN.types.*;

public class Alias extends Settable {
  public final Settable prev;
  public final String name;
  
  public Alias(Settable prev, Body b, int n) {
    this.prev = prev;
    name = b.nameMap[n];
  }
  
  public Value get(Scope sc) {
    throw new ImplementationError("Reading a left-hand-side namespace");
  }
  
  public void set(Value x, boolean update, Scope sc, Callable blame) {
    prev.set(x, update, sc, blame);
  }
  
  public boolean seth(Value x, Scope sc) {
    return prev.seth(x, sc);
  }
  
  public String name(Scope sc) {
    return name;
  }
}
