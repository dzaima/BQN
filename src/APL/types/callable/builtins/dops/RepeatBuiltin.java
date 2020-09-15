package APL.types.callable.builtins.dops;

import APL.errors.DomainError;
import APL.types.*;
import APL.types.callable.DerivedDop;
import APL.types.callable.builtins.Md2Builtin;

public class RepeatBuiltin extends Md2Builtin {
  @Override public String repr() {
    return "⍟";
  }
  
  public Value call(Value f, Value g, Value x, DerivedDop derv) {
    Value gf = g.call(x);
    
    int[] bs = new int[2]; bounds(bs, gf); bs[0]*=-1; // min, max
    
    Value nx = x; Value[] neg = new Value[bs[0]]; for (int i = 0; i < bs[0]; i++) neg[i] = nx = f.callInv(nx);
    Value px = x; Value[] pos = new Value[bs[1]]; for (int i = 0; i < bs[1]; i++) pos[i] = px = f.call   (px);
    return replace(gf, neg, x, pos);
  }
  
  public Value callInv(Value f, Value g, Value x) {
    if (g instanceof Fun) throw new DomainError("(f⌾g)A cannot be inverted", this);
    
    int am = g.asInt();
    if (am < 0) {
      for (int i = 0; i < -am; i++) {
        x = f.call(x);
      }
    } else for (int i = 0; i < am; i++) {
      x = f.callInv(x);
    }
    return x;
  }
  
  public Value call(Value f, Value g, Value w, Value x, DerivedDop derv) {
    Value gf = g.call(w, x);
    
    int[] bs = new int[2]; bounds(bs, gf); bs[0]*=-1; // min, max
    
    Value nx = x; Value[] neg = new Value[bs[0]]; for (int i = 0; i < bs[0]; i++) neg[i] = nx = f.callInvX(w, nx);
    Value px = x; Value[] pos = new Value[bs[1]]; for (int i = 0; i < bs[1]; i++) pos[i] = px = f.call    (w, px);
    return replace(gf, neg, x, pos);
  }
  
  public Value callInvX(Value f, Value g, Value w, Value x) {
    int am = g.asInt();
    if (am < 0) {
      for (int i = 0; i < -am; i++) {
        x = f.call(w, x);
      }
    } else for (int i = 0; i < am; i++) {
      x = f.callInvX(w, x);
    }
    return x;
  }
  
  private static void bounds(int[] res, Value v) {
    if (v.quickDoubleArr()) {
      for (int n : v.asIntArr()) {
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
  
  
  
  public Value callInvW(Value f, Value g, Value w, Value x) {
    int am = g.asInt();
    if (am== 1) return f.callInvW(w, x);
    if (am==-1) return f.callInvW(x, w);
    
    throw new DomainError("f⌾N: 𝕨-inverting is only possible when N∊¯1 1", this, g);
  }
  
  public Value under(Value f, Value g, Value o, Value x, DerivedDop derv) {
    int n = g.asInt();
    return repeat(f, n, o, x);
  }
  
  public Value repeat(Value f, int n, Value o, Value x) { // todo don't do recursion?
    if (n==0) {
      return o instanceof Fun? o.call(x) : o;
    }
    
    return repeat(f, n-1, new Fun() { public String repr() { return f.repr(); }
      public Value call(Value x) {
        return f.under(o, x);
      }
    }, x);
  }
}