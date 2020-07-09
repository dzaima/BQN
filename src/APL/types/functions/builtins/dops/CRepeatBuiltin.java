package APL.types.functions.builtins.dops;

import APL.Main;
import APL.types.*;
import APL.types.functions.*;

import java.util.ArrayList;

public class CRepeatBuiltin extends Dop {
  @Override public String repr() {
    return "⍡";
  }
  
  @Override public Value call(Value f, Value g, Value x, DerivedDop derv) {
    Fun aaf = f.asFun();
    if (g instanceof Fun) {
      ArrayList<Value> res = new ArrayList<>();
      Value prev = x;
      res.add(prev);
      
      Value next = aaf.call(prev);
      res.add(next);
      Fun wwf = (Fun) g;
      while(!Main.bool(wwf.call(prev, next))) {
        prev = next;
        next = aaf.call(prev);
        res.add(next);
      }
      return Arr.create(res);
    } else {
      int n = g.asInt();
      Value[] res = new Value[n];
      Value curr = x;
      for (int i = 0; i < n; i++) {
        curr = aaf.call(curr);
        res[i] = curr;
      }
      return Arr.create(res);
    }
  }
}