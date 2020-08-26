package APL.types.functions.builtins.mops;

import APL.Main;
import APL.errors.*;
import APL.tools.MutVal;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.functions.*;

import java.util.Arrays;

public class EachBuiltin extends Mop {
  @Override public String repr() {
    return "¨";
  }
  
  
  
  public Value call(Value f, Value x, DerivedMop derv) {
    return on(f, x);
  }
  
  public static Value on(Value f, Value x) {
    if (x.scalar()) return SingleItemArr.r0(f.call(x.first()));
    if (f instanceof Callable) {
      MutVal res = new MutVal(x.shape);
      for (int i = 0; i < x.ia; i++) {
        res.set(i, f.call(x.get(i)));
      }
      return res.get();
    } else {
      if (f instanceof Num && Num.isBool(((Num) f).num)) { // bitarr code is very bad at respecting SingleItemArrs
        long[] ls = new long[BitArr.sizeof(x.ia)];
        if (((Num) f).num==1) Arrays.fill(ls, ~0L);
        return new BitArr(ls, x.shape);
      }
      return new SingleItemArr(f, x.shape);
    }
  }
  
  public Value call(Value f, Value w, Value x, DerivedMop derv) {
    if (x.scalar()) {
      if (w.scalar()) return SingleItemArr.r0(f.call(w.first(), x.first()));
      Value[] n = new Value[w.ia];
      for (int i = 0; i < n.length; i++) n[i] = f.call(w.get(i), x.first());
      return Arr.create(n, w.shape);
    }
    if (w.scalar()) {
      Value[] n = new Value[x.ia];
      for (int i = 0; i < n.length; i++) n[i] = f.call(w.first(), x.get(i));
      return Arr.create(n, x.shape);
    }
    
    int mr = Math.min(w.shape.length, x.shape.length);
    if (!Arr.eqPrefix(w.shape, x.shape, mr)) throw new LengthError("shape prefixes not equal ("+Main.formatAPL(w.shape)+" vs "+Main.formatAPL(x.shape)+")", derv, x);
    
    if (w.shape.length == x.shape.length) {
      MutVal res = new MutVal(x.shape);
      for (int i = 0; i < x.ia; i++) {
        res.set(i, f.call(w.get(i), x.get(i)));
      }
      return res.get();
    }
    
    boolean we = w.rank < x.rank; // w is expanded
    int max = Math.max(w.ia, x.ia);
    int min = Math.min(w.ia, x.ia);
    int ext = max/min;
    Value[] n = new Value[max];
    int r = 0;
    if (we) for (int i = 0; i < min; i++) { Value c = w.get(i); for (int j = 0; j < ext; j++) { n[r] = f.call(c, x.get(r)); r++; } }
    else    for (int i = 0; i < min; i++) { Value c = x.get(i); for (int j = 0; j < ext; j++) { n[r] = f.call(w.get(r), c); r++; } }
    return Arr.create(n, we? x.shape : w.shape);
  }
  
  public Value callInv(Value f, Value x) {
    if (!(f instanceof Fun)) throw new DomainError("can't invert A¨", this);
    Value[] n = new Value[x.ia];
    for (int i = 0; i < n.length; i++) {
      n[i] = f.callInv(x.get(i)).squeeze();
    }
    if (x.rank == 0 && n[0] instanceof Primitive) return n[0];
    return Arr.create(n, x.shape);
  }
  
  public Value under(Value f, Value o, Value x, DerivedMop derv) {
    Value[] res2 = new Value[x.ia];
    rec(f, o, x, 0, new Value[x.ia], new Value[1], res2);
    return Arr.create(res2, x.shape);
  }
  
  private static void rec(Value f, Value o, Value x, int i, Value[] args, Value[] resPre, Value[] res) {
    if (i == args.length) {
      Value v = o instanceof Fun? o.call(Arr.create(args, x.shape)) : o;
      resPre[0] = v;
    } else {
      res[i] = f.under(new Fun() { public String repr() { return f.repr()+"¨"; }
        public Value call(Value x) {
          args[i] = x;
          rec(f, o, x, i+1, args, resPre, res);
          return resPre[0].get(i);
        }
      }, x.get(i));
    }
  }
  
  
  public Value underW(Value f, Value o, Value w, Value x, DerivedMop derv) {
    return underW(f, o, w, x, this);
  }
  
  public static Value underW(Value f, Value o, Value w, Value x, Callable blame) {
    if (w.rank!=0 && x.rank!=0 && !Arrays.equals(w.shape, x.shape)) throw new LengthError("shapes not equal ("+Main.formatAPL(w.shape)+" vs "+Main.formatAPL(x.shape)+")", blame, x);
    int ia = Math.max(w.ia, x.ia);
    Value[] res2 = new Value[ia];
    if (w.rank==0 && !(w instanceof Primitive)) w = SingleItemArr.r0(w.first()); // abuse that get doesn't check indexes for simple scalar extension
    if (x.rank==0 && !(x instanceof Primitive)) x = SingleItemArr.r0(w.first());
    rec(f, o, w, x, 0, new Value[ia], new Value[1], res2);
    return Arr.create(res2, x.shape);
  }
  
  private static void rec(Value f, Value o, Value w, Value x, int i, Value[] args, Value[] resPre, Value[] res) {
    if (i == args.length) {
      Value v = o instanceof Fun? o.call(Arr.create(args, x.shape)) : o;
      resPre[0] = v;
    } else {
      res[i] = f.underW(new Fun() { public String repr() { return f.repr()+"¨"; }
        public Value call(Value x) {
          args[i] = x;
          rec(f, o, w, x, i+1, args, resPre, res);
          return resPre[0].get(i);
        }
      }, w.get(i), x.get(i));
    }
  }
}