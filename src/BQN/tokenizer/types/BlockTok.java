package BQN.tokenizer.types;

import BQN.*;
import BQN.errors.*;
import BQN.tokenizer.Token;
import BQN.tools.*;
import BQN.types.*;
import BQN.types.callable.blocks.*;

import java.util.*;

public class BlockTok extends TokArr {
  public Comp comp;
  private ArrayList<Body> allBodies;
  public final boolean immediate;
  public final static boolean immBlock = true;
  public final Body[] bdM;
  public final Body[] bdD;
  public final Body[] bdMxi;
  public final Body[] bdDxi;
  public final Body[] bdDwi;
  
  public BlockTok(String line, int spos, int epos, ArrayList<Token> tokens) {
    super(line, spos, epos, tokens);
    type = 'f'; boolean canBeImmediate = funType(tokens, this);
    if (tokens.size()==0) throw new SyntaxError("Empty block", this);
    ArrayList<List<Token>> bodyTks = new ArrayList<>();
    int li = 0;
    for (int i = 0; i < tokens.size(); i++) {
      List<Token> cln = ((LineTok) tokens.get(i)).tokens;
      if (cln.size()==1 && cln.get(0) instanceof SemiTok) {
        bodyTks.add(tokens.subList(li, i));
        li = i+1;
      }
    }
    bodyTks.add(li==0? tokens : tokens.subList(li, tokens.size()));
    ArrayList<Body> bodies = new ArrayList<>();
    for (List<Token> part : bodyTks) {
      boolean header = false;
      for (int j = 0; j < part.size(); j++) {
        List<Token> cln = ((LineTok) part.get(j)).tokens;
        if (cln.size()==1 && cln.get(0) instanceof ColonTok) {
          if (j != 1) throw new SyntaxError("Function header mid-body", cln.get(0));
          header = true;
        }
      }
      ArrayList<Token> src = new ArrayList<>(header? part.subList(2, part.size()) : part);
      if (src.size() == 0) throw new SyntaxError("Block contains empty body", this);
      Body body;
      if (header) body = new Body(src, part.get(0), funType(src, this));
      else        body = new Body(src, '\0'       , funType(src, this));
      bodies.add(body);
    }
    for (int i = 0; i < bodies.size()-2; i++) {
      if (bodies.get(i).arity=='\0') throw new SyntaxError("Header-less bodies must be the last two", bodies.get(i).lns.get(0));
    }
    if (bodies.size()>=2) {
      Body pb = bodies.get(bodies.size()-2); boolean p = pb.arity=='\0';
      Body lb = bodies.get(bodies.size()-1); boolean l = lb.arity=='\0';
      if (p && l) { pb.arity='m'; lb.arity='d'; }
      else if (p) throw new SyntaxError("Header-less bodies must be at the end", bodies.get(bodies.size()-2).lns.get(0));
      else if (l) { lb.arity='a'; }
    }
    if (immBlock && canBeImmediate && type=='f' && bodies.size()==1) type = 'a';
    
    char htype = 0;
    for (Body b : bodies) {
      if (b.type != 0) {
        if (b.type=='a' && !canBeImmediate) throw new SyntaxError("Using function tokens in a value block", this);
        if (b.type=='a' && bodies.size()>1) throw new DomainError("Value blocks must contain only 1 body", this);
        if (htype==0) htype = b.type;
        else if (b.type != htype) throw new SyntaxError("Different type headers in one function", this);
      }
    }
    if (htype != 0) {
      if (type=='d' && htype!='d') throw new SyntaxError("Using combinator tokens with non-combinator header", this);
      if (type=='m' && htype=='f') throw new SyntaxError("Using modifier tokens with non-modifier header", this);
      type = htype;
    }
    
    if (type=='m' || type=='d') {
      if (bodies.size() == 1) {
        immediate = bodies.get(0).immediate;
      } else {
        immediate = false;
        for (Body b : bodies) if (b.immediate) throw new SyntaxError("Immediate operators cannot have multiple bodies", this);
      }
    } else {
      immediate = false;
    }
    this.allBodies = bodies;
    
    ArrayList<Body> db   = new ArrayList<>(); ArrayList<Body> mb   = new ArrayList<>();
    ArrayList<Body> dbxi = new ArrayList<>(); ArrayList<Body> mbxi = new ArrayList<>();
    ArrayList<Body> dbwi = new ArrayList<>();
    for (Body c : bodies) {
      if (c.arity!='m') (c.inverse==0? db : c.inverse==1? dbxi : dbwi).add(c);
      if (c.arity!='d') (c.inverse==0? mb :               mbxi       ).add(c);
    }
    bdD   = db  .toArray(new Body[0]); bdM   = mb  .toArray(new Body[0]);
    bdDxi = dbxi.toArray(new Body[0]); bdMxi = mbxi.toArray(new Body[0]);
    bdDwi = dbwi.toArray(new Body[0]);
  }
  
  public void compile(Comp.Mut p) {
    comp = Comp.comp(new Comp.Mut(p), allBodies, this);
    allBodies = null;
  }
  
  public BlockTok(String line, int spos, int epos, List<Token> tokens, boolean pointless) { // for pointless
    super(line, spos, epos, tokens);
    assert pointless;
    immediate = false;
    bdM=bdD = bdMxi=bdDxi = bdDwi = null;
  }
  
  
  public BlockTok(char type, boolean imm, Body[] mb, Body[] db) {
    super(Token.COMP.raw, 0, 18, new ArrayList<>());
    this.type = type;
    immediate = imm;
    bdM = mb;
    bdD = db;
    bdMxi=bdDxi = bdDwi = new Body[0];
  }
  
  public static boolean funType(Token t, BlockTok dt) { // returns if can be immediate, mutates dt's type
    if (t instanceof TokArr) {
      if (!(t instanceof BlockTok)) {
        boolean imm = true;
        for (Token c : ((TokArr) t).tokens) imm&= funType(c, dt);
        return imm;
      }
      return true;
    } else if (t instanceof OpTok) {
      String op = ((OpTok) t).op;
      if (dt.type != 'd') {
        if (op.equals("𝕗") || op.equals("𝔽")) dt.type = 'm';
        else if (op.equals("𝕘") || op.equals("𝔾")) dt.type = 'd';
      }
      return !(op.equals("𝕨") || op.equals("𝕎") || op.equals("𝕩") || op.equals("𝕏") || op.equals("𝕤") || op.equals("𝕊"));
    } else if (t instanceof NameTok) {
      NameTok nt = (NameTok) t;
      if (nt.name.equals("𝕣")) {
        if (nt.rawName.equals("_𝕣_")) dt.type = 'd';
        else if (dt.type!='d') dt.type = 'm';
      }
      return true;
    } else if (t instanceof ParenTok) {
      return funType(((ParenTok) t).ln, dt);
    } else return true;
  }
  
  private boolean funType(List<Token> lns, BlockTok block) { // TODO split up into separate thing getting immediate and type
    boolean imm = true;
    for (Token ln : lns) imm&= funType(ln, block);
    return imm;
  }
  
  public String toRepr() {
    StringBuilder s = new StringBuilder("{");
    if (tokens.size()>0) {
      s.append(tokens.get(0).toRepr());
      int pi = edge(tokens.get(0));
      for (int i = 1; i < tokens.size(); i++) {
        Token c = tokens.get(i);
        int ci = edge(c);
        if (pi==0 && ci==0) s.append(" ⋄ ");
        else if (ci!=2) s.append(" ");
        s.append(c.toRepr());
        pi = ci;
      }
      s.append("}");
    } else s.append("…}");
    return s.toString();
  }
  
  private static int edge(Token t) { // 0 - normal; 1 - ";"; 2 - ":"
    List<Token> ts = ((LineTok) t).tokens;
    if (ts.size()!=1) return 0;
    Token t0 = ts.get(0);
    return t0 instanceof ColonTok? 2 : t0 instanceof SemiTok? 1 : 0;
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
    return varnames(type, immediate || type=='a');
  }
  
  
  
  public Value eval(Scope sc) {
    switch (this.type) {
      case 'f': return new FunBlock(this, sc);
      case 'm': return new Md1Block(this, sc);
      case 'd': return new Md2Block(this, sc);
      case '?': if (Main.vind) return new FunBlock(this, sc);
        /* fallthrough */
      case 'a': {
        Body b = bdD[0];
        return comp.exec(new Scope(sc, b.vars), b);
      }
      default: throw new IllegalStateException(this.type+"");
    }
  }
  
  public Value exec(Scope psc, Value w, Value[] vb, int inv) {
    Scope sc = new Scope(psc);
    boolean dy = w != null;
    for (Body b : inv==0? (dy? bdD : bdM) : inv==1? (dy? bdDxi : bdMxi) : bdDwi) {
      sc.varNames = b.vars; // no cloning is suuuuurely fiiine
      sc.vars = new Value[b.vars.length]; // TODO decide some nicer way than just creating a new array per header
      System.arraycopy(vb, 0, sc.vars, 0, vb.length);
      sc.varAm = b.vars.length;
      Value res = comp.exec(sc, b);
      if (res != null) return res;
    }
    throw new DomainError("No header matched", this);
  }
  
  
  public static class Wrapper extends Primitive {
    public final BlockTok tk;
    public Wrapper(BlockTok tk) { this.tk = tk; }
    
    public boolean eq(Value o) { return o instanceof Wrapper && tk == ((Wrapper) o).tk; }
    public int hashCode() { return tk.hashCode(); }
    public String ln(FmtInfo f) { return tk.toRepr(); }
    public Value pretty(FmtInfo f) { return Format.str("block:"+tk.toRepr()); }
  }
  
  
  public static BlockTok get(Value v, Callable blame) {
    if (v instanceof FunBlock) return ((FunBlock) v).code;
    else if (v instanceof Md1Block) return ((Md1Block) v).code;
    else if (v instanceof Md2Block) return ((Md2Block) v).code;
    else if (v instanceof Wrapper) return ((Wrapper) v).tk;
    else if (blame==null) return null;
    else throw new DomainError(blame+": Argument must be a block", blame);
  }
}