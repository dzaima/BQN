package APL.types.callable.builtins.md1;

import APL.tools.Pervasion;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.callable.Md1Derv;
import APL.types.callable.builtins.Md1Builtin;

public final class TableBuiltin extends Md1Builtin {
  @Override public String repr() {
    return "⌜";
  }
  
  public Value call(Value f, Value x, Md1Derv derv) {
    return EachBuiltin.on(f, x);
  }
  
  public Value call(Value f, Value w, Value x, Md1Derv derv) { // TODO use valuecopy
    int[] sh = new int[w.rank+x.rank];
    System.arraycopy(w.shape, 0, sh, 0, w.rank);
    System.arraycopy(x.shape, 0, sh, w.rank, x.rank);
    
    if (w.ia==0 || x.ia==0) return new EmptyArr(sh, w.safePrototype());
  
    if (w.quickDoubleArr() && x.quickDoubleArr()) {
      Pervasion.NN2N fd = f.dyNum();
      if (fd != null) {
        double[] arr = new double[w.ia*x.ia];
        int i = 0;
        double[] xd = x.asDoubleArr();
        for (double na : w.asDoubleArr()) {
          for (double nw : xd) {
            arr[i++] = fd.on(na, nw);
          }
        }
        return new DoubleArr(arr, sh);
      }
    }
    
    int i = 0;
    Value first = f.call(w.first(), x.first());
    
    if (first instanceof Num) {
      double[] dres = new double[w.ia*x.ia];
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
      Value[] res = new Value[w.ia*x.ia];
      
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
    boolean firstSkipped = false;
    Value[] arr = new Value[w.ia*x.ia];
    for (Value na : w) {
      for (Value nw : x) {
        if (firstSkipped) {
          arr[i++] = f.call(na, nw);
        } else {
          firstSkipped = true;
          arr[i++] = first;
        }
      }
    }
    return Arr.create(arr, sh);
  }
}