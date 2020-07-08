package APL.types.functions.userDefined;

import APL.Scope;
import APL.tokenizer.types.DfnTok;
import APL.types.*;

public class UserDefined {
  public static Value of(DfnTok t, Scope sc) {
    switch (t.type) {
      case 'f': return new Dfn (t, sc);
      case 'm': return new Dmop(t, sc);
      case 'd': return new Ddop(t, sc);
      case 'a': {
        Scope nsc = new Scope(sc);
        return t.comp.exec(nsc, t.start(nsc, null, null, null, null, Nothing.inst));
      }
      default : throw new IllegalStateException(t.type+"");
    }
  }
}