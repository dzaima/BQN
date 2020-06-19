package APL.types.functions.userDefined;

import APL.*;
import APL.tokenizer.types.DfnTok;
import APL.types.Value;
import APL.types.functions.*;



public class Ddop extends Dop {
  public final DfnTok code;
  
  Ddop(DfnTok t, Scope sc) {
    super(sc);
    code = t;
  }
  
  public Value call(Value aa, Value ww, Value w, DerivedDop derv) {
    Main.printdbg("ddop call", w);
    Scope nsc = new Scope(sc);
    nsc.set("𝕗", aa); nsc.set("𝔽", aa.asFun());
    nsc.set("𝕘", ww); nsc.set("𝔾", ww.asFun());
    nsc.set("𝕨", null); nsc.set("𝕎", null); // +TODO was new Variable(nsc, "𝕨")
    nsc.set("𝕩", w); nsc.set("𝕏", w.asFun());
    nsc.set("𝕩", w);
    nsc.set("∇", derv);
    return code.comp.exec(nsc);
  }
  
  public Value call(Value aa, Value ww, Value a, Value w, DerivedDop derv) {
    Main.printdbg("ddop call", a, w);
    Scope nsc = new Scope(sc);
    nsc.set("𝕗", aa); nsc.set("𝔽", aa.asFun());
    nsc.set("𝕘", ww); nsc.set("𝔾", ww.asFun());
    nsc.set("𝕨", a); nsc.set("𝕎", a.asFun());
    nsc.set("𝕩", w); nsc.set("𝕏", w.asFun());
    nsc.set("∇", derv);
    nsc.alphaDefined = true;
    return code.comp.exec(nsc);
  }
  
  public String repr() {
    return code.toRepr();
  }
}