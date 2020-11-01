package APL.types.callable.builtins.fns;

import APL.tools.*;
import APL.types.Value;
import APL.types.arrs.ChrArr;
import APL.types.callable.builtins.FnBuiltin;

public class FormatBuiltin extends FnBuiltin {
  public String ln(FmtInfo f) { return "⍕"; }
  
  public Value call(Value x) {
    return new ChrArr(Format.outputFmt(x));
  }
}