package APL.types.callable;

import APL.types.*;

public class Md2Derv extends Fun {
  public final Value f, g;
  public final Md2 op;
  public Md2Derv(Value f, Value g, Md2 op) {
    this.f = f;
    this.g = g;
    this.op = op;
    token = op.token;
  }
  
  public Value call(Value x) {
    return op.call(f, g, x, this);
  }
  public Value call(Value w, Value x) {
    return op.call(f, g, w, x, this);
  }
  public Value callInv(Value x) {
    return op.callInv(f, g, x);
  }
  public Value callInvX(Value w, Value x) {
    return op.callInvX(f, g, w, x);
  }
  public Value callInvW(Value w, Value x) {
    return op.callInvW(f, g, w, x);
  }
  
  public Value under(Value o, Value x) {
    return op.under(f, g, o, x, this);
  }
  public Value underW(Value o, Value w, Value x) {
    return op.underW(f, g, o, w, x, this);
  }
  public Value underA(Value o, Value w, Value x) {
    return op.underA(f, g, o, w, x, this);
  }
  
  public String repr() {
    String wws = g.oneliner();
    if (!(g instanceof Arr) && wws.length() != 1) wws = "("+wws+")";
    return f.oneliner()+op.repr()+wws;
  }
  public boolean eq(Value o) {
    if (!(o instanceof Md2Derv)) return false;
    Md2Derv that = (Md2Derv) o;
    return this.op.eq(that.op) && this.f.eq(that.f) && this.g.eq(that.g);
  }
  public int hashCode() {
    int res =     op.hashCode();
    res = 31*res + f.hashCode();
    res = 31*res + g.hashCode();
    return res;
  }
}