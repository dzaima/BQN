package APL.tools;

import APL.Scope;
import APL.tokenizer.types.BlockTok;
import APL.types.Value;

public abstract class JFn {
  public Value[] vals;
  public BlockTok[] blocks;
  public abstract Value get(Scope sc, int off);
}
