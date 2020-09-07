package APL.types.functions.trains;

import APL.errors.DomainError;
import APL.types.*;

public class Fork extends Fun {
  private final Value f,  g, h;
  public Fork(Value f, Value g, Value h) {
    this.f = f;
    this.g = g;
    this.h = h;
  }
  
  public Value call(Value x) {
    Value r = h.call(x);
    Value l = f.call(x);
    return g.call(l, r);
  }
  public Value callInv(Value x) {
    return h.callInv(g.callInvX(f.constant(this), x));
  }
  public Value call(Value w, Value x) {
    Value r = h.call(w, x);
    Value l = f.call(w, x);
    return g.call(l, r);
  }
  
  @Override public Value callInvX(Value w, Value x) {
    return h.callInvX(w, g.callInvX(f.constant(this), x));
  }
  
  @Override public Value callInvW(Value w, Value x) {
    if (f instanceof Callable) throw new DomainError("𝕨(F G H)𝕩 cannot be inverted", this);
    return h.callInvW(g.callInvX(f, w), x);
  }
  
  @Override public String repr() {
    return "("+f+" "+g+" "+h+")";
  }
  
  public Value under(Value o, Value x) {
    Value cf = f.constant(this);
    return h.under(new Fun() { public String repr() { return g.repr(); }
      public Value call(Value x) {
        return g.underW(o, cf, x);
      }
    }, x);
  }
}