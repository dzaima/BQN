package APL.types.callable.builtins.fns;

import APL.Main;
import APL.errors.*;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.callable.builtins.FnBuiltin;

import java.util.ArrayList;

public class LShoeBuiltin extends FnBuiltin {
  @Override public String repr() {
    return "⊂";
  }
  
  
  
  public Value call(Value x) {
    return SingleItemArr.r0(x);
  }
  
  @Override public Value call(Value w, Value x) {
    if (x.rank != 1) throw new DomainError("⊂: 𝕩 should be of rank 1 ("+Main.formatAPL(x.shape)+" ≡ ≢𝕩)", this);
    if (w.rank != 1) throw new DomainError("⊂: 𝕨 should be of rank 1 ("+Main.formatAPL(w.shape)+" ≡ ≢𝕨)", this);
    if (w.ia+1 != x.ia) throw new LengthError("⊂: (1+≢𝕨) ≡ ≢𝕩 is required ("+Main.formatAPL(w.shape)+" ≡ ≢𝕨; "+Main.formatAPL(x.shape)+" ≡ ≢𝕩)", this);
    int[] wi = w.asIntVec();
    ArrayList<Value> parts = new ArrayList<>();
    
    if (x.quickDoubleArr()) {
      double[] vals = x.asDoubleArr();
      ArrayList<Double> cpart = new ArrayList<>();
      for (int i = 0; i < wi.length; i++) {
        int am = wi[i];
        cpart.add(vals[i]);
        if (am > 0) {
          parts.add(new DoubleArr(cpart));
          for (int j = 0; j < am - 1; j++) parts.add(EmptyArr.SHAPE0N);
          cpart.clear();
        }
      }
      cpart.add(vals[vals.length - 1]);
      parts.add(new DoubleArr(cpart));
    } else {
      Value[] vals = x.values();
      ArrayList<Value> cpart = new ArrayList<>();
      for (int i = 0; i < wi.length; i++) {
        int am = wi[i];
        cpart.add(vals[i]);
        if (am > 0) {
          parts.add(Arr.create(cpart));
          for (int j = 0; j < am - 1; j++) parts.add(new EmptyArr(EmptyArr.SHAPE0, x.safePrototype()));
          cpart.clear();
        }
      }
      cpart.add(vals[vals.length - 1]);
      parts.add(Arr.create(cpart));
    }
    return Arr.create(parts);
  }
}