package APL.types.functions.userDefined;

import APL.*;
import APL.tokenizer.types.DfnTok;
import APL.types.*;
import APL.types.functions.*;



public class Dmop extends Mop {
  public final DfnTok code;
  
  Dmop(DfnTok t, Scope sc) {
    super(sc);
    code = t;
  }
  
  public Value call(Value f, Value w, DerivedMop derv) {
    Main.printdbg("dmop call", w);
    Scope nsc = new Scope(sc);
    nsc.set("𝕗", f); nsc.set("𝔽", f.asFun());
    nsc.set("𝕨", Nothing.inst); nsc.set("𝕎", Nothing.inst);
    nsc.set("𝕩", w); nsc.set("𝕏", f.asFun());
    nsc.set("∇", derv);
    return code.comp.exec(nsc);
  }
  
  public Value call(Value f, Value a, Value w, DerivedMop derv) {
    Main.printdbg("dmop call", a, w);
    Scope nsc = new Scope(sc);
    nsc.set("𝕗", f); nsc.set("𝔽", f.asFun());
    nsc.set("𝕨", a); nsc.set("𝕎", f.asFun());
    nsc.set("𝕩", w); nsc.set("𝕏", f.asFun());
    nsc.set("∇", derv);
    nsc.alphaDefined = true;
    return code.comp.exec(nsc);
  }
  
  public String repr() {
    return code.toRepr();
  }
}