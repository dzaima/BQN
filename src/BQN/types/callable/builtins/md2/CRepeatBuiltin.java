package BQN.types.callable.builtins.md2;

import BQN.Main;
import BQN.tools.FmtInfo;
import BQN.types.*;
import BQN.types.callable.Md2Derv;
import BQN.types.callable.builtins.Md2Builtin;

import java.util.ArrayList;

public class CRepeatBuiltin extends Md2Builtin {
  public String ln(FmtInfo f) { return "⍡"; }
  
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