package APL.tools;

import APL.*;
import APL.errors.SyntaxError;
import APL.tokenizer.Token;
import APL.tokenizer.types.*;
import APL.types.Value;

import java.util.*;

public class Body {
  public final DfnTok o;
  public final ArrayList<LineTok> lns;
  public final char ftype; // one of [mda] - monadic, dyadic, ambivalent
  public final char otype; // one of [afmd\0] - value, function, modifier, composition, unknown
  public final boolean noHeader;
  public final boolean immediate;
  public final Token wM, fM, gM, xM;
  public final String self;
  
  public int start;
  public String[] vars;
  
  public Body(DfnTok o, boolean imm, int off, String[] vars) { // •COMPiled body
    this.o = o;
    this.lns = null;
    self = null;
    wM=fM=gM=xM=null;
    immediate = imm;
    noHeader = true;
    otype = 0;
    ftype = 'a';
    this.start = off;
    this.vars = vars;
  }
  
  public Body(DfnTok o, ArrayList<LineTok> lns, char ftype, boolean immediate) { // no header
    noHeader = true;
    this.o = o;
    this.lns = lns;
    this.otype = 0;
    this.ftype = ftype;
    this.immediate = immediate;
    self=null;
    wM=fM=gM=xM=null;
  }
  
  
  
  public Body(DfnTok o, LineTok hdr, ArrayList<LineTok> lns, boolean imm) { // given header
    this.o = o;
    this.lns = lns;
    noHeader = false;
    char type = Comp.typeof(hdr);
    List<Token> ts = hdr.tokens;
    int sz = ts.size();
    if (sz == 1) {
      Token a = ts.get(0);
      if (type == 'a') { // 1: or v:
        fM=gM=wM=null;
        if (a instanceof NameTok) { // v:
          otype = 'a'; ftype = 'a';
          immediate = true;
        
          xM=null;
          self = ((NameTok) a).name;
        } else { // 1:
          otype = 'f'; ftype = 'm';
          immediate = false;
        
          xM = a;
          self = null;
        }
      } else { // F: or _m: or _d_:
        otype = type; ftype = 'a';
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
      Token d =     ts.get(ts.size()-2)       ; char dt =     d.type;
      Token e =     ts.get(ts.size()-1)       ; char et =     e.type;
      if (type == 'a') { // non-immediate definitions
        if (dt == 'f' && ts.size()<=3) { // F 𝕩: or 𝕨 F 𝕩:
          if (ce && ct!='a' && ct!='A'  ||  et!='a') throw new SyntaxError("Invalid header", hdr);
          boolean wo = ce && op(c, "𝕨");
          otype = 'f'; ftype = wo? 'a' : ce? 'd' : 'm';
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
          otype = 'm'; ftype = wo? 'a' : be? 'd' : 'm';
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
          otype = 'd'; ftype = wo? 'a' : ae? 'd' : 'm';
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
          otype = 'm'; ftype = 'a';
          fM = op(d, "𝔽")||op(d, "𝕗")? null : d;
          gM = null;
        
          if (e instanceof NameTok) self = ((NameTok) e).name;
          else if (name(e, "𝕣")) self = null;
          else throw new SyntaxError(e.source()+" not allowed as self in function header", e);
        
        } else if (dt == 'd') { // F _d_ G:
          otype = 'd'; ftype = 'a';
          fM = op(c, "𝔽")||op(c, "𝕗")? null : c;
          gM = op(e, "𝔾")||op(e, "𝕘")? null : e;
        
          if (d instanceof NameTok) self = ((NameTok) d).name;
          else if (name(d, "𝕣")) self = null;
          else throw new SyntaxError(d.source()+" not allowed as self in function header", d);
        
        } else throw new SyntaxError("Invalid header", hdr);
      } else throw new SyntaxError("Invalid header", hdr);
    }
  }
  
  
  
  
  
  public boolean match(Scope sc, Value w, Value f, Value g, Value x) {
    if (ftype != 'a' && (ftype=='m') != (w==null)) return false;
    if (noHeader) return true;
    
    if (xM!=null) if (!matches(sc, xM, x)) return false;
    if (gM!=null) if (!matches(sc, gM, g)) return false;
    if (fM!=null) if (!matches(sc, fM, f)) return false;
    if (wM!=null) if (!matches(sc, wM, w)) return false;
    
    return true;
  }
  private boolean matches(Scope sc, Token t, Value g) {
    if (t instanceof ConstTok) return ((ConstTok) t).val.equals(g); // 2, 'a', "ab"
    if (t instanceof LineTok) {
      if (((LineTok) t).tokens.size()!=1) throw new SyntaxError("Couldn't match "+t);
      return matches(sc, ((LineTok) t).tokens.get(0), g);
    }
    if (t instanceof ParenTok) {
      return matches(sc, ((ParenTok) t).ln, g);
    }
    if (t instanceof NameTok) {
      sc.set(((NameTok) t).name, g);
      return true;
    }
    if (t instanceof ArrayTok) {
      if (g.rank != 1) return false;
      List<LineTok> ts = ((ArrayTok) t).tokens;
      if (g.ia != ts.size()) return false;
      for (int i = 0; i < g.ia; i++) if (!matches(sc, ts.get(i), g.get(i))) return false;
      return true;
    }
    if (t instanceof StrandTok) {
      if (g.rank != 1) return false;
      List<Token> ts = ((StrandTok) t).tokens;
      if (g.ia != ts.size()) return false;
      for (int i = 0; i < g.ia; i++) if (!matches(sc, ts.get(i), g.get(i))) return false;
      return true;
    }
    throw new SyntaxError("Couldn't match "+t.source(), t);
    // throw new SyntaxError("Couldn't match "+t.getClass(), t);
  }
  
  
  public static boolean op(Token tk, String str) {
    return tk instanceof OpTok && ((OpTok) tk).op.equals(str);
  }
  public static boolean name(Token tk, String str) {
    return tk instanceof NameTok && ((NameTok) tk).name.equals(str);
  }
  public static String[] varnames(char t, boolean imm) {
    assert "fmda".indexOf(t)!=-1;
    switch ((t=='d'? 2 : t=='m'? 1 : 0) + (imm? 3 : 0)) { default: throw new IllegalStateException();
      //    𝕊𝕩𝕨𝕣𝕗𝕘 | 012345
      case 0: return new String[]{"𝕤","𝕩","𝕨"            }; // f  012··· | 𝕊𝕩𝕨···
      case 1: return new String[]{"𝕤","𝕩","𝕨","𝕣","𝕗"    }; // m  01234· | 𝕊𝕩𝕨𝕣𝕗·
      case 2: return new String[]{"𝕤","𝕩","𝕨","𝕣","𝕗","𝕘"}; // d  012345 | 𝕊𝕩𝕨𝕣𝕗𝕘
      case 3: return new String[]{                       }; // fi ······ | ······
      case 4: return new String[]{            "𝕣","𝕗"    }; // mi ···01· | 𝕣𝕗····
      case 5: return new String[]{            "𝕣","𝕗","𝕘"}; // di ···012 | 𝕣𝕗𝕘···
    }
  }
  
  public String[] defNames() {
    return varnames(o.type, o.immediate || o.type=='a');
  }
}
