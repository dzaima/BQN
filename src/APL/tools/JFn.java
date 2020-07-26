package APL.tools;

import APL.Scope;
import APL.tokenizer.types.DfnTok;
import APL.types.Value;

public abstract class JFn {
  public Value[] vals;
  public DfnTok[] dfns;
  public abstract Value get(Scope sc, int off);
}
