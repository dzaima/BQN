package APL.types.functions.userDefined;

import APL.*;
import APL.tokenizer.types.DfnTok;
import APL.types.Value;
import APL.types.functions.*;


public class Dmop extends Mop {
  public final DfnTok code;
  
  @Override public String repr() {
    return code.toRepr();
  }
  
  Dmop(DfnTok t, Scope sc) {
    super(sc);
    code = t;
  }
  
  public Value call(Value f, Value w, DerivedMop derv) {
    Main.printdbg("dmop call", w);
    Scope nsc = new Scope(sc);
    nsc.set("𝕗", f);
    nsc.set("𝕨", null); // +TODO was new Variable(nsc, "𝕨")
    nsc.set("𝕩", w);
    nsc.set("∇", derv);
    return Main.execLines(code, nsc);
  }
  
  public Value call(Value f, Value a, Value w, DerivedMop derv) {
    Main.printdbg("dmop call", a, w);
    Scope nsc = new Scope(sc);
    nsc.set("𝕗", f);
    nsc.set("𝕨", a);
    nsc.set("𝕩", w);
    nsc.set("∇", derv);
    nsc.alphaDefined = true;
    return Main.execLines(code, nsc);
  }
}