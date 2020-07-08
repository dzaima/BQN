package APL.types.functions.userDefined;

import APL.Scope;
import APL.tokenizer.types.DfnTok;
import APL.types.*;

public class UserDefined {
  public static Value of(DfnTok ts, Scope sc) {
    switch (ts.type) {
      case 'f': return new Dfn(ts, sc);
      case 'm': return new Dmop(ts, sc);
      case 'd': return new Ddop(ts, sc);
      case 'a': {
        Scope nsc = new Scope(sc);
        return ts.comp.exec(nsc, ts.start(nsc, null, null, null, null, Nothing.inst));
      }
      default : throw new IllegalStateException(ts.type+"");
    }
  }
}