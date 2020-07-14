package APL.types.functions.builtins.fns2;

import APL.*;
import APL.algs.MutVal;
import APL.errors.RankError;
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
      MutVal res = new MutVal(resShape);
      
      int i = 0;
      for (Value v : vals) {
        res.copy(v, 0, i, v.ia);
        i+= subIA;
      }
      return res.get();
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
    
    
    Value[] res = new Value[totalIA]; // complicated valuecopy
    int i = 0;
    for (Value v : vals) {
      Value proto = v.prototype();
      for (int[] c : new Indexer(def)) {
        res[i++] = v.at(c, proto);
      }
    }
    return Arr.create(res, resShape);
  }
  
  
  public Value call(Value w, Value x) {
    return LTBuiltin.DF.call(x, w);
  }
  
}