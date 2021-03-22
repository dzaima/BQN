package BQN.tools;

import BQN.Comp;
import BQN.errors.*;
import BQN.tokenizer.Token;
import BQN.tokenizer.types.*;

import java.util.*;

public class Body {
  // code
  public final ArrayList<Token> lns;
  public JFn gen;
  public int iter;
  
  // headers
  public final Token wM, fM, gM, xM;
  public final String self;
  public final int inverse; // 0 - normal; 1 - ⍵; 2 - ⍺
  
  // important
  public int start;
  public String[] vars;
  public HashMap<String, Integer> exp;
  
  // unimportant
  public final boolean immediate;
  public final char type; // one of [afmd\0] - value, function, modifier, composition, unknown
  public char arity; // one of [mda] - monadic, dyadic, ambivalent
  
  
  
  public Body(int off, String[] vars, int[] exp) { // •COMPiled body
    lns = null;
    
    self = null;
    wM=fM=gM=xM = null;
    
    this.start = off;
    this.vars = vars;
    setExp(exp);
    
    immediate = false;
    type = '⍰';
    arity = 'a';
    inverse = 0; // TODO
  }
  
  public Body(ArrayList<Token> lns, char arity, boolean immediate) { // no header
    this.lns = lns;
    this.type = 0;
    this.arity = arity;
    this.immediate = immediate;
    self=null;
    wM=fM=gM=xM=null;
    inverse = 0;
  }
  
  
  
  public Body(ArrayList<Token> lns, Token hdr, boolean canBeImm) { // given header
    this.lns = lns;
    char hdrty = Comp.typeof(hdr);
    List<Token> ts = ((LineTok)hdr).tokens;
    int sz = ts.size();
    if (sz==1 && hdrty=='a') {
      wM=fM=gM = null;
      inverse = 0;
      Token t0 = ts.get(0);
      if (t0 instanceof NameTok) { // v:
        this.type = 'a'; arity = 'a';
        immediate = true;
        
        xM=null;
        self = ((NameTok) t0).name;
      } else { // 1:
        this.type = 'f'; arity = 'm';
        immediate = false;
        
        xM = t0;
        self = null;
      }
    } else {
      char ty = 'f';
      int bi = -123;
      for (int i = 0; i < sz; i++) {
        Token c = ts.get(i);
        if (c.type=='a' || c instanceof OpTok && (((OpTok) c).op.equals("⁼") || ((OpTok) c).op.equals("˜"))) continue;
        
        if (c.type=='m' || c.type=='d') {
          ty = c.type;
          bi = i;
          break;
        }
        if (c.type=='f') bi = i;
      }
      if (bi==-123) throw new SyntaxError("Invalid header", hdr);
      this.type = ty;
      int is = bi+(ty=='d'? 2 : 1); // inverse start
      if (is < sz && ts.get(is) instanceof OpTok) {
        String i1 = ((OpTok) ts.get(is)).op;
        if (i1.equals("⁼")) inverse = 1;
        else if (i1.equals("˜")) {
          if (is+1 >= sz) throw new SyntaxError("Header cannot end with ˜", ts.get(is));
          Token i2 = ts.get(is+1);
          if (!(i2 instanceof OpTok) || !((OpTok) i2).op.equals("⁼")) throw new SyntaxError("Expected ⁼ after ˜ in header", ts.get(bi+2));
          inverse = 2;
        } else inverse = 0;
      } else inverse = 0;
      this.immediate = hdrty!='a' && canBeImm;
      boolean fx = ty!='f';
      boolean gx = ty=='d';
      
      int wi = bi-1 - (fx?1:0);
      int fi = bi-1;
      int gi = bi+1;
      int xi = bi+1 + (gx?1:0) + inverse;
      
      Token st = ts.get(bi);
      
      if (gx  &&  (fi>=0) != (gi<sz)) throw new SyntaxError("Header must either specify both operands or none", st);
      if (!fx || fi>=0) {
        if (wi>0   ) throw new SyntaxError("Invalid header", ts.get(0   ));
        if (xi<sz-1) throw new SyntaxError("Invalid header", ts.get(sz-1));
        if (wi>=0 && xi>=sz) throw new SyntaxError("Header cannot only specify left argument", ts.get(wi));
        
        if (fx) { Token ft = ts.get(fi); fM = op(ft, "𝔽")||op(ft, "𝕗")? null : ft; } else fM = null;
        if (gx) { Token gt = ts.get(gi); gM = op(gt, "𝔾")||op(gt, "𝕘")? null : gt; } else gM = null;
        if (wi>=0) { Token wt = ts.get(wi); wM = op(wt, "𝕨")? null : wt; } else wM = null;
        if (xi<sz) { Token xt = ts.get(xi); xM = op(xt, "𝕩")? null : xt; } else xM = null;
        
      } else { wM = null; fM = null; xM = null; gM = null; }
      
      if (st instanceof NameTok) self = ((NameTok) st).name;
      else if (op(st, ty=='f'? "𝕊" : "𝕣")) self = null;
      else throw new SyntaxError(st.source()+" not allowed as self in header", st);
      
      arity = wM!=null? 'd' : wi<0 && xi<sz? 'm' : inverse==2? 'd' : 'a';
      if (inverse==2 && arity!='d') throw new SyntaxError("𝕊˜⁼-header must be dyadic", hdr);
      if (inverse!=0 && immediate) throw new SyntaxError("Inverse headers cannot be immediate", hdr);
      if (inverse!=0 && fx) throw new NYIError("modifier inverses not yet implemented", hdr);
    }
  }
  
  
  public void setExp(int[] expi) {
    if (expi==null) return;
    exp = new HashMap<>();
    for (int id : expi) exp.put(vars[id], id);
  }
  
  
  public static boolean op(Token tk, String str) {
    return tk instanceof OpTok && ((OpTok) tk).op.equals(str);
  }
  public static boolean name(Token tk, String str) {
    return tk instanceof NameTok && ((NameTok) tk).name.equals(str);
  }
  
  public void addHeader(Comp.Mut m) {
    addVar(m, xM, "𝕩");
    addVar(m, gM, "𝕘");
    if (self!=null && type!='a') {
      m.var(null, type=='f'? "𝕤" : "𝕣", false);
      m.nvar(self);
      m.var(null, self, true);
      m.add(Comp.SETH);
    }
    addVar(m, fM, "𝕗");
    addVar(m, wM, "𝕨");
  }
  
  private void addVar(Comp.Mut m, Token k, String v) {
    if (k==null) return;
    m.var(k, v, false);
    Comp.compM(m, k, true, true);
    m.add(k, Comp.SETH);
  }
}