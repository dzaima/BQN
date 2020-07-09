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
  
  public Fun derive(Value f, Value g) {
    if (!code.immediate) return super.derive(f, g);
    Main.printdbg("ddop immediate call", f, g);
    Scope nsc = new Scope(sc);
    int s = code.start(nsc, null, f, g, null, this);
    nsc.set("𝕗", f);
    nsc.set("𝕘", g);
    return code.comp.exec(nsc, s).asFun();
  }
  
  public Value call(Value f, Value g, Value x, DerivedDop derv) {
    Main.printdbg("ddop call", x);
    Scope nsc = new Scope(sc);
    int s = code.start(nsc, null, f, g, x, this);
    nsc.set("𝕗", f);
    nsc.set("𝕘", g);
    nsc.set("𝕨", Nothing.inst);
    nsc.set("𝕩", x);
    nsc.set("𝕤", derv);
    nsc.set("𝕣", this);
    return code.comp.exec(nsc, s);
  }
  
  public Value call(Value f, Value g, Value w, Value x, DerivedDop derv) {
    Main.printdbg("ddop call", w, x);
    Scope nsc = new Scope(sc);
    int s = code.start(nsc, w, f, g, x, this);
    nsc.set("𝕗", f);
    nsc.set("𝕘", g);
    nsc.set("𝕨", w);
    nsc.set("𝕩", x);
    nsc.set("𝕤", derv);
    nsc.set("𝕣", this);
    return code.comp.exec(nsc, s);
  }
  
  public String repr() {
    return code.toRepr();
  }
}