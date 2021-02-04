package BQN.types.callable.builtins.fns;

import BQN.errors.DomainError;
import BQN.tools.*;
import BQN.types.Value;
import BQN.types.arrs.ChrArr;
import BQN.types.callable.builtins.FnBuiltin;

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