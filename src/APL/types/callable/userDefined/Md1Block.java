package APL.types.callable.userDefined;

import APL.*;
import APL.tokenizer.types.DfnTok;
import APL.types.*;
import APL.types.callable.DerivedMop;


public class Md1Block extends Md1 {
  public final DfnTok code;
  public final Scope sc;
  
  public Md1Block(DfnTok t, Scope sc) {
    this.sc = sc;
    code = t;
  }
  
  public Value derive(Value f) { // ···𝕣𝕗·
    if (!code.immediate) return super.derive(f);
    Main.printdbg("Md1Block immediate call", f);
    
    return code.exec(sc, null, new Value[]{this, f});
  }
  
  public Value call(Value f, Value x, DerivedMop derv) { // 𝕊𝕩𝕨𝕣𝕗·
    Main.printdbg("Md1Block call", x);
    
    return code.exec(sc, null, new Value[]{derv, x, Nothing.inst, this, f});
  }
  
  public Value call(Value f, Value w, Value x, DerivedMop derv) { // 𝕊𝕩𝕨𝕣𝕗·
    Main.printdbg("Md1Block call", w, x);
    
    return code.exec(sc, w, new Value[]{derv, x, w, this, f});
  }
  
  public String repr() {
    return code.toRepr();
  }
}