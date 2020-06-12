package APL.types.functions.builtins.dops;

import APL.types.*;
import APL.types.functions.*;

public class AtopBuiltin extends Dop {
  public String repr() {
    return "∘";
  }
  
  public Value call(Obj aa, Obj ww, Value w, DerivedDop derv) {
    return aa.asFun().call(ww.asFun().call(w));
  }
  public Value call(Obj aa, Obj ww, Value a, Value w, DerivedDop derv) {
    return aa.asFun().call(ww.asFun().call(a, w));
  }
  
  // +TODO inverses (from OldJotDiaeresisBuiltin)
}
