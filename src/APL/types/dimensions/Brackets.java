package APL.types.dimensions;

import APL.*;
import APL.tokenizer.types.*;
import APL.types.*;
import APL.types.functions.builtins.fns2.GTBuiltin;

public class Brackets extends Callable {
  
  public final Value val;
  
  public Brackets(Value val) {
    this.val = val;
  }
  
  public Integer toInt() {
    return val==null? null : val.asInt();
  }
  public int[] toInts() {
    return val==null? null : val.asIntVec();
  }
  
  
  @Override
  public String toString() {
    return "["+val+"]";
  }
  
  public static Obj of(BracketTok t, Scope sc) {
    Value[] lns = new Value[t.tokens.size()];
    for (int i = 0; i < t.tokens.size(); i++) {
      LineTok tk = t.tokens.get(i);
      lns[i] = Main.exec(tk, sc);
    }
    return GTBuiltin.merge(lns, new int[]{lns.length}, t);
  }
  
  
  public Fun asFun() { throw new AssertionError("this object shouldn't be accessible"); }
  public boolean notIdentity() { throw new AssertionError("this object shouldn't be accessible"); }
}