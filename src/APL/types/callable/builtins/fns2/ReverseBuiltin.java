package APL.types.callable.builtins.fns2;

import APL.Main;
import APL.errors.DomainError;
import APL.tools.MutVal;
import APL.types.*;
import APL.types.callable.builtins.FnBuiltin;

import java.util.Arrays;

public class ReverseBuiltin extends FnBuiltin {
  @Override public String repr() {
    return "⌽";
  }
  
  
  public Value call(Value x) {
    return on(x);
  }
  public static Value on(Value x) {
    if (x instanceof Primitive) return x;
    return ((Arr) x).reverseOn(0);
  }
  public Value callInv(Value x) {
    return call(x);
  }
  
  
  public Value call(Value w, Value x) {
    if (w instanceof Primitive) return on(w.asInt(), x);
    int[] wi = w.asIntVec();
    if (wi.length > x.rank) throw new DomainError("⌽: length of 𝕨 was greater than rank of 𝕩 ("+(Main.formatAPL(x.shape))+" ≡ ≢𝕩, "+Main.formatAPL(wi)+" ≡ 𝕨)", this);
    wi = Arrays.copyOf(wi, x.rank); // pads with 0s; also creates a mutable copy for moduloing
    if (x.scalar()) return x; // so recursion doesn't have to worry about it
  
    for (int i = 0; i < wi.length; i++) {
      int l = x.shape[i];
      if (l==0) return x;
      int c = wi[i];
      c%= l; if (c<0) c+= l;
      wi[i] = c;
    }
    
    MutVal res = new MutVal(x.shape, x);
    rec(wi, res, x, 0, 0, 0);
    return res.get();
  }
  
  private void rec(int[] w, MutVal res, Value x, int d, int is, int rs) {
    int ax = x.shape[d];
    int mv = w[d];
    is*= ax;
    rs*= ax;
    if (d == x.rank-1) {
      res.copy(x, is   , rs+ax-mv,    mv);
      res.copy(x, is+mv, rs      , ax-mv);
    } else {
      for (int i =  0; i < mv; i++) rec(w, res, x, d+1, is+i, rs+i+ax-mv);
      for (int i = mv; i < ax; i++) rec(w, res, x, d+1, is+i, rs+i   -mv);
    }
  }
  
  @Override public Value callInvX(Value w, Value x) {
    return call(numM(MinusBuiltin.NF, w), x);
  }
  
  
  
  public static Value on(int a, Value x) {
    if (x.ia==0) return x;
    a = Math.floorMod(a, x.shape[0]);
    if (a == 0) return x;
    int csz = Arr.prod(x.shape, 1, x.shape.length);
    int pA = csz*a; // first part
    int pB = x.ia - pA; // second part
    
    MutVal res = new MutVal(x.shape, x);
    res.copy(x, pA,  0, pB);
    res.copy(x,  0, pB, pA);
    return res.get();
  }
}