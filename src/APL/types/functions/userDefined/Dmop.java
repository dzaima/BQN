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
  
  public Fun derive(Value f) { // ···𝕣𝕗·
    if (!code.immediate) return super.derive(f);
    Main.printdbg("dmop immediate call", f);
    Scope nsc = new Scope(sc);
    int s = code.find(nsc, null, f, null, null, this);
    nsc.set(0, this);
    nsc.set(1, f);
    return code.comp.exec(nsc, s).asFun();
  }
  
  public Value call(Value f, Value x, DerivedMop derv) { // 𝕊𝕩𝕨𝕣𝕗·
    Main.printdbg("dmop call", x);
    Scope nsc = new Scope(sc);
    int s = code.find(nsc, null, f, null, x, this);
    nsc.set(0, derv);
    nsc.set(1, x);
    nsc.set(2, Nothing.inst);
    nsc.set(3, this);
    nsc.set(4, f);
    return code.comp.exec(nsc, s);
  }
  
  public Value call(Value f, Value w, Value x, DerivedMop derv) { // 𝕊𝕩𝕨𝕣𝕗·
    Main.printdbg("dmop call", w, x);
    Scope nsc = new Scope(sc);
    int s = code.find(nsc, w, f, null, x, this);
    nsc.set(0, derv);
    nsc.set(1, x);
    nsc.set(2, w);
    nsc.set(3, this);
    nsc.set(4, f);
    return code.comp.exec(nsc, s);
  }
  
  public String repr() {
    return code.toRepr();
  }
}