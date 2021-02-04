package BQN.types.callable.builtins.md1;

import BQN.Main;
import BQN.errors.*;
import BQN.tools.*;
import BQN.types.*;
import BQN.types.arrs.*;
import BQN.types.callable.Md1Derv;
import BQN.types.callable.builtins.Md1Builtin;

import java.util.Arrays;

public class EachBuiltin extends Md1Builtin {
  public String ln(FmtInfo f) { return "¨"; }
  
  public Value call(Value f, Value x, Md1Derv derv) {
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
        long[] res = new long[BitArr.sizeof(x.ia)];
        if (((Num) f).num==1) Arrays.fill(res, ~0L);
        return new BitArr(res, x.shape);
      }
      return new SingleItemArr(f, x.shape);
    }
  }
  
  public Value call(Value f, Value w, Value x, Md1Derv derv) {
    if (x.scalar()) {
      Value x0 = x.first();
      if (w.scalar()) return SingleItemArr.r0(f.call(w.first(), x0));
      Value[] n = new Value[w.ia];
      for (int i = 0; i < n.length; i++) n[i] = f.call(w.get(i), x0);
      return Arr.create(n, w.shape);
    }
    if (w.scalar()) {
      Value[] n = new Value[x.ia];
      Value w0 = w.first();
      for (int i = 0; i < n.length; i++) n[i] = f.call(w0, x.get(i));
      return Arr.create(n, x.shape);
    }
    
    int mr = Math.min(w.r(), x.r());
    if (!Arr.eqPrefix(w.shape, x.shape, mr)) throw new LengthError("shape prefixes not equal ("+Main.formatAPL(w.shape)+" vs "+Main.formatAPL(x.shape)+")", derv);
    
    if (w.r() == x.r()) {
      MutVal res = new MutVal(x.shape);
      for (int i = 0; i < x.ia; i++) res.set(i, f.call(w.get(i), x.get(i)));
      return res.get();
    }
    
    boolean we = w.r() < x.r(); // w is expanded
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
    return onInv(f, x, this);
  }
  public static Value onInv(Value f, Value x, Callable blame) {
    if (x instanceof Primitive) throw new DomainError("F"+blame+"⁼: argument cannot be an atom", blame); 
    if (!(f instanceof Fun)) throw new DomainError("can't invert A"+blame, blame);
    Value[] n = new Value[x.ia];
    for (int i = 0; i < n.length; i++) {
      n[i] = f.callInv(x.get(i));
    }
    if (x.r() == 0 && n[0] instanceof Primitive) return n[0];
    return Arr.create(n, x.shape);
  }
  
  public Value under(Value f, Value o, Value x, Md1Derv derv) {
    Value[] res2 = new Value[x.ia];
    rec(f, o, x, 0, new Value[x.ia], new Value[1], res2);
    return Arr.create(res2, x.shape);
  }
  
  private static void rec(Value f, Value o, Value x, int i, Value[] args, Value[] resPre, Value[] res) {
    if (i == args.length) {
      Value v = o instanceof Fun? o.call(Arr.create(args, x.shape)) : o;
      resPre[0] = v;
    } else {
      res[i] = f.under(new Fun() { public String ln(FmtInfo fi) { return f.ln(fi)+"¨"; }
        public Value call(Value x1) {
          args[i] = x1;
          rec(f, o, x, i+1, args, resPre, res);
          return resPre[0].get(i);
        }
      }, x.get(i));
    }
  }
  
  
  public Value underW(Value f, Value o, Value w, Value x, Md1Derv derv) {
    return underW(f, o, w, x, this);
  }
  
  public static Value underW(Value f, Value o, Value w, Value x, Callable blame) {
    if (w.r()!=0 && x.r()!=0 && !Arrays.equals(w.shape, x.shape)) throw new LengthError("shapes not equal ("+Main.formatAPL(w.shape)+" vs "+Main.formatAPL(x.shape)+")", blame);
    int ia = Math.max(w.ia, x.ia);
    Value[] res2 = new Value[ia];
    if (w.r()==0 && !(w instanceof Primitive)) w = SingleItemArr.r0(w.first()); // abuse that get doesn't check indexes for simple scalar extension
    if (x.r()==0 && !(x instanceof Primitive)) x = SingleItemArr.r0(w.first());
    rec(f, o, w, x, 0, new Value[ia], new Value[1], res2);
    return Arr.create(res2, x.shape);
  }
  
  private static void rec(Value f, Value o, Value w, Value x, int i, Value[] args, Value[] resPre, Value[] res) {
    if (i == args.length) {
      Value v = o instanceof Fun? o.call(Arr.create(args, x.shape)) : o;
      resPre[0] = v;
    } else {
      res[i] = f.underW(new Fun() { public String ln(FmtInfo fi) { return f.ln(fi)+"¨"; }
        public Value call(Value x1) {
          args[i] = x1;
          rec(f, o, w, x, i+1, args, resPre, res);
          return resPre[0].get(i);
        }
      }, w.get(i), x.get(i));
    }
  }
}