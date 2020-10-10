package APL.tools;

import APL.Comp;
import APL.errors.SyntaxError;
import APL.tokenizer.Token;
import APL.tokenizer.types.*;

import java.util.*;

public class Body {
  // code
  public final ArrayList<Token> lns;
  public JFn gen;
  public int iter;
  
  // headers
  public final Token wM, fM, gM, xM;
  public final String self;
  
  // important
  public int start;
  public String[] vars;
  public int[] exp;
  
  // unimportant
  public final boolean immediate;
  public final char type; // one of [afmd\0] - value, function, modifier, composition, unknown
  public final char arity; // one of [mda] - monadic, dyadic, ambivalent
  
  
  public Body(char type, boolean imm, int off, String[] vars, char arity, int[] exp) {
    this.exp = exp; // •COMPiled body
    this.lns = null;
    self = null;
    wM=fM=gM=xM=null;
    immediate = imm;
    this.type = type;
    this.arity = arity;
    start = off;
    this.vars = vars;
  }
  public Body(int off, String[] vars, int[] exp) {
    this.start = off;
    this.vars = vars;
    this.exp = exp;
    immediate = false;
    arity = 'a';
    type = '⍰';
    wM=fM=gM=xM = null;
    self = null;
    lns = null;
  }
  
  public Body(ArrayList<Token> lns, char arity, boolean immediate) { // no header
    this.lns = lns;
    this.type = 0;
    this.arity = arity;
    this.immediate = immediate;
    self=null;
    wM=fM=gM=xM=null;
  }
  
  
  
  public Body(Token hdr, ArrayList<Token> lns, boolean imm) { // given header
    this.lns = lns;
    char type = Comp.typeof(hdr);
    List<Token> ts = ((LineTok)hdr).tokens;
    int sz = ts.size();
    if (sz == 1) {
      Token a = ts.get(0);
      if (type == 'a') { // 1: or v:
        fM=gM=wM=null;
        if (a instanceof NameTok) { // v:
          this.type = 'a'; arity = 'a';
          immediate = true;
        
          xM=null;
          self = ((NameTok) a).name;
        } else { // 1:
          this.type = 'f'; arity = 'm';
          immediate = false;
        
          xM = a;
          self = null;
        }
      } else { // F: or _m: or _d_:
        this.type = type; arity = 'a';
        // if (!imm) throw new SyntaxError("Using 𝕨/𝕩 in immediate definition", a);
        immediate = imm;
      
        fM=gM=wM=xM=null;
        if (!(a instanceof NameTok) || name(a, "𝕣")) throw new SyntaxError(a.source()+" not allowed as self in function header", a);
        self = ((NameTok) a).name;
      }
    } else {
      boolean ae = ts.size()>4; Token a = ae? ts.get(ts.size()-5) : null; char at = ae? a.type : 0;
      boolean be = ts.size()>3; Token b = be? ts.get(ts.size()-4) : null; char bt = be? b.type : 0;
      boolean ce = ts.size()>2; Token c = ce? ts.get(ts.size()-3) : null; char ct = ce? c.type : 0;
      Token d =                               ts.get(ts.size()-2)       ; char dt =     d.type;
      Token e =                               ts.get(ts.size()-1)       ; char et =     e.type;
      if (type == 'a') { // non-immediate definitions
        if (dt == 'f' && ts.size()<=3) { // F 𝕩: or 𝕨 F 𝕩:
          if (ce && ct!='a' && ct!='A'  ||  et!='a') throw new SyntaxError("Invalid header", hdr);
          boolean wo = ce && op(c, "𝕨");
          this.type = 'f'; arity = wo? 'a' : ce? 'd' : 'm';
          immediate = false;
        
          wM = op(c, "𝕨")? null : c; // no 𝕨 handled automatically
          fM=gM=null;
          xM = op(e, "𝕩")? null : e;
        
          if (d instanceof NameTok) self = ((NameTok) d).name;
          else if (op(d, "𝕊")) self = null;
          else throw new SyntaxError(d+" not allowed as self in function header", d);
        
        } else if (dt == 'm') { // F _m 𝕩 or 𝕨 F _m 𝕩
          if (be && bt!='a' && bt!='A'  ||  et!='a') throw new SyntaxError("Invalid header", hdr);
          boolean wo = be && op(b, "𝕨");
          this.type = 'm'; arity = wo? 'a' : be? 'd' : 'm';
          immediate = false;
        
          wM = op(b, "𝕨")? null : b;
          fM = op(c, "𝔽")||op(c, "𝕗")? null : c;
          gM = null;
          xM = op(e, "𝕩")? null : e;
        
          if (d instanceof NameTok) self = ((NameTok) d).name;
          else if (op(d, "𝕣")) self = null;
          else throw new SyntaxError(d.source()+" not allowed as self in function header", d);
        
        } else if (ct == 'd') { // F _d_ G 𝕩: or 𝕨 F _d_ G 𝕩:
          if (ae && at!='a' && at!='A'  ||  et!='a') throw new SyntaxError("Invalid header", hdr);
          boolean wo = ae && op(a, "𝕨");
          this.type = 'd'; arity = wo? 'a' : ae? 'd' : 'm';
          immediate = false;
        
          wM = op(a, "𝕨")? null : a;
          fM = op(b, "𝔽")||op(b, "𝕗")? null : b;
          gM = op(d, "𝔾")||op(d, "𝕘")? null : d;
          xM = op(e, "𝕩")? null : e;
        
          if (c instanceof NameTok) self = ((NameTok) c).name;
          else if (name(c, "𝕣")) self = null;
          else throw new SyntaxError(c.source()+" not allowed as self in function header", c);
        
        } else throw new SyntaxError("Invalid header", hdr);
      } else if (type == 'f') { // immediate operators
        immediate = imm;
        wM=xM=null;
        if (et == 'm') { // F _m:
          this.type = 'm'; arity = 'a';
          fM = op(d, "𝔽")||op(d, "𝕗")? null : d;
          gM = null;
        
          if (e instanceof NameTok) self = ((NameTok) e).name;
          else if (name(e, "𝕣")) self = null;
          else throw new SyntaxError(e.source()+" not allowed as self in function header", e);
        
        } else if (dt == 'd') { // F _d_ G:
          this.type = 'd'; arity = 'a';
          fM = op(c, "𝔽")||op(c, "𝕗")? null : c;
          gM = op(e, "𝔾")||op(e, "𝕘")? null : e;
        
          if (d instanceof NameTok) self = ((NameTok) d).name;
          else if (name(d, "𝕣")) self = null;
          else throw new SyntaxError(d.source()+" not allowed as self in function header", d);
        
        } else throw new SyntaxError("Invalid header", hdr);
      } else throw new SyntaxError("Invalid header", hdr);
    }
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
