package APL.types.functions.builtins.fns2;

import APL.types.Value;
import APL.types.functions.builtins.FnBuiltin;

public class LaminateBuiltin extends FnBuiltin {
  public String repr() {
    return "≍";
  }
  
  private static final int[] MSH = new int[]{2};
  public Value call(Value w, Value x) {
    return GTBuiltin.merge(new Value[]{w, x}, MSH, this);
  }
  
  public Value call(Value x) {
    int[] nsh = new int[x.rank+1];
    nsh[0] = 1;
    System.arraycopy(x.shape, 0, nsh, 1, x.shape.length);
    return x.ofShape(nsh);
  }
}