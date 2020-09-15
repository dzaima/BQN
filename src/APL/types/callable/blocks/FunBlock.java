package APL.types.callable.blocks;

import APL.*;
import APL.tokenizer.types.BlockTok;
import APL.types.*;


public class FunBlock extends Fun {
  public final BlockTok code;
  public final Scope sc;
  
  public FunBlock(BlockTok t, Scope sc) {
    this.sc = sc;
    code = t;
  }
  
  public Value call(Value x) { // 𝕊𝕩𝕨···
    Main.printdbg("FunBlock call", x);
    return code.exec(sc, null, new Value[]{this, x, Nothing.inst});
  }
  
  public Value call(Value w, Value x) { // 𝕊𝕩𝕨···
    Main.printdbg("FunBlock call", w, x);
    return code.exec(sc, w, new Value[]{this, x, w});
  }
  
  public String repr() {
    return code.toRepr();
  }
}