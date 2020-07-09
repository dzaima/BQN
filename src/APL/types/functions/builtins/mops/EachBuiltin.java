package APL.types.functions.builtins.mops;

import APL.Main;
import APL.errors.*;
import APL.types.*;
import APL.types.arrs.Rank0Arr;
import APL.types.functions.*;

import java.util.Arrays;

public class EachBuiltin extends Mop {
  @Override public String repr() {
    return "¨";
  }
  
  
  
  public Value call(Value f, Value w, DerivedMop derv) {
    if (w.scalar()) return new Rank0Arr(f.asFun().call(w.first()));
    Fun ff = f.asFun();
    Value[] n = new Value[w.ia];
    for (int i = 0; i < n.length; i++) {
      n[i] = ff.call(w.get(i));
    }
    return Arr.create(n, w.shape);
  }
  public Value call(Value f, Value w, Value x, DerivedMop derv) {
    Fun ff = f.asFun();
    if (x.scalar()) {
      if (w.scalar()) return new Rank0Arr(ff.call(w.first(), x.first()));
      Value[] n = new Value[w.ia];
      for (int i = 0; i < n.length; i++) n[i] = ff.call(w.get(i), x.first());
      return Arr.create(n, w.shape);
    }
    if (w.scalar()) {
      Value[] n = new Value[x.ia];
      for (int i = 0; i < n.length; i++) n[i] = ff.call(w.first(), x.get(i));
      return Arr.create(n, x.shape);
    }
    
    int mr = Math.min(w.shape.length, x.shape.length);
    if (!Arr.eqPrefix(w.shape, x.shape, mr)) throw new LengthError("shape prefixes not equal ("+ Main.formatAPL(w.shape)+" vs "+Main.formatAPL(x.shape)+")", derv, x);
    
    if (w.shape.length == x.shape.length) {
      Value[] n = new Value[x.ia];
      for (int i = 0; i < n.length; i++) {
        n[i] = ff.call(w.get(i), x.get(i));
      }
      return Arr.create(n, x.shape);
    }
    
    boolean we = w.rank < x.rank; // w is expanded
    int max = Math.max(w.ia, x.ia);
    int min = Math.min(w.ia, x.ia);
    int ext = max/min;
    Value[] n = new Value[max];
    int r = 0;
    if (we) for (int i = 0; i < min; i++) { Value c = w.get(i); for (int j = 0; j < ext; j++) { n[r] = ff.call(c, x.get(r)); r++; } }
    else    for (int i = 0; i < min; i++) { Value c = x.get(i); for (int j = 0; j < ext; j++) { n[r] = ff.call(w.get(r), c); r++; } } 
    return Arr.create(n, we? x.shape : w.shape);
  }
  
  public Value callInv(Value f, Value w) {
    if (!(f instanceof Fun)) throw new DomainError("can't invert A¨", this);
    Value[] n = new Value[w.ia];
    for (int i = 0; i < n.length; i++) {
      n[i] = ((Fun) f).callInv(w.get(i)).squeeze();
    }
    if (w.rank == 0 && n[0] instanceof Primitive) return n[0];
    return Arr.create(n, w.shape);
  }
  
  public Value under(Value aa, Value o, Value w, DerivedMop derv) {
    Fun aaf = aa.asFun();
    Value[] res2 = new Value[w.ia];
    rec(aaf, o, w, 0, new Value[w.ia], new Value[1], res2);
    return Arr.create(res2, w.shape);
  }
  
  private static void rec(Fun aa, Obj o, Value w, int i, Value[] args, Value[] resPre, Value[] res) {
    if (i == args.length) {
      Value v = o instanceof Fun? ((Fun) o).call(Arr.create(args, w.shape)) : (Value) o;
      resPre[0] = v;
    } else {
      res[i] = aa.under(new Fun() { public String repr() { return aa.repr()+"¨"; }
        public Value call(Value x) {
          args[i] = x;
          rec(aa, o, w, i+1, args, resPre, res);
          return resPre[0].get(i);
        }
      }, w.get(i));
    }
  }
  
  
  public Value underW(Value aa, Value o, Value a, Value w, DerivedMop derv) {
    return underW(aa.asFun(), o, a, w, this);
  }
  
  public static Value underW(Fun aa, Obj o, Value a, Value w, Callable blame) {
    if (a.rank!=0 && w.rank!=0 && !Arrays.equals(a.shape, w.shape)) throw new LengthError("shapes not equal ("+ Main.formatAPL(a.shape)+" vs "+Main.formatAPL(w.shape)+")", blame, w);
    int ia = Math.max(a.ia, w.ia);
    Value[] res2 = new Value[ia];
    if (a.rank==0 && !(a instanceof Primitive)) a = new Rank0Arr(a.first()); // abuse that get doesn't check indexes for simple scalar extension
    if (w.rank==0 && !(w instanceof Primitive)) w = new Rank0Arr(a.first());
    rec(aa, o, a, w, 0, new Value[ia], new Value[1], res2);
    return Arr.create(res2, w.shape);
  }
  
  private static void rec(Fun aa, Obj o, Value a, Value w, int i, Value[] args, Value[] resPre, Value[] res) {
    if (i == args.length) {
      Value v = o instanceof Fun? ((Fun) o).call(Arr.create(args, w.shape)) : (Value) o;
      resPre[0] = v;
    } else {
      res[i] = aa.underW(new Fun() { public String repr() { return aa.repr()+"¨"; }
        public Value call(Value x) {
          args[i] = x;
          rec(aa, o, a, w, i+1, args, resPre, res);
          return resPre[0].get(i);
        }
      }, a.get(i), w.get(i));
    }
  }
}