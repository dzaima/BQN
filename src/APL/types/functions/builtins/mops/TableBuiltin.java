package APL.types.functions.builtins.mops;

import APL.algs.Pervasion;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.functions.*;

public final class TableBuiltin extends Mop {
  @Override public String repr() {
    return "⌜";
  }
  
  
  public Value call(Value f, Value w, Value x, DerivedMop derv) {
    int[] sh = new int[w.rank+x.rank];
    System.arraycopy(w.shape, 0, sh, 0, w.rank);
    System.arraycopy(x.shape, 0, sh, w.rank, x.rank);
    
    if (w.ia==0 || x.ia==0) return new EmptyArr(sh, w.safePrototype());
    
    Fun ff = (Fun) f;
    
    if (w.quickDoubleArr() && x.quickDoubleArr()) {
      Pervasion.NN2N fd = ff.dyNum();
      if (fd != null) {
        double[] arr = new double[w.ia*x.ia];
        int i = 0;
        for (double na : w.asDoubleArr()) {
          for (double nw : x.asDoubleArr()) {
            arr[i++] = fd.on(na, nw);
          }
        }
        return new DoubleArr(arr, sh);
      }
    }
    
    int i = 0;
    Value first = ff.call(w.first(), x.first());
    
    if (first instanceof Num) {
      double[] dres = new double[w.ia*x.ia];
      boolean allNums = true;
      boolean firstSkipped = false;
      Value failure = null;
      
      numatt: for (Value na : w) {
        for (Value nw : x) {
          Value r;
          if (firstSkipped) r = ff.call(na, nw);
          else {
            firstSkipped = true;
            r = first;
          }
          if (r instanceof Num) dres[i++] = ((Num) r).num;
          else {
            allNums = false;
            failure = r;
            break numatt;
          }
        }
      }
      if (allNums) {
        if (sh.length == 0) return new Num(dres[0]);
        return new DoubleArr(dres, sh);
      } else { // i points to the place the failure should be
        Value[] res = new Value[w.ia*x.ia];
        for (int n = 0; n < i; n++) { // slowly copy the data back..
          res[n] = new Num(dres[n]);
        }
        res[i++] = failure; // insert that horrible thing that broke everything
        if (i%x.ia != 0) { // finish the damn row..
          Value va = w.get(i / x.ia);
          for (int wi = i % x.ia; wi < x.ia; wi++) {
            res[i++] = ff.call(va, x.get(wi));
          }
        }
        for (int ai = (i+x.ia-1) / x.ia; ai < w.ia; ai++) { // and do the rest, slowly and horribly
          Value va = w.get(ai);
          for (Value vw : x) {
            res[i++] = ff.call(va, vw);
          }
        }
        return Arr.create(res, sh);
      }
    }
    boolean firstSkipped = false;
    Value[] arr = new Value[w.ia*x.ia];
    for (Value na : w) {
      for (Value nw : x) {
        if (firstSkipped) arr[i++] = ff.call(na, nw);
        else {
          firstSkipped = true;
          arr[i++] = first;
        }
      }
    }
    return Arr.create(arr, sh);
  }
}