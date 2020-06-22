package APL.types.functions.userDefined;

import APL.*;
import APL.tokenizer.types.DfnTok;
import APL.types.*;



public class Dfn extends Fun {
  public final DfnTok code;
  
  public Dfn(DfnTok t, Scope sc) {
    super(sc);
    code = t;
  }
  
  public Value call(Value w) {
    Main.printdbg("dfn call", w);
    Scope nsc = new Scope(sc);
    nsc.set("𝕨", Nothing.inst);
    nsc.set("𝕩", w);
    nsc.set("∇", this);
    return code.comp.exec(nsc);
  }
  
  public Value call(Value a, Value w) {
    Main.printdbg("dfn call", a, w);
    Scope nsc = new Scope(sc);
    nsc.set("𝕨", a);
    nsc.set("𝕩", w);
    nsc.set("∇", this);
    nsc.alphaDefined = true;
    return code.comp.exec(nsc);
  }
  
  public String repr() {
    return code.toRepr();
  }
  
  public String name() { return "dfn"; }
}