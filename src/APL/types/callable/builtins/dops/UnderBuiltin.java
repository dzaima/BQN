package APL.types.callable.builtins.dops;

import APL.errors.DomainError;
import APL.types.*;
import APL.types.callable.Md2Derv;
import APL.types.callable.builtins.Md2Builtin;
import APL.types.callable.builtins.mops.InvBuiltin;

public class UnderBuiltin extends Md2Builtin {
  @Override public String repr() {
    return "⌾";
  }
  
  
  
  public Value call(Value f, Value g, Value x, Md2Derv derv) {
    return g.under(f, x);
  }
  public Value callInv(Value f, Value g, Value x) {
    return g.under(InvBuiltin.invertM(f), x);
  }
  
  public Value call(Value f, Value g, Value w, Value x, Md2Derv derv) {
    return g.under(new BindA(g.call(w), f), x);
  }
  public Value callInvX(Value f, Value g, Value w, Value x) {
    return g.under(new BindA(g.call(w), InvBuiltin.invertW(f)), x);
  }
  public Value callInvW(Value f, Value g, Value w, Value x) { // structural inverse is not possible; fall back to computational inverse
    Value w1 = g.call(w);
    Value x1 = g.call(x);
    try {
      return g.callInv(f.callInvW(w1, x1));
    } catch (DomainError e) { // but add a nice warning about it if a plausible error was received (todo better error management to not require parsing the message?)
      String msg = e.getMessage();
      if (msg.contains("doesn't support") && msg.contains("inverting")) {
        throw new DomainError(msg + " (possibly caused by using f⌾g˜⁼, which only allows computational inverses)", e.obj);
      } throw e;
    }
  }
  
  public static class BindA extends Fun { // +todo think about merging with ⊸
    final Value w;
    final Value f;
    public BindA(Value w, Value f) {
      this.w = w;
      this.f = f;
    }
    
    public Value call(Value x) {
      return f.call(w, x);
    }
    public Value callInv(Value x) {
      return f.callInvX(w, x);
    }
    
    public String repr() {
      return f.repr();
    }
    public boolean eq(Value o) {
      if (!(o instanceof BindA)) return false;
      BindA that = (BindA) o;
      return this.w.eq(that.w) && this.f.eq(that.f);
    }
    public int hashCode() {
      return 31*w.hashCode() + f.hashCode();
    }
  }
}