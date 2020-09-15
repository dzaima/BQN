package APL.types.callable.builtins.md2;

import APL.Main;
import APL.types.*;
import APL.types.callable.Md2Derv;
import APL.types.callable.builtins.Md2Builtin;

import java.util.ArrayList;

public class CRepeatBuiltin extends Md2Builtin {
  @Override public String repr() {
    return "⍡";
  }
  
  @Override public Value call(Value f, Value g, Value x, Md2Derv derv) {
    if (g instanceof Fun) {
      ArrayList<Value> res = new ArrayList<>();
      Value prev = x;
      res.add(prev);
      
      Value next = f.call(prev);
      res.add(next);
      while(!Main.bool(g.call(prev, next))) {
        prev = next;
        next = f.call(prev);
        res.add(next);
      }
      return Arr.create(res);
    } else {
      int n = g.asInt();
      Value[] res = new Value[n];
      Value curr = x;
      for (int i = 0; i < n; i++) {
        curr = f.call(curr);
        res[i] = curr;
      }
      return Arr.create(res);
    }
  }
}