package BQN.types.callable.builtins.fns;

import BQN.errors.DomainError;
import BQN.tools.FmtInfo;
import BQN.types.Value;
import BQN.types.callable.builtins.FnBuiltin;
import BQN.types.callable.builtins.md1.CellBuiltin;

import java.util.Arrays;

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
  
  public Value callInv(Value x) {
    if (x.r()==0 || x.shape[0]!=1) throw new DomainError("≍⁼: Argument shape should start with 1", this);
    return x.ofShape(Arrays.copyOfRange(x.shape, 1, x.shape.length));
  }
  
  public Value callInvX(Value w, Value x) {
    if (x.r()==0) throw new DomainError("≍⁼: 𝕩 cannot be a scalar", this);
    Value[] c = CellBuiltin.cells(x);
    if (c.length!=2) throw new DomainError("≍⁼: Expected 𝕩 to have 2 cells", this);
    if (!c[0].eq(w)) throw new DomainError("≍⁼: 𝕨 didn't match expected", this);
    return c[1];
  }
  
  public Value callInvW(Value w, Value x) {
    if (w.r()==0) throw new DomainError("≍⁼: 𝕨 cannot be a scalar", this);
    Value[] c = CellBuiltin.cells(w);
    if (c.length!=2) throw new DomainError("≍˜⁼: Expected 𝕨 to have 2 cells", this);
    if (!c[1].eq(x)) throw new DomainError("≍˜⁼: 𝕩 didn't match expected", this);
    return c[0];
  }
}