package APL.types.functions.builtins.dops;

import APL.errors.DomainError;
import APL.types.*;
import APL.types.functions.*;

public class RepeatBuiltin extends Dop {
  @Override public String repr() {
    return "⍟";
  }
  
  public Value call(Value aa, Value ww, Value w, DerivedDop derv) {
    Fun aaf = aa.asFun();
    Value wwa = ww.asFun().call(w);
  
    int[] bs = new int[2]; bounds(bs, wwa); bs[0]*=-1; // min, max
    
    Value nx = w; Value[] neg = new Value[bs[0]]; for (int i = 0; i < bs[0]; i++) neg[i] = nx = aaf.callInv(nx);
    Value px = w; Value[] pos = new Value[bs[1]]; for (int i = 0; i < bs[1]; i++) pos[i] = px = aaf.call   (px);
    return replace(wwa, neg, w, pos);
  }
  
  public Value callInv(Value aa, Value ww, Value w) {
    Fun aaf = isFn(aa, '⍶');
    if (ww instanceof Fun) throw new DomainError("(f⍣g)A cannot be inverted", this);
    
    int am = ww.asInt();
    if (am < 0) {
      for (int i = 0; i < -am; i++) {
        w = aaf.call(w);
      }
    } else for (int i = 0; i < am; i++) {
      w = aaf.callInv(w);
    }
    return w;
  }
  
  public Value call(Value aa, Value ww, Value a, Value w, DerivedDop derv) {
    Fun aaf = aa.asFun();
    Value wwa = ww.asFun().call(w);
  
    int[] bs = new int[2]; bounds(bs, wwa); bs[0]*=-1; // min, max
  
    Value nx = w; Value[] neg = new Value[bs[0]]; for (int i = 0; i < bs[0]; i++) neg[i] = nx = aaf.callInvW(a, nx);
    Value px = w; Value[] pos = new Value[bs[1]]; for (int i = 0; i < bs[1]; i++) pos[i] = px = aaf.call    (a, px);
    return replace(wwa, neg, w, pos);
  }
  
  public Value callInvW(Value aa, Value ww, Value a, Value w) {
    Fun aaf = isFn(aa, '⍶');
    int am = ww.asInt();
    if (am < 0) {
      for (int i = 0; i < -am; i++) {
        w = aaf.call(a, w);
      }
    } else for (int i = 0; i < am; i++) {
      w = aaf.callInvW(a, w);
    }
    return w;
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
  
  
  
  public Value callInvA(Value aa, Value ww, Value a, Value w) {
    Fun aaf = isFn(aa, '⍶');
    int am = ww.asInt();
    if (am== 1) return aaf.callInvA(a, w);
    if (am==-1) return aaf.callInvA(w, a);
    
    throw new DomainError("f⍣N: 𝕨-inverting is only possible when N∊¯1 1", this, ww);
  }
  
  public Value under(Value aa, Value ww, Value o, Value w, DerivedDop derv) {
    Fun aaf = isFn(aa, '⍶');
    int n = ww.asInt();
    return repeat(aaf, n, o, w);
  }
  
  public Value repeat(Fun aa, int n, Value o, Value w) { // todo don't do recursion?
    if (n==0) {
      return o instanceof Fun? ((Fun) o).call(w) : o;
    }
    
    return repeat(aa, n-1, new Fun() { public String repr() { return aa.repr(); }
      public Value call(Value w) {
        return aa.under(o, w);
      }
    }, w);
  }
}