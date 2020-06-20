package APL.types.functions.userDefined;

import APL.Scope;
import APL.tokenizer.types.DfnTok;
import APL.types.Obj;

public class UserDefined {
  public static Obj of(DfnTok ts, Scope sc) {
    switch (ts.type) {
      case 'f': return new Dfn(ts, sc);
      case 'm': return new Dmop(ts, sc);
      case 'd': return new Ddop(ts, sc);
      default : throw new IllegalStateException();
    }
  }
}