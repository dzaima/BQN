package APL.types;

import APL.errors.DomainError;

public abstract class Callable extends Primitive {
  public Value callInv (         Value x) { throw new DomainError("Cannot invert "+humanType(true));}
  public Value callInvX(Value w, Value x) { throw new DomainError("Cannot invert "+humanType(true));}
  public Value callInvW(Value w, Value x) { throw new DomainError("Cannot invert "+humanType(true));}
}