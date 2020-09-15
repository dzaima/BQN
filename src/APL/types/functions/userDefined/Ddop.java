package APL.types.functions.userDefined;

import APL.*;
import APL.tokenizer.types.DfnTok;
import APL.types.*;
import APL.types.functions.DerivedDop;



public class Ddop extends Dop {
  public final DfnTok code;
  public final Scope sc;
  
  public Ddop(DfnTok t, Scope sc) {
    this.sc = sc;
    code = t;
  }
  
  public Value derive(Value f, Value g) { // ···𝕣𝕗𝕘
    if (!code.immediate) return super.derive(f, g);
    Main.printdbg("ddop immediate call", f, g);
  
    return code.exec(sc, null, new Value[]{this, f, g});
  }
  
  public Mop derive(Value g) {
    if (!code.immediate) return super.derive(g);
    Main.printdbg("ddop immediate half-derive", g);
    return new HalfDerivedDdop(g, this);
  }
  
  public static class HalfDerivedDdop extends Mop {
    public final Value g;
    public final Ddop op;
    
    public HalfDerivedDdop(Value g, Ddop op) {
      this.g = g;
      this.op = op;
    }
    
    public Value derive(Value f) {
      return op.derive(f, g);
    }
    
    public String repr() {
      String gs = g.oneliner();
      if (!(g instanceof Arr) && gs.length() != 1) gs = "("+gs+")";
      return op.repr()+gs;
    }
    
    public boolean eq(Value o) { // reminder: there's a separate HalfDerivedDop
      if (!(o instanceof HalfDerivedDdop)) return false;
      HalfDerivedDdop that = (HalfDerivedDdop) o;
      return g.eq(that.g) && op.eq(that.op);
    }
    public int hashCode() {
      return 31*g.hashCode() + op.hashCode();
    }
  }
  
  public Value call(Value f, Value g, Value x, DerivedDop derv) { // 𝕊𝕩𝕨𝕣𝕗𝕘
    Main.printdbg("ddop call", x);
    
    return code.exec(sc, null, new Value[]{derv, x, Nothing.inst, this, f, g});
  }
  
  public Value call(Value f, Value g, Value w, Value x, DerivedDop derv) { // 𝕊𝕩𝕨𝕣𝕗𝕘
    Main.printdbg("ddop call", w, x);
    
    return code.exec(sc, w, new Value[]{derv, x, w, this, f, g});
  }
  
  public String repr() {
    return code.toRepr();
  }
}