package BQN.types.callable.builtins.md1;

import BQN.Main;
import BQN.errors.*;
import BQN.tools.FmtInfo;
import BQN.types.Value;
import BQN.types.arrs.*;
import BQN.types.callable.Md1Derv;
import BQN.types.callable.builtins.Md1Builtin;
import BQN.types.callable.builtins.fns.JoinBuiltin;

import java.util.Arrays;

public class InsertBuiltin extends Md1Builtin {
  public String ln(FmtInfo f) { return "˝"; }
  
  public Value call(Value f, Value x, Md1Derv derv) {
    if (x.r()==0) throw new RankError("˝: argument cannot be a scalar", this);
    if (x.ia==0) {
      Value id = f.identity();
      int[] csh = Arrays.copyOfRange(x.shape, 1, x.r());
      if (id==null) {
        for (int c : csh) if (c==0) return new EmptyArr(csh, null);
        if (f instanceof JoinBuiltin && x.r()>1) { // ಠ_ಠ
          int[] sh = new int[x.r()-1];
          System.arraycopy(x.shape, 2, sh, 1, x.shape.length-2);
          return new EmptyArr(sh, x.fItemS());
        }
        throw new DomainError("˝: reducing array with shape "+ Main.fArr(x.shape)+" with a function without an identity value", this);
      }
      return new SingleItemArr(id, csh);
    }
    Value[] vs = CellBuiltin.cells(x);
    Value c2 = vs[vs.length-1];
    for (int i = vs.length-2; i >= 0; i--) c2 = f.call(vs[i], c2);
    return c2;
  }
  
  public Value call(Value f, Value w, Value x, Md1Derv derv) {
    if (x.r()==0) throw new RankError("˝: 𝕩 cannot be a scalar", this);
    Value[] vs = CellBuiltin.cells(x);
    Value c = w;
    for (int i = vs.length-1; i >= 0; i--) c = f.call(vs[i], c);
    return c;
  }
}