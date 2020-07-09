package APL.types.functions.builtins.dops;

import APL.errors.DomainError;
import APL.types.*;
import APL.types.functions.*;

public class RepeatBuiltin extends Dop {
  @Override public String repr() {
    return "⍟";
  }
  
  public Value call(Value f, Value g, Value x, DerivedDop derv) {
    Fun ff = f.asFun();
    Value gf = g.asFun().call(x);
  
    int[] bs = new int[2]; bounds(bs, gf); bs[0]*=-1; // min, max
    
    Value nx = x; Value[] neg = new Value[bs[0]]; for (int i = 0; i < bs[0]; i++) neg[i] = nx = ff.callInv(nx);
    Value px = x; Value[] pos = new Value[bs[1]]; for (int i = 0; i < bs[1]; i++) pos[i] = px = ff.call   (px);
    return replace(gf, neg, x, pos);
  }
  
  public Value callInv(Value f, Value g, Value x) {
    Fun ff = f.asFun();
    if (g instanceof Fun) throw new DomainError("(f⌾g)A cannot be inverted", this);
    
    int am = g.asInt();
    if (am < 0) {
      for (int i = 0; i < -am; i++) {
        x = ff.call(x);
      }
    } else for (int i = 0; i < am; i++) {
      x = ff.callInv(x);
    }
    return x;
  }
  
  public Value call(Value f, Value g, Value w, Value x, DerivedDop derv) {
    Fun ff = f.asFun();
    Value gf = g.asFun().call(w, x);
  
    int[] bs = new int[2]; bounds(bs, gf); bs[0]*=-1; // min, max
  
    Value nx = x; Value[] neg = new Value[bs[0]]; for (int i = 0; i < bs[0]; i++) neg[i] = nx = ff.callInvW(w, nx);
    Value px = x; Value[] pos = new Value[bs[1]]; for (int i = 0; i < bs[1]; i++) pos[i] = px = ff.call    (w, px);
    return replace(gf, neg, x, pos);
  }
  
  public Value callInvW(Value f, Value g, Value w, Value x) {
    Fun ff = f.asFun();
    int am = g.asInt();
    if (am < 0) {
      for (int i = 0; i < -am; i++) {
        x = ff.call(w, x);
      }
    } else for (int i = 0; i < am; i++) {
      x = ff.callInvW(w, x);
    }
    return x;
  }
  
  private static void bounds(int[] res, Value v) {
    if (v.quickDoubleArr()) {
      for (double d : v.asDoubleArr()) {
        int n = Num.toInt(d);
        if (n < res[0]) res[0] = n;
        else if (n > res[1]) res[1] = n;
      }
    } else for (Value c : v) bounds(res, c);
  }
  private static Value replace(Value c, Value[] n, Value z, Value[] p) {
    if (c instanceof Num) {
      int i = (int) ((Num) c).num;
      return i==0? z : i<0? n[-i-1] : p[i-1];
    }
    Value[] vs = new Value[c.ia];
    for (int i = 0; i < vs.length; i++) vs[i] = replace(c.get(i), n, z, p);
    return Arr.create(vs, c.shape);
  }
  
  
  
  public Value callInvA(Value f, Value g, Value w, Value x) {
    Fun ff = f.asFun();
    int am = g.asInt();
    if (am== 1) return ff.callInvA(w, x);
    if (am==-1) return ff.callInvA(x, w);
    
    throw new DomainError("f⌾N: 𝕨-inverting is only possible when N∊¯1 1", this, g);
  }
  
  public Value under(Value f, Value g, Value o, Value x, DerivedDop derv) {
    Fun ff = f.asFun();
    int n = g.asInt();
    return repeat(ff, n, o, x);
  }
  
  public Value repeat(Fun f, int n, Value o, Value x) { // todo don't do recursion?
    if (n==0) {
      return o instanceof Fun? ((Fun) o).call(x) : o;
    }
    
    return repeat(f, n-1, new Fun() { public String repr() { return f.repr(); }
      public Value call(Value x) {
        return f.under(o, x);
      }
    }, x);
  }
}