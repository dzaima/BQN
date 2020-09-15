package APL.types.functions.builtins.mops;

import APL.Main;
import APL.errors.*;
import APL.types.*;
import APL.types.arrs.HArr;
import APL.types.functions.DerivedMop;
import APL.types.functions.builtins.MopBuiltin;

import java.util.*;

public class OldKeyBuiltin extends MopBuiltin {
  @Override public String repr() {
    return "⌸";
  }
  
  public Value call(Value f, Value x, DerivedMop derv) {
    if (f instanceof APLMap) {
      if (x.rank > 1) {
        Value[] arr = new Value[x.ia];
        for (int i = 0; i < x.ia; i++) {
          arr[i] = ((APLMap) f).getRaw(x.get(i));
        }
        return Arr.create(arr, x.shape);
      }
      return ((APLMap) f).getRaw(x);
    }
    if (f instanceof Fun) {
      int i = 0;
      var vals = new HashMap<Value, ArrayList<Value>>();
      var order = new ArrayList<Value>();
      for (Value v : x) {
        if (!vals.containsKey(v)) {
          var l = new ArrayList<Value>();
          l.add(Num.of(i));
          vals.put(v, l);
          order.add(v);
        } else {
          vals.get(v).add(Num.of(i));
        }
        i++;
      }
      var res = new Value[order.size()];
      i = 0;
      for (Value c : order) {
        res[i++] = f.call(c, Arr.create(vals.get(c)));
      }
      return new HArr(res);
    }
    throw new DomainError("⌸: 𝔽 must be a function or a map, was "+f.humanType(true), derv, f);
  }
  
  public Value call(Value f, Value w, Value x, DerivedMop derv) {
    if (f instanceof APLMap) {
      ((APLMap) f).set(w, x);
      return x;
    }
    if (f instanceof Fun) {
      if (!Arrays.equals(w.shape, x.shape)) {
        if (w.rank != x.rank) throw new RankError("dyadic ⌸ expected 𝕨 & 𝕩 to have equal ranks ("+w.rank+" vs "+x.rank+")", derv, x);
        throw new LengthError("dyadic ⌸ expected 𝕨 & 𝕩 to have equal shapes ("+Main.formatAPL(w.shape)+" vs "+Main.formatAPL(x.shape)+")", derv, x);
      }
      HashMap<Value, ArrayList<Value>> vals = new HashMap<>();
      ArrayList<Value> order = new ArrayList<>();
      for (int i = 0; i < w.ia; i++) {
        Value k = x.get(i);
        Value v = w.get(i);
        ArrayList<Value> curr = vals.get(k);
        if (curr == null) {
          ArrayList<Value> newArr = new ArrayList<>();
          vals.put(k, newArr);
          curr = newArr;
          order.add(k);
        }
        curr.add(v);
      }
      Value[] res = new Value[order.size()];
      for (int i = 0; i < order.size(); i++) {
        Value k = order.get(i);
        Value vs = Arr.create(vals.get(k));
        res[i] = f.call(k, vs);
      }
      return Arr.create(res);
    }
    throw new DomainError("⌸: 𝔽 must be a function or a map, was "+f.humanType(true), derv, f);
  }
}