package APL.types.callable.trains;

import APL.errors.DomainError;
import APL.tools.FmtInfo;
import APL.types.*;

public class Fork extends Fun {
  private final Value f, g, h;
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
  public Value callInvX(Value w, Value x) {
    return h.callInvX(w, g.callInvX(f.constant(this), x));
  }
  public Value callInvW(Value w, Value x) {
    if (f instanceof Callable) throw new DomainError("𝕨(F G H)𝕩 cannot be inverted", this);
    return h.callInvW(g.callInvX(f, w), x);
  }
  
  public Value under(Value o, Value x) {
    Value cf = f.constant(this);
    return h.under(new Fun() { public String ln(FmtInfo f) { return g.ln(f); }
      public Value call(Value x) {
        return g.underW(o, cf, x);
      }
    }, x);
  }
  
  
  public boolean eq(Value o) {
    if (!(o instanceof Fork)) return false;
    Fork that = (Fork) o;
    return this.f.eq(that.f) && this.g.eq(that.g) && this.h.eq(that.h);
  }
  public int hashCode() {
    int res =      f.hashCode();
    res = 31*res + g.hashCode();
    res = 31*res + h.hashCode();
    return res;
  }
  public String ln(FmtInfo fi) {
    return "("+f.ln(fi)+" "+g.ln(fi)+" "+h.ln(fi)+")";
  }
}