package APL.types.functions.builtins.dops;

import APL.types.Value;
import APL.types.functions.*;

public class AtopBuiltin extends Dop {
  public String repr() {
    return "∘";
  }
  
  public Value call(Value aa, Value ww, Value w, DerivedDop derv) {
    return aa.asFun().call(ww.asFun().call(w));
  }
  public Value call(Value aa, Value ww, Value a, Value w, DerivedDop derv) {
    return aa.asFun().call(ww.asFun().call(a, w));
  }
  
  // +TODO inverses (from OldJotDiaeresisBuiltin)
}
