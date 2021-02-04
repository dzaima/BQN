package BQN.types.callable.trains;

import BQN.tools.FmtInfo;
import BQN.types.*;

public class Fork extends Fun {
  public final Value f, g, h;
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
    Value fc = f.constant(this, false);
    if (fc!=null) return h.callInv(g.callInvX(fc, x));
    return f.callInv(g.callInvW(x, h.constant(this, true)));
  }
  public Value call(Value w, Value x) {
    Value r = h.call(w, x);
    Value l = f.call(w, x);
    return g.call(l, r);
  }
  public Value callInvX(Value w, Value x) {
    return h.callInvX(w, g.callInvX(f.constant(this, true), x));
  }
  public Value callInvW(Value w, Value x) {
    return h.callInvW(g.callInvX(f.constant(this, true), w), x);
  }
  
  public Value under(Value o, Value x) {
    Value cf = f.constant(this, true);
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