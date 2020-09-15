package APL.types.callable.blocks;

import APL.*;
import APL.tokenizer.types.BlockTok;
import APL.types.*;
import APL.types.callable.Md2Derv;



public class Md2Block extends Md2 {
  public final BlockTok code;
  public final Scope sc;
  
  public Md2Block(BlockTok t, Scope sc) {
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
    return new Md2BlockHalfDerv(g, this);
  }
  
  public static class Md2BlockHalfDerv extends Md1 {
    public final Value g;
    public final Md2Block op;
    
    public Md2BlockHalfDerv(Value g, Md2Block op) {
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
    
    public boolean eq(Value o) { // reminder: there's a separate Md2HalfDerv
      if (!(o instanceof Md2BlockHalfDerv)) return false;
      Md2BlockHalfDerv that = (Md2BlockHalfDerv) o;
      return g.eq(that.g) && op.eq(that.op);
    }
    public int hashCode() {
      return 31*g.hashCode() + op.hashCode();
    }
  }
  
  public Value call(Value f, Value g, Value x, Md2Derv derv) { // 𝕊𝕩𝕨𝕣𝕗𝕘
    Main.printdbg("Md2Block call", x);
    
    return code.exec(sc, null, new Value[]{derv, x, Nothing.inst, this, f, g});
  }
  
  public Value call(Value f, Value g, Value w, Value x, Md2Derv derv) { // 𝕊𝕩𝕨𝕣𝕗𝕘
    Main.printdbg("Md2Block call", w, x);
    
    return code.exec(sc, w, new Value[]{derv, x, w, this, f, g});
  }
  
  public String repr() {
    return code.toRepr();
  }
}