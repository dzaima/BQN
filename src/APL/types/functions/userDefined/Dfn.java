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
    int s = code.start(nsc, null, null, null, w, this);
    nsc.set("𝕨", Nothing.inst);
    nsc.set("𝕩", w);
    nsc.set("𝕤", this);
    return code.comp.exec(nsc, s);
  }
  
  public Value call(Value a, Value w) {
    Main.printdbg("dfn call", a, w);
    Scope nsc = new Scope(sc);
    int s = code.start(nsc, a, null, null, w, this);
    nsc.set("𝕨", a);
    nsc.set("𝕩", w);
    nsc.set("𝕤", this);
    return code.comp.exec(nsc, s);
  }
  
  public String repr() {
    return code.toRepr();
  }
  
  public String name() { return "dfn"; }
}