package APL.types.callable.builtins.fns;

import APL.tools.FmtInfo;
import APL.types.Value;
import APL.types.callable.builtins.FnBuiltin;

public class LaminateBuiltin extends FnBuiltin {
  public String ln(FmtInfo f) { return "≍"; }
  
  private static final int[] MSH = new int[]{2};
  public Value call(Value w, Value x) {
    return GTBuiltin.merge(new Value[]{w, x}, MSH, this);
  }
  
  public Value call(Value x) {
    int[] nsh = new int[x.r()+1];
    nsh[0] = 1;
    System.arraycopy(x.shape, 0, nsh, 1, x.r());
    return x.ofShape(nsh);
  }
}