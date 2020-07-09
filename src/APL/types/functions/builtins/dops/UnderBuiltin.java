package APL.types.functions.builtins.dops;

import APL.errors.DomainError;
import APL.types.*;
import APL.types.functions.*;
import APL.types.functions.builtins.mops.InvBuiltin;

public class UnderBuiltin extends Dop {
  @Override public String repr() {
    return "⌾";
  }
  
  
  
  public Value call(Value f, Value g, Value x, DerivedDop derv) {
    Fun gf = g.asFun();
    return gf.under(f, x);
  }
  public Value callInv(Value f, Value g, Value x) {
    Fun ff = f.asFun(); Fun gf = g.asFun();
    return gf.under(InvBuiltin.invertM(ff), x);
  }
  
  public Value call(Value f, Value g, Value w, Value x, DerivedDop derv) {
    Fun ff = f.asFun(); Fun gf = g.asFun();
    return gf.under(new BindA(gf.call(w), ff), x);
  }
  public Value callInvW(Value f, Value g, Value w, Value x) {
    Fun gf = g.asFun();
    return gf.under(new BindA(gf.call(w), InvBuiltin.invertW(f.asFun())), x);
  }
  public Value callInvA(Value f, Value g, Value w, Value x) { // structural inverse is not possible; fall back to computational inverse
    Fun gf = g.asFun();
    Value w1 = gf.call(w);
    Value x1 = gf.call(x);
    try {
      return gf.callInv(f.asFun().callInvA(w1, x1));
    } catch (DomainError e) { // but add a nice warning about it if a plausible error was received (todo better error management to not require parsing the message?)
      String msg = e.getMessage();
      if (msg.contains("doesn't support") && msg.contains("inverting")) {
        throw new DomainError(msg + " (possibly caused by using f⌾g˜⁼, which only allows computational inverses)", e.cause);
      } throw e;
    }
  }
  
  public static class BindA extends Fun {
    final Value a;
    final Fun f;
    public BindA(Value a, Fun f) {
      this.a = a;
      this.f = f;
    }
    
    public Value call(Value x) {
      return f.call(a, x);
    }
    public Value callInv(Value x) {
      return f.callInvW(a, x);
    }
    
    public String repr() {
      return f.repr();
    }
  }
}