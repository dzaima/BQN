package APL.types.callable.userDefined;

import APL.*;
import APL.tokenizer.types.DfnTok;
import APL.types.*;
import APL.types.callable.DerivedDop;



public class Md2Block extends Md2 {
  public final DfnTok code;
  public final Scope sc;
  
  public Md2Block(DfnTok t, Scope sc) {
    this.sc = sc;
    code = t;
  }
  
  public Value derive(Value f, Value g) { // ···𝕣𝕗𝕘
    if (!code.immediate) return super.derive(f, g);
    Main.printdbg("Md2Block immediate call", f, g);
  
    return code.exec(sc, null, new Value[]{this, f, g});
  }
  
  public Md1 derive(Value g) {
    if (!code.immediate) return super.derive(g);
    Main.printdbg("Md2Block immediate half-derive", g);
    return new HalfDerivedDdop(g, this);
  }
  
  public static class HalfDerivedDdop extends Md1 {
    public final Value g;
    public final Md2Block op;
    
    public HalfDerivedDdop(Value g, Md2Block op) {
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
    Main.printdbg("Md2Block call", x);
    
    return code.exec(sc, null, new Value[]{derv, x, Nothing.inst, this, f, g});
  }
  
  public Value call(Value f, Value g, Value w, Value x, DerivedDop derv) { // 𝕊𝕩𝕨𝕣𝕗𝕘
    Main.printdbg("Md2Block call", w, x);
    
    return code.exec(sc, w, new Value[]{derv, x, w, this, f, g});
  }
  
  public String repr() {
    return code.toRepr();
  }
}