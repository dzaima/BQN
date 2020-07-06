package APL.types.functions.userDefined;

import APL.*;
import APL.tokenizer.types.DfnTok;
import APL.types.*;
import APL.types.functions.*;



public class Dmop extends Mop {
  public final DfnTok code;
  public final Scope sc;
  
  public Dmop(DfnTok t, Scope sc) {
    this.sc = sc;
    code = t;
  }
  
  public Fun derive(Value aa) {
    if (!code.immediate) return super.derive(aa);
    Main.printdbg("dmop immediate call", aa);
    Scope nsc = new Scope(sc);
    int s = code.start(nsc, null, aa, null, null, this);
    nsc.set("𝕗", aa);
    return code.comp.exec(nsc, s).asFun();
  }
  
  public Value call(Value f, Value w, DerivedMop derv) {
    Main.printdbg("dmop call", w);
    Scope nsc = new Scope(sc);
    int s = code.start(nsc, null, f, null, w, this);
    nsc.set("𝕗", f);
    nsc.set("𝕨", Nothing.inst);
    nsc.set("𝕩", w);
    nsc.set("𝕤", derv);
    nsc.set("𝕣", this);
    return code.comp.exec(nsc, s);
  }
  
  public Value call(Value f, Value a, Value w, DerivedMop derv) {
    Main.printdbg("dmop call", a, w);
    Scope nsc = new Scope(sc);
    int s = code.start(nsc, a, f, null, w, this);
    nsc.set("𝕗", f);
    nsc.set("𝕨", a);
    nsc.set("𝕩", w);
    nsc.set("𝕤", derv);
    nsc.set("𝕣", this);
    return code.comp.exec(nsc, s);
  }
  
  public String repr() {
    return code.toRepr();
  }
}