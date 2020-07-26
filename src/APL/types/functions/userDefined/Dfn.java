package APL.types.functions.userDefined;

import APL.*;
import APL.tokenizer.types.DfnTok;
import APL.types.*;
import APL.types.arrs.EmptyArr;


public class Dfn extends Fun {
  public final DfnTok code;
  public final Scope sc;
  
  public Dfn(DfnTok t, Scope sc) {
    this.sc = sc;
    code = t;
  }
  
  public Value call(Value x) { // 𝕊𝕩𝕨···
    Main.printdbg("dfn call", x);
    Scope nsc = new Scope(sc, EmptyArr.NOSTRS);
    nsc.removeMap();
    int s = code.find(nsc, null, null, null, x, this);
    nsc.set(0, this);
    nsc.set(1, x);
    nsc.set(2, Nothing.inst);
    return code.comp.exec(nsc, s);
  }
  
  public Value call(Value w, Value x) { // 𝕊𝕩𝕨···
    Main.printdbg("dfn call", w, x);
    Scope nsc = new Scope(sc, EmptyArr.NOSTRS);
    int s = code.find(nsc, w, null, null, x, this);
    nsc.set(0, this);
    nsc.set(1, x);
    nsc.set(2, w);
    return code.comp.exec(nsc, s);
  }
  
  public String repr() {
    return code.toRepr();
  }
  
  public String name() { return "dfn"; }
}