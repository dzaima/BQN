package APL.types.functions.builtins.fns2;

import APL.*;
import APL.errors.*;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.functions.Builtin;


public class GTBuiltin extends Builtin {
  @Override public String repr() {
    return ">";
  }
  
  
  public Value call(Value x) {
    if (x instanceof Arr) {
      if (x instanceof DoubleArr || x instanceof ChrArr || x instanceof BitArr) return x;
      Value[] subs = x.values();
      return merge(subs, x.shape, this);
    } else return x;
  }
  
  public static Value merge(Value[] vals, int[] sh, Tokenable blame) {
    if (vals.length == 0) return EmptyArr.SHAPE0N;
    
    Value first = vals[0];
    int[] def = new int[first.rank];
    System.arraycopy(first.shape, 0, def, 0, def.length);
    boolean allNums = true;
    boolean eqShapes = true;
    for (Value v : vals) {
      if (v.rank != def.length) {
        String msg = blame + ": expected equal ranks of items (shapes " + Main.formatAPL(first.shape) + " vs " + Main.formatAPL(v.shape) + ")";
        if (blame instanceof Callable) throw new RankError(msg, (Callable) blame, v);
        else throw new RankError(msg, v);
      }
      for (int i = 0; i < def.length; i++) {
        if (v.shape[i] != def[i]) {
          eqShapes = false;
          if (v.shape[i] > def[i]) def[i] = v.shape[i];
        }
      }
      if (!v.quickDoubleArr()) {
        allNums = false;
      }
    }
    int subIA = Arr.prod(def);
    int totalIA = subIA * Arr.prod(sh);
    int[] resShape = new int[def.length + sh.length];
    System.arraycopy(sh, 0, resShape, 0, sh.length);
    System.arraycopy(def, 0, resShape, sh.length, def.length);
    
    if (eqShapes) {
      if (allNums) {
        double[] res = new double[totalIA];
        
        int i = 0;
        for (Value v : vals) {
          double[] da = v.asDoubleArr();
          System.arraycopy(da, 0, res, i, da.length);
          i+= subIA;
        }
        return new DoubleArr(res, resShape);
      }
      Value[] res = new Value[totalIA];
      
      int i = 0;
      for (Value v : vals) {
        Value[] va = v.values();
        System.arraycopy(va, 0, res, i, va.length);
        i+= subIA;
      }
      return Arr.create(res, resShape);
    }
    
    if (allNums) {
      double[] res = new double[totalIA];
      
      int i = 0;
      for (Value v : vals) {
        double[] c = v.asDoubleArr();
        int k = 0;
        for (int j : new SimpleIndexer(def, v.shape)) {
          res[i+j] = c[k++];
        }
        // automatic zero padding
        i+= subIA;
      }
      
      return new DoubleArr(res, resShape);
    }
    
    
    Value[] res = new Value[totalIA];
    int i = 0;
    for (Value v : vals) {
      Value proto = v.prototype();
      for (int[] c : new Indexer(def)) {
        res[i++] = v.at(c, proto);
      }
    }
    return Arr.create(res, resShape);
  }
  
  private static final D_NNeB DNF = new D_NNeB() {
    public boolean on(double a, double w) {
      return a > w;
    }
    public void on(BitArr.BA res, double a, double[] w) {
      for (double cw : w) res.add(a > cw);
    }
    public void on(BitArr.BA res, double[] a, double w) {
      for (double ca : a) res.add(ca > w);
    }
    public void on(BitArr.BA res, double[] a, double[] w) {
      for (int i = 0; i < a.length; i++) res.add(a[i] > w[i]);
    }
    public Value call(BigValue a, BigValue w) {
      return a.i.compareTo(w.i) > 0? Num.ONE : Num.ZERO;
    }
  };
  
  public Value call(Value a, Value w) {
    return numChrD(DNF, (ca, cw) -> ca>cw? Num.ONE : Num.ZERO,
      (ca, cw) -> { throw new DomainError("comparing "+ ca.humanType(true)+" and "+cw.humanType(true), this); },
      a, w);
  }
  
}