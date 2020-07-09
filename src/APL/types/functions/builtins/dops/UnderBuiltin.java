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
    Fun wwf = g.asFun();
    return wwf.under(f, x);
  }
  public Value callInv(Value f, Value g, Value x) {
    Fun aaf = f.asFun(); Fun wwf = g.asFun();
    return wwf.under(InvBuiltin.invertM(aaf), x);
  }
  
  public Value call(Value f, Value g, Value w, Value x, DerivedDop derv) {
    Fun aaf = f.asFun(); Fun wwf = g.asFun();
    return wwf.under(new BindA(wwf.call(w), aaf), x);
  }
  public Value callInvW(Value f, Value g, Value w, Value x) {
    Fun wwf = g.asFun();
    return wwf.under(new BindA(wwf.call(w), InvBuiltin.invertW(f.asFun())), x);
  }
  public Value callInvA(Value f, Value g, Value w, Value x) { // structural inverse is not possible; fall back to computational inverse
    Fun wwf = g.asFun();
    Value a1 = wwf.call(w);
    Value w1 = wwf.call(x);
    try {
      return wwf.callInv(f.asFun().callInvA(a1, w1));
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