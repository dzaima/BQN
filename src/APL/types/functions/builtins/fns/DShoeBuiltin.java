package APL.types.functions.builtins.fns;

import APL.types.*;
import APL.types.functions.builtins.FnBuiltin;

import java.util.*;

@SuppressWarnings("Convert2Diamond") // convert.py chokes if not
public class DShoeBuiltin extends FnBuiltin {
  @Override public String repr() {
    return "∪";
  }
  
  
  
  
  public Value call(Value w, Value x) {
    var m = new LinkedHashSet<Value>(Arrays.asList(w.values()));
    m.addAll(Arrays.asList(x.values()));
    return Arr.create(m.toArray(new Value[0]));
  }
}