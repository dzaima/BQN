package APL.types.functions.trains;

import APL.Type;
import APL.types.*;

public class Atop extends Fun {
  private final Fun g;
  private final Fun h;
  public Atop(Fun g, Fun h) {
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
  
  public Value callInvW(Value w, Value x) {
    return h.callInvW(w, g.callInv(x));
  }
  
  public Value callInvA(Value w, Value x) {
    return h.callInvA(g.callInv(w), x);
  }
  
  public Value under(Value o, Value w) {
    return h.under(new Fun() { public String repr() { return g.repr(); }
      public Value call(Value x) {
        return g.under(o, x);
      }
    }, w);
  }
  
  @Override public String repr() {
    return "("+g+" "+h+")";
  }
  
  @Override public Type type() {
    return Type.fn;
  }
}