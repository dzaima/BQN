package APL.types.callable.builtins.fns;

import APL.errors.DomainError;
import APL.tools.*;
import APL.types.Value;
import APL.types.arrs.ChrArr;
import APL.types.callable.builtins.FnBuiltin;

import java.util.Arrays;

public class FormatBuiltin extends FnBuiltin {
  public String ln(FmtInfo f) { return "⍕"; }
  
  public Value call(Value x) {
    return new ChrArr(Format.outputFmt(x));
  }
  
  public Value call(Value w, Value x) {
    int[] wi = w.asIntVec();
    if (wi.length==0) throw new DomainError("⍕: 𝕨 should have at least 1 item of mode", this);
    FmtInfo fi = FmtInfo.def.with(Arrays.copyOfRange(wi, 1, wi.length));
    if (wi[0] == 0) return x.pretty(fi);
    else if (wi[0] == 1) return new ChrArr(x.ln(fi));
    else throw new DomainError("⍕: ⊑𝕨 should be 0 or 1", this);
  }
}