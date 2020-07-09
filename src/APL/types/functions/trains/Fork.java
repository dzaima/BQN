package APL.types.functions.trains;

import APL.Type;
import APL.errors.DomainError;
import APL.types.*;

public class Fork extends Fun {
  private final Value f;
  private final Fun g, h;
  public Fork(Value f, Fun g, Fun h) {
    this.f = f;
    this.g = g;
    this.h = h;
  }
  
  @Override
  public Type type() {
    return Type.fn;
  }
  
  public Value call(Value x) {
    Value r = h.call(x);
    Value l = f.asFun().call(x);
    return g.call(l, r);
  }
  public Value callInv(Value x) {
    if (f.notIdentity()) throw new DomainError("(F G H)𝕩 cannot be inverted", this);
    return h.callInv(g.callInvW(f, x));
  }
  public Value call(Value w, Value x) {
    Value r = h.call(w, x);
    Value l = f.asFun().call(w, x);
    return g.call(l, r);
  }
  
  @Override public Value callInvW(Value w, Value x) {
    if (f.notIdentity()) throw new DomainError("𝕨(F G H)𝕩 cannot be inverted", this);
    return h.callInvW(w, g.callInvW(f, x));
  }
  
  @Override public Value callInvA(Value w, Value x) {
    if (f.notIdentity()) throw new DomainError("𝕨(F G H)𝕩 cannot be inverted", this);
    return h.callInvA(g.callInvW(f, w), x);
  }
  
  @Override public String repr() {
    return "("+f+" "+g+" "+h+")";
  }
  
  public Value under(Value o, Value x) {
    if (f.notIdentity()) throw new DomainError("(F G H)𝕩 cannot be inverted", this);
    return h.under(new Fun() { public String repr() { return g.repr(); }
      public Value call(Value x) {
        return g.underW(o, f, x);
      }
    }, x);
  }
}