package APL.types.functions.userDefined;

import APL.*;
import APL.tokenizer.types.DfnTok;
import APL.types.*;

public class Dfn extends Fun {
  public final DfnTok code;
  Dfn(DfnTok t, Scope sc) {
    super(sc);
    code = t;
  }
  public Value call(Value w) {
    Main.printdbg("dfn call", w);
    Scope nsc = new Scope(sc);
    nsc.set("𝕨", null); // +TODO was new Variable(nsc, "𝕨")
    nsc.set("𝕩", w);
    nsc.set("∇", this);
    return Main.execLines(code, nsc);
  }
  public Value call(Value a, Value w) {
    Main.printdbg("dfn call", a, w);
    Scope nsc = new Scope(sc);
    nsc.set("𝕨", a);
    nsc.set("𝕩", w);
    nsc.set("∇", this);
    nsc.alphaDefined = true;
    return Main.execLines(code, nsc);
  }
  public String repr() {
    return code.toRepr();
  }
  
  @Override
  public Type type() {
    return Type.fn;
  }
  
  public String name() { return "dfn"; }
}