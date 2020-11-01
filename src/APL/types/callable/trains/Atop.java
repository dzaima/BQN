package APL.types.callable.trains;

import APL.tools.FmtInfo;
import APL.types.*;

public class Atop extends Fun {
  private final Value g;
  private final Value h;
  public Atop(Value g, Value h) {
    this.g = g;
    this.h = h;
  }
  
  public Value call(Value x) {
    return g.call(h.call(x));
  }
  public Value callInv(Value x) {
    return h.callInv(g.callInv(x));
  }
  public Value call(Value w, Value x) {
    return g.call(h.call(w, x));
  }
  
  public Value callInvX(Value w, Value x) {
    return h.callInvX(w, g.callInv(x));
  }
  
  public Value callInvW(Value w, Value x) {
    return h.callInvW(g.callInv(w), x);
  }
  
  public Value under(Value o, Value x) {
    return h.under(new Fun() { public String ln(FmtInfo f) { return g.ln(f); }
      public Value call(Value x) {
        return g.under(o, x);
      }
    }, x);
  }
  
  public String ln(FmtInfo f) {
    return "("+g.ln(f)+" "+h.ln(f)+")";
  }
  
  
  public boolean eq(Value o) {
    if (!(o instanceof Atop)) return false;
    Atop that = (Atop) o;
    return this.g.eq(that.g) && this.h.eq(that.h);
  }
  public int hashCode() {
    return 31*g.hashCode() + h.hashCode();
  }
}