package APL.types.functions.builtins.dops;

import APL.types.Value;
import APL.types.functions.*;

public class AmbivalentBuiltin extends Dop {
  public Value call(Value aa, Value ww, Value w, DerivedDop derv) {
    return aa.asFun().call(w);
  }
  public Value callInv(Value aa, Value ww, Value w) {
    return aa.asFun().callInv(w);
  }
  
  public Value call(Value aa, Value ww, Value a, Value w, DerivedDop derv) {
    return ww.asFun().call(a, w);
  }
  public Value callInvA(Value aa, Value ww, Value a, Value w) {
    return ww.asFun().callInvA(a, w);
  }
  public Value callInvW(Value aa, Value ww, Value a, Value w) {
    return ww.asFun().callInvW(a, w);
  }
  
  public String repr() {
    return "⊘";
  }
}
