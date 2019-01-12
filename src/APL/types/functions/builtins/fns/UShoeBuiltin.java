package APL.types.functions.builtins.fns;

import APL.types.*;
import APL.types.functions.Builtin;

import java.util.*;

public class UShoeBuiltin extends Builtin {
  public UShoeBuiltin() {
    super("∩", 0x010);
  }
  
  public Obj call(Value a, Value w) {
    var res = new ArrayList<Value>();
    for (Value v : a) if (Arrays.stream(w.values()).anyMatch(v::equals)) res.add(v);
    return Arr.create(res.toArray(new Value[0]));
  }
}