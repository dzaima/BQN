package APL.types.functions;

import APL.types.*;

public class FunArr extends Fun {
  private final Obj[] os;
  
  public FunArr(Obj[] os) {
    this.os = os;
  }
  
  @Override public String repr() {
    StringBuilder res = new StringBuilder("(");
    boolean first = true;
    for (Obj o : os) {
      if (first) first = false;
      else res.append("⋄");
      res.append(o.toString());
    }
    res.append(")");
    return res.toString();
  }
  
  @Override public Value call(Value x) {
    Value[] vs = new Value[os.length];
    for (int i = 0; i < os.length; i++) {
      vs[i] = ((Fun) os[i]).call(x);
    }
    return Arr.create(vs);
  }
  
  @Override public Value call(Value w, Value x) {
    Value[] vs = new Value[os.length];
    for (int i = 0; i < os.length; i++) {
      vs[i] = ((Fun) os[i]).call(w, x);
    }
    return Arr.create(vs);
  }
}