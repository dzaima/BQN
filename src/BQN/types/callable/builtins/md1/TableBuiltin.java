package BQN.types.callable.builtins.md1;

import BQN.tools.*;
import BQN.types.*;
import BQN.types.arrs.*;
import BQN.types.callable.Md1Derv;
import BQN.types.callable.builtins.Md1Builtin;

public final class TableBuiltin extends Md1Builtin {
  public String ln(FmtInfo f) { return "⌜"; }
  
  public Value call(Value f, Value x, Md1Derv derv) {
    return EachBuiltin.on(f, x);
  }
  public Value callInv(Value f, Value x) {
    return EachBuiltin.onInv(f, x, this);
  }
  
  public Value call(Value f, Value w, Value x, Md1Derv derv) { // TODO use valuecopy
    int ia = w.ia*x.ia;
    int[] sh = new int[w.r() + x.r()];
    System.arraycopy(w.shape, 0, sh, 0, w.r());
    System.arraycopy(x.shape, 0, sh, w.r(), x.r());
    if (ia==0) return new EmptyArr(sh, w.fItemS());
    
    if (w.quickDoubleArr() && x.quickDoubleArr()) {
      Pervasion.NN2N fd = f.dyNum();
      if (fd != null) {
        double[] arr = new double[ia];
        int i2 = 0;
        double[] xd = x.asDoubleArr();
        for (double na : w.asDoubleArr()) {
          for (double nw : xd) {
            arr[i2++] = fd.on(na, nw);
          }
        }
        return new DoubleArr(arr, sh);
      }
    }
    
    int i = 0;
    Value first = f.call(w.first(), x.first());
    
    if (first instanceof Num) {
      double[] dres = new double[ia];
      boolean allNums = true;
      boolean firstSkipped = false;
      Value failure = null;
      
      numatt: for (Value na : w) {
        for (Value nw : x) {
          Value r;
          if (firstSkipped) r = f.call(na, nw);
          else {
            firstSkipped = true;
            r = first;
          }
          if (r instanceof Num) {
            dres[i++] = ((Num) r).num;
          } else {
            allNums = false;
            failure = r;
            break numatt;
          }
        }
      }
      if (allNums) return IntArr.maybe(dres, sh);
      // i points to the place the failure should be
      Value[] res = new Value[ia];
      
      for (int n = 0; n < i; n++) res[n] = Num.of(dres[n]); // slowly copy the data back..
      
      res[i++] = failure; // insert that horrible thing that broke everything
      if (i%x.ia != 0) { // finish the damn row..
        Value va = w.get(i / x.ia);
        for (int wi = i % x.ia; wi < x.ia; wi++) {
          res[i++] = f.call(va, x.get(wi));
        }
      }
      for (int ai = (i+x.ia-1) / x.ia; ai < w.ia; ai++) { // and do the rest, slowly and horribly
        Value va = w.get(ai);
        for (Value vw : x) res[i++] = f.call(va, vw);
      }
      return Arr.create(res, sh);
    }
    boolean firstSkipped2 = false;
    Value[] arr2 = new Value[ia];
    for (Value na : w) {
      for (Value nw : x) {
        if (firstSkipped2) {
          arr2[i++] = f.call(na, nw);
        } else {
          firstSkipped2 = true;
          arr2[i++] = first;
        }
      }
    }
    return Arr.create(arr2, sh);
  }
}