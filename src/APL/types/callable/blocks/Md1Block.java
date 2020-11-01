package APL.types.callable.blocks;

import APL.*;
import APL.tokenizer.types.BlockTok;
import APL.tools.FmtInfo;
import APL.types.*;
import APL.types.callable.Md1Derv;


public class Md1Block extends Md1 {
  public final BlockTok code;
  public final Scope sc;
  
  public Md1Block(BlockTok t, Scope sc) {
    this.sc = sc;
    code = t;
  }
  
  public Value derive(Value f) { // ···𝕣𝕗·
    if (!code.immediate) return super.derive(f);
    Main.printdbg("Md1Block immediate call", f);
    return code.exec(sc, null, new Value[]{this, f}, 0);
  }
  
  public Value call(Value f, Value x, Md1Derv derv) { // 𝕊𝕩𝕨𝕣𝕗·
    Main.printdbg("Md1Block call", x);
    return code.exec(sc, null, new Value[]{derv, x, Nothing.inst, this, f}, 0);
  }
  
  public Value call(Value f, Value w, Value x, Md1Derv derv) { // 𝕊𝕩𝕨𝕣𝕗·
    Main.printdbg("Md1Block call", w, x);
    return code.exec(sc, w, new Value[]{derv, x, w, this, f}, 0);
  }
  
  public String ln(FmtInfo f) { return code.toRepr(); }
}