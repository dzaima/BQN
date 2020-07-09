package APL.types.functions.builtins.fns;

import APL.types.*;
import APL.types.functions.Builtin;

import java.util.*;

public class UShoeBuiltin extends Builtin {
  @Override public String repr() {
    return "∩";
  }
  
  
  
  public Value call(Value w, Value x) {
    var res = new ArrayList<Value>();
    HashSet<Value> ws = new HashSet<>(Arrays.asList(x.values()));
    for (Value v : w) if (ws.contains(v)) res.add(v);
    return Arr.create(res);
  }
}