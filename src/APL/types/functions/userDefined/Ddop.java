package APL.types.functions.userDefined;

import APL.*;
import APL.tokenizer.types.DfnTok;
import APL.types.*;
import APL.types.functions.*;



public class Ddop extends Dop {
  public final DfnTok code;
  public final Scope sc;
  
  public Ddop(DfnTok t, Scope sc) {
    this.sc = sc;
    code = t;
  }
  
  public Fun derive(Value aa, Value ww) {
    if (!code.immediate) return super.derive(aa, ww);
    Main.printdbg("ddop immediate call", aa, ww);
    Scope nsc = new Scope(sc);
    int s = code.start(nsc, null, aa, ww, null, this);
    nsc.set("𝕗", aa);
    nsc.set("𝕘", ww);
    return code.comp.exec(nsc, s).asFun();
  }
  
  public Value call(Value aa, Value ww, Value w, DerivedDop derv) {
    Main.printdbg("ddop call", w);
    Scope nsc = new Scope(sc);
    int s = code.start(nsc, null, aa, ww, w, this);
    nsc.set("𝕗", aa);
    nsc.set("𝕘", ww);
    nsc.set("𝕨", Nothing.inst);
    nsc.set("𝕩", w);
    nsc.set("𝕤", derv);
    nsc.set("𝕣", this);
    return code.comp.exec(nsc, s);
  }
  
  public Value call(Value aa, Value ww, Value a, Value w, DerivedDop derv) {
    Main.printdbg("ddop call", a, w);
    Scope nsc = new Scope(sc);
    int s = code.start(nsc, a, aa, ww, w, this);
    nsc.set("𝕗", aa);
    nsc.set("𝕘", ww);
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