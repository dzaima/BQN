package BQN.types.callable.builtins.md1;

import BQN.Main;
import BQN.errors.*;
import BQN.tools.FmtInfo;
import BQN.types.*;
import BQN.types.arrs.HArr;
import BQN.types.callable.Md1Derv;
import BQN.types.callable.builtins.Md1Builtin;

import java.util.*;

public class OldKeyBuiltin extends Md1Builtin {
  public String ln(FmtInfo f) { return "⌸"; }
  
  public Value call(Value f, Value x, Md1Derv derv) {
    if (f instanceof APLMap) {
      if (x.r() > 1) {
        Value[] arr = new Value[x.ia];
        for (int i = 0; i < x.ia; i++) {
          arr[i] = ((APLMap) f).get(x.get(i));
        }
        return Arr.create(arr, x.shape);
      }
      return ((APLMap) f).get(x);
    }
    if (f instanceof Fun) {
      int i = 0;
      HashMap<Value, ArrayList<Value>> vals = new HashMap<>();
      ArrayList<Value> order = new ArrayList<>();
      for (Value v : x) {
        if (!vals.containsKey(v)) {
          ArrayList<Value> l = new ArrayList<>();
          l.add(Num.of(i));
          vals.put(v, l);
          order.add(v);
        } else {
          vals.get(v).add(Num.of(i));
        }
        i++;
      }
      Value[] res = new Value[order.size()];
      i = 0;
      for (Value c : order) {
        res[i++] = f.call(c, Arr.create(vals.get(c)));
      }
      return new HArr(res);
    }
    throw new DomainError("⌸: 𝔽 must be a function or a map, was "+f.humanType(true), derv);
  }
  
  public Value call(Value f, Value w, Value x, Md1Derv derv) {
    if (f instanceof APLMap) {
      ((APLMap) f).set(w, x);
      return x;
    }
    if (f instanceof Fun) {
      if (!Arrays.equals(w.shape, x.shape)) {
        if (w.r() != x.r()) throw new RankError("dyadic ⌸ expected 𝕨 & 𝕩 to have equal ranks ("+w.r()+" vs "+x.r()+")", derv);
        throw new LengthError("dyadic ⌸ expected 𝕨 & 𝕩 to have equal shapes ("+Main.formatAPL(w.shape)+" vs "+Main.formatAPL(x.shape)+")", derv);
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
    throw new DomainError("⌸: 𝔽 must be a function or a map, was "+f.humanType(true), derv);
  }
}