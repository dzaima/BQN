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
  
  public Value derive(Value f) { // ···𝕣𝕗·
    if (!code.immediate) return super.derive(f);
    Main.printdbg("dmop immediate call", f);
    
    return code.exec(sc, null, new Value[]{this, f});
  }
  
  public Value call(Value f, Value x, DerivedMop derv) { // 𝕊𝕩𝕨𝕣𝕗·
    Main.printdbg("dmop call", x);
    
    return code.exec(sc, null, new Value[]{derv, x, Nothing.inst, this, f});
  }
  
  public Value call(Value f, Value w, Value x, DerivedMop derv) { // 𝕊𝕩𝕨𝕣𝕗·
    Main.printdbg("dmop call", w, x);
    
    return code.exec(sc, w, new Value[]{derv, x, w, this, f});
  }
  
  public String repr() {
    return code.toRepr();
  }
}