package APL.types.callable;

import APL.tools.FmtInfo;
import APL.types.*;

public class Md1Derv extends Fun {
  public final Value f;
  public final Md1 op;
  public Md1Derv(Value f, Md1 op) {
    this.f = f;
    this.op = op;
    token = op.token;
  }
  
  public Value call(Value x) {
    return op.call(f, x, this);
  }
  public Value call(Value w, Value x) {
    return op.call(f, w, x, this);
  }
  public Value callInv(Value x) {
    return op.callInv(f, x);
  }
  public Value callInvX(Value w, Value x) {
    return op.callInvX(f, w, x);
  }
  public Value callInvW(Value w, Value x) {
    return op.callInvW(f, w, x);
  }
  
  public Value under(Value o, Value x) {
    return op.under(f, o, x, this);
  }
  public Value underW(Value o, Value w, Value x) {
    return op.underW(f, o, w, x, this);
  }
  public Value underA(Value o, Value w, Value x) {
    return op.underA(f, o, w, x, this);
  }
  
  
  public String ln(FmtInfo fi) {
    return f.ln(fi)+op.ln(fi);
  }
  public boolean eq(Value o) {
    if (!(o instanceof Md1Derv)) return false;
    Md1Derv that = (Md1Derv) o;
    return this.op.eq(that.op) && this.f.eq(that.f);
  }
  public int hashCode() {
    return 31*f.hashCode() + op.hashCode();
  }
}