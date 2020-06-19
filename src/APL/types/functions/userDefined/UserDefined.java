package APL.types.functions.userDefined;

import APL.*;
import APL.tokenizer.Token;
import APL.tokenizer.types.*;
import APL.types.Obj;

public class UserDefined {
  public static Obj of(DfnTok ts, Scope sc) {
    char type = DfnTok.funType(ts, true);
    switch (type) {
      case 'f': return new Dfn(ts, sc);
      case 'm': return new Dmop(ts, sc);
      case 'd': return new Ddop(ts, sc);
      default : throw new IllegalStateException();
    }
  }
}