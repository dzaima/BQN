package BQN.tools;

import BQN.Scope;
import BQN.tokenizer.types.BlockTok;
import BQN.types.Value;

public abstract class JFn {
  public abstract Value get(Scope sc, Body bd);
}
