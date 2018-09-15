package APL.types.functions.builtins.fns;

import APL.types.Char;
import APL.types.Num;
import APL.types.Obj;
import APL.types.Value;
import APL.types.functions.Builtin;

import java.util.ArrayList;

public class CeilingBuiltin extends Builtin {
  public CeilingBuiltin() {
    super("⌈");
    valid = 0x11;
  }  public Obj call(Value w) {
    return numChr(Num::ceil, Char::upper, w);
  }
  public Obj call(Value a0, Value w0) {
    return scalar((a, w) -> Num.max((Num)a, (Num)w), a0, w0);
  }
}