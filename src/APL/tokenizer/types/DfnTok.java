package APL.tokenizer.types;

import APL.*;
import APL.errors.*;
import APL.tokenizer.*;
import APL.types.Value;

import java.util.*;

public class DfnTok extends TokArr<LineTok> {
  public final Comp comp;
  public final boolean immediate;
  public final ArrayList<Body> bodies;
  
  public DfnTok(String line, int spos, int epos, List<LineTok> tokens) {
    super(line, spos, epos, tokens);
    type = 'f';
    ArrayList<List<LineTok>> parts = new ArrayList<>();
    int li = 0;
    for (int i = 0; i < tokens.size(); i++) {
      if (tokens.get(i).end == ';') {
        parts.add(tokens.subList(li, i+1));
        li = i+1;
      }
    }
    parts.add(li==0? tokens : tokens.subList(li, tokens.size()));
    for (int i = 0; i < parts.size(); i++) {
      List<LineTok> part = parts.get(i);
      if (part.size() == 0) throw new SyntaxError("function contained empty body", this);
      for (int j = 1; j < part.size(); j++) {
        LineTok c = part.get(j);
        if (c.end == ':') throw new SyntaxError("function body contained header in the middle", c.tokens.get(c.tokens.size()-1));
      }
      if (i < parts.size()-2  &&  part.get(0).end != ':') throw new SyntaxError("only the last 2 bodies in a function can be header-less", part.get(0));
    }
    int tail;
    if (parts.size() > 1) {
      boolean p = parts.get(parts.size()-2).get(0).end != ':';
      boolean l = parts.get(parts.size()-1).get(0).end != ':';
      if (p && !l) throw new SyntaxError("header-less function bodies must be the last", parts.get(parts.size()-2).get(0));
      tail = p? 2 : l? 1 : 0;
    } else tail = parts.get(0).get(0).end != ':'? 1 : 0;
    ArrayList<List<LineTok>> bodySrcs = new ArrayList<>();
    ArrayList<Body> bodies = new ArrayList<>();
    for (int i = 0; i < parts.size(); i++) {
      List<LineTok> part = parts.get(i);
      List<LineTok> src;
      Body body;
      if (part.get(0).end == ':') {
        src = part.subList(1, part.size());
        body = new Body(part.get(0), funType(src, this));
      } else {
        src = part;
        assert tail != 0;
        int rid = parts.size()-i; 
        body = new Body(tail==1? 'a' : rid==1? 'd' : 'm', funType(src, this));
      }
      bodies.add(body);
      bodySrcs.add(src);
    }
    char htype = 0;
    for (Body b : bodies) {
      if (b.otype != 0) {
        if (htype==0) htype = b.otype;
        else if (b.otype != htype) throw new SyntaxError("Different type headers in one function", b.token);
      }
    }
    if (htype != 0) {
      if (type=='d' && htype!='d') throw new SyntaxError("Using combinator tokens with non-combinator header", this);
      if (type=='m' && htype=='f') throw new SyntaxError("Using modifier tokens with non-modifier header", this);
      type = htype;
    }
    if (type=='m' || type=='d') {
      if (bodies.size() < 2) {
        immediate = bodies.get(0).immediate;
      } else {
        immediate = false;
        for (Body b : bodies) if (b.immediate) throw new SyntaxError("Immediate operator not allowed with multiple bodies", this);
      }
    } else immediate = false; // no {2+2} for now
  
    int[] offs = new int[parts.size()];
    comp = Comp.comp(bodySrcs, offs);
    for (int i = 0; i < bodies.size(); i++) bodies.get(i).start = offs[i];
    
    this.bodies = bodies;
  }  
  
  
  public static class Body {
    public int start;
    public final Token token;
    public final char ftype; // one of [mda] - monadic, dyadic, ambivalent
    public final char otype; // one of [fmd\0] - function, modifier, composition, unknown
    public final boolean noHeader;
    public final boolean immediate;
    public final Token wM, fM, gM, xM;
    public final String self;
  
    public Body(LineTok header, boolean imm) {
      token = header;
      noHeader = false;
      char type = Comp.typeof(header);
      List<Token> ts = header.tokens;
      int sz = ts.size();
      if (sz == 1) {
        Token a = ts.get(0);
        if (type == 'a') { // 1: +TODO v:
          otype = 'f'; ftype = 'm';
          immediate = false;
          
          fM=gM=wM=null;
          xM = a;
          self = null;
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
            if (ce && ct!='a' && ct!='A'  ||  et!='a') throw new SyntaxError("Invalid header", header);
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
            if (be && bt!='a' && bt!='A'  ||  et!='a') throw new SyntaxError("Invalid header", header);
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
            if (ae && at!='a' && at!='A'  ||  et!='a') throw new SyntaxError("Invalid header", header);
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
            
          } else throw new SyntaxError("Invalid header", header);
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
            
          } else throw new SyntaxError("Invalid header", header);
        } else throw new SyntaxError("Invalid header", header);
      }
      
    }
  
    public Body(char f, boolean imm) { // +TODO
      token = null;
      noHeader = true;
      ftype = f;
      otype = 0;
      wM=fM=gM=xM=null;
      immediate = imm;
      self = null;
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
    
    public static boolean op(Token tk, String str) {
      return tk instanceof OpTok && ((OpTok) tk).op.equals(str);
    }
    public static boolean name(Token tk, String str) {
      return tk instanceof NameTok && ((NameTok) tk).name.equals(str);
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
  }
  
  
  public int start(Scope nsc, Value w, Value f, Value g, Value x, Value self) {
    assert nsc.vars.size() == 0;
    for (Body b : bodies) {
      if (b.match(nsc, w, f, g, x)) {
        if (b.self != null) nsc.set(b.self, self);
        return b.start;
      }
      nsc.vars.clear();
    }
    throw new DomainError("No dfn header matched", this);
  }
  
  
  public DfnTok(Comp code, char type) {
    super("•COMP", 0, 5, new ArrayList<>());
    immediate = funType(this, this, true);
    this.type = type;
    comp = code;
    bodies = null; // +TODO ..
  }
  
  public static boolean funType(Token t, DfnTok dt, boolean first) { // returns immediate, mutates dt's type; +TODO remove first
    if (t instanceof TokArr<?>) {
      if (first || !(t instanceof DfnTok)) {
        boolean imm = true;
        for (Token c : ((TokArr<?>) t).tokens) imm&= funType(c, dt, false);
        return imm;
      }
      return true;
    } else if (t instanceof OpTok) {
      String op = ((OpTok) t).op;
      if (dt.type != 'd') {
        if (op.equals("𝕗") || op.equals("𝔽")) dt.type = 'm';
        else if (op.equals("𝕘") || op.equals("𝔾")) dt.type = 'd';
      }
      return !(op.equals("𝕨") || op.equals("𝕎") || op.equals("𝕩") || op.equals("𝕏"));
    } else if (t instanceof ParenTok) {
      return funType(((ParenTok) t).ln, dt, false);
    } else return true;
  }
  
  private boolean funType(List<LineTok> lns, DfnTok dfn) {
    boolean imm = true;
    for (LineTok ln : lns) imm&= funType(ln, dfn, true);
    return imm;
  }
  
  @Override public String toRepr() {
    StringBuilder s = new StringBuilder("{");
    boolean tail = false;
    for (var v : tokens) {
      if (tail) s.append(" ⋄ ");
      s.append(v.toRepr());
      tail = true;
    }
    s.append("}");
    return s.toString();
  }
}