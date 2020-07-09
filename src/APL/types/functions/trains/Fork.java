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
  public Value callInv(Value w) {
    if (f.notIdentity()) throw new DomainError("(F G H)𝕩 cannot be inverted", this);
    return h.callInv(g.callInvW(f, w));
  }
  public Value call(Value w, Value x) {
    Value r = h.call(w, x);
    Value l = f.asFun().call(w, x);
    return g.call(l, r);
  }
  
  @Override public Value callInvW(Value a, Value w) {
    if (f.notIdentity()) throw new DomainError("𝕨(F G H)𝕩 cannot be inverted", this);
    return h.callInvW(a, g.callInvW(f, w));
  }
  
  @Override public Value callInvA(Value a, Value w) {
    if (f.notIdentity()) throw new DomainError("𝕨(F G H)𝕩 cannot be inverted", this);
    return h.callInvA(g.callInvW(f, a), w);
  }
  
  @Override public String repr() {
    return "("+f+" "+g+" "+h+")";
  }
  
  public Value under(Value o, Value w) {
    if (f.notIdentity()) throw new DomainError("(F G H)𝕩 cannot be inverted", this);
    return h.under(new Fun() { public String repr() { return g.repr(); }
      public Value call(Value x) {
        return g.underW(o, f, x);
      }
    }, w);
  }
}