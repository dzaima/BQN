package APL;

import APL.errors.*;
import APL.tokenizer.*;
import APL.tokenizer.types.*;
import APL.tools.*;
import APL.types.*;
import APL.types.functions.*;
import APL.types.functions.builtins.dops.*;
import APL.types.functions.builtins.fns2.*;
import APL.types.functions.builtins.mops.*;
import APL.types.functions.trains.*;
import APL.types.mut.*;

import java.util.*;

public class Comp {
  public final byte[] bc;
  public final Value[] objs;
  public final String[] strs;
  public final DfnTok[] dfns;
  private final Token[] ref;
  private final Token tk;
  
  public static int compileStart = 1; // at which iteration of calling the function should it be compiled to Java bytecode; negative for never, 0 for always
  private int iter;
  private JFn gen;
  
  public Comp(byte[] bc, Value[] objs, String[] strs, DfnTok[] dfns, Token[] ref, Token tk) {
    this.bc = bc;
    this.objs = objs;
    this.strs = strs;
    this.dfns = dfns;
    this.ref = ref;
    this.tk = tk;
  }
  
  public static final byte PUSH =  0; // N; 2
  public static final byte VARO =  1; // N; x/𝕨/𝕏
  public static final byte VARM =  2; // N; mutable x/𝕨/𝕏
  public static final byte ARRO =  3; // N; 1‿2‿3 / ⟨1⋄2⋄3⟩
  public static final byte ARRM =  4; // N; mutable x‿y‿z / ⟨x⋄y⋄z)
  public static final byte FN1C =  5; // monadic call
  public static final byte FN2C =  6; // dyadic call
  public static final byte OP1D =  7; // derive modifier
  public static final byte OP2D =  8; // derive composition
  public static final byte TR2D =  9; // derive 2-train aka atop
  public static final byte TR3D = 10; // derive 3-train aka fork
  public static final byte SETN = 11; // set new; _  ←_;
  public static final byte SETU = 12; // set upd; _  ↩_;
  public static final byte SETM = 13; // set mod; _ F↩_;
  public static final byte POPS = 14; // pop object from stack
  public static final byte DFND = 15; // N; derive dfn with current scope; {𝕩}; {𝔽}; {𝔽𝔾}
  public static final byte FN1O = 16; // optional monadic call
  public static final byte FN2O = 17; // optional dyadic call
  public static final byte CHKV = 18; // error if ToS is ·
  public static final byte TR3O = 19; // derive 3-train aka fork, with optional 𝕨
  public static final byte OP2H = 20; // derive composition to modifier
  public static final byte LOCO = 21; // B,N; push local object
  public static final byte LOCM = 22; // B,N; push mutable local object
  public static final byte RETN = 25; // returns, giving ToS
  // public static final byte ____ = 6;
  
  public static final byte SPEC = 30; // special
  public static final byte   EVAL = 0; // ⍎
  public static final byte   STDIN = 1; // •
  public static final byte   STDOUT = 2; // •←
  
  
  static class Stk {
    private Obj[] vals = new Obj[4];
    private int sz = 0;
    void push(Obj o) {
      if (sz>=vals.length) vals = Arrays.copyOf(vals, vals.length<<1);
      vals[sz++] = o;
    }
    Obj pop() {
      Obj val = vals[--sz];
      vals[sz] = null;
      return val;
    }
    Obj peek() {
      return vals[sz-1];
    }
  }
  
  public static final boolean DBGPROG = true;
  public Value exec(Scope sc) {
    return exec(sc, 0);
  }
  public Value exec(Scope sc, int spt) {
    if (gen!=null) return gen.get(sc, spt);
    if (iter++>=compileStart && compileStart>=0) {
      gen = new JComp(this).r;
      return gen.get(sc, spt);
    }
    Value last = null;
    int pi = spt;
    try {
    int i = spt;
    Stk s = new Stk();
    // Stack<Obj> s = new Stack<>();
    exec: while (i != bc.length) {
      pi = i;
      i++;
      switch (bc[pi]) {
        case PUSH: {
          int n=0,h=0,b; do { b = bc[i]; n|= (b&0x7f)<<h; h+=7; i++; } while (b<0);
          s.push(objs[n]);
          break;
        }
        case VARO: {
          int n=0,h=0,b; do { b = bc[i]; n|= (b&0x7f)<<h; h+=7; i++; } while (b<0);
          Value got = sc.getC(strs[n]);
          s.push(got);
          break;
        }
        case VARM: {
          int n=0,h=0,b; do { b = bc[i]; n|= (b&0x7f)<<h; h+=7; i++; } while (b<0);
          s.push(new Variable(strs[n]));
          break;
        }
        case LOCO: {
          int depth = bc[i++];
          int n=0,h=0,b; do { b = bc[i]; n|= (b&0x7f)<<h; h+=7; i++; } while (b<0);
          Value got = sc.getL(depth, n);
          s.push(got);
          break;
        }
        case LOCM: {
          int depth = bc[i++];
          int n=0,h=0,b; do { b = bc[i]; n|= (b&0x7f)<<h; h+=7; i++; } while (b<0);
          s.push(new Local(depth, n));
          break;
        }
        case ARRO: {
          int n=0,h=0,b; do { b = bc[i]; n|= (b&0x7f)<<h; h+=7; i++; } while (b<0);
          Value[] vs = new Value[n];
          for (int j = 0; j < n; j++) {
            vs[n-j-1] = (Value) s.pop();
          }
          s.push(Arr.create(vs));
          break;
        }
        case ARRM: {
          int n=0,h=0,b; do { b = bc[i]; n|= (b&0x7f)<<h; h+=7; i++; } while (b<0);
          Settable[] vs = new Settable[n];
          for (int j = 0; j < n; j++) {
            vs[n-j-1] = (Settable) s.pop();
          }
          s.push(new SettableArr(vs));
          break;
        }
        case FN1C: {
          Value f = (Value) s.pop();
          Value x = (Value) s.pop();
          if (DBGPROG) { Main.faulty = f; last = f; }
          s.push(f.asFun().call(x));
          break;
        }
        case FN2C: {
          Value w = (Value) s.pop();
          Value f = (Value) s.pop();
          Value x = (Value) s.pop();
          if (DBGPROG) { Main.faulty = f; last = f; }
          s.push(f.asFun().call(w, x));
          break;
        }
        case FN1O: {
          Value f = (Value) s.pop();
          Value x = (Value) s.pop();
          if (DBGPROG) { Main.faulty = f; last = f; }
          if (x instanceof Nothing) s.push(x);
          else s.push(f.asFun().call(x));
          break;
        }
        case FN2O: {
          Value w = (Value) s.pop();
          Value f = (Value) s.pop();
          Value x = (Value) s.pop();
          if (DBGPROG) { Main.faulty = f; last = f; }
          if (x instanceof Nothing) s.push(x);
          else if (w instanceof Nothing) s.push(f.asFun().call(x));
          else s.push(f.asFun().call(w, x));
          break;
        }
        case OP1D: {
          Value f = (Value) s.pop();
          Mop   r = (Mop  ) s.pop(); // +TODO (+↓ & ↓↓) don't cast to Mop/Dop for stuff like F←+ ⋄ 1_f
          if (DBGPROG) { Main.faulty = f; last = f; }
          Fun d = r.derive(f); d.token = r.token;
          s.push(d);
          break;
        }
        case OP2D: {
          Value f = (Value) s.pop();
          Dop   r = (Dop  ) s.pop();
          Value g = (Value) s.pop();
          if (DBGPROG) { Main.faulty = r; last = r; }
          Fun d = r.derive(f, g); d.token = r.token;
          s.push(d);
          break;
        }
        case OP2H: {
          Dop   r = (Dop  ) s.pop();
          Value g = (Value) s.pop();
          if (DBGPROG) { Main.faulty = r; last = r; }
          Mop d = r.derive(g); d.token = r.token;
          s.push(d);
          break;
        }
        case TR2D: {
          Value f = (Value) s.pop();
          Value g = (Value) s.pop();
          Atop d = new Atop(f.asFun(), g.asFun()); d.token = f.token;
          s.push(d);
          break;
        }
        case TR3D: {
          Value f = (Value) s.pop();
          Value g = (Value) s.pop();
          Value h = (Value) s.pop();
          Fork d = new Fork(f, g.asFun(), h.asFun()); d.token = f.token;
          s.push(d);
          break;
        }
        case TR3O: {
          Value f = (Value) s.pop();
          Value g = (Value) s.pop();
          Value h = (Value) s.pop();
          Obj d = f instanceof Nothing? new Atop(g.asFun(), h.asFun()) : new Fork(f, g.asFun(), h.asFun()); d.token = f.token;
          s.push(d);
          break;
        }
        case SETN: {
          Settable k = (Settable) s.pop();
          Value    v = (Value   ) s.pop();
          k.set(v, false, sc, null);
          s.push(v);
          break;
        }
        case SETU: {
          Settable k = (Settable) s.pop();
          Value    v = (Value   ) s.pop();
          k.set(v, true, sc, null);
          s.push(v);
          break;
        }
        case SETM: {
          Settable k = (Settable) s.pop();
          Value    f = (Value   ) s.pop();
          Value    v = (Value   ) s.pop();
          k.set(f.asFun().call(k.get(sc), v), true, sc, null);
          s.push(v);
          break;
        }
        case POPS: {
          s.pop();
          break;
        }
        case DFND: {
          int n=0,h=0; byte b; do { b = bc[i]; n|= (b&0x7f)<<h; h+=7; i++; } while (b<0);
          DfnTok dfn = dfns[n];
          s.push(dfn.eval(sc));
          break;
        }
        case CHKV: {
          Obj v = s.peek();
          if (v instanceof Nothing) throw new SyntaxError("didn't expect · here", v);
          break;
        }
        case RETN: {
          break exec;
        }
        case SPEC: {
          switch(bc[i++]) {
            case EVAL:
              s.push(new EvalBuiltin(sc));
              break;
            case STDOUT:
              s.push(new Quad());
              break;
            case STDIN:
              s.push(new Quad().get(sc));
              break;
            default:
              throw new InternalError("Unknown special "+bc[i-1]);
          }
          break;
        }
        default: throw new InternalError("Unknown bytecode "+bc[pi]);
      }
    }
    return (Value) s.peek();
    } catch (Throwable t) {
      APLError e = t instanceof APLError? (APLError) t : new ImplementationError(t);
      ArrayList<APLError.Mg> mgs = new ArrayList<>();
      APLError.Mg.add(mgs, tk, '¯');
      if (last != null) {
        if (last.token == null) {
          if (last instanceof DerivedDop) last = ((DerivedDop) last).op;
          else if (last instanceof DerivedMop) last = ((DerivedMop) last).op;
        }
        Tokenable tk = last.getToken();
        if (tk == null) {
          int j  = pi;
          // while (--j > 0 && tk==null) {
          //   if (ref[j] != null) tk = ref[j];
          // }
          tk = ref[j];
        }
        // if (tk == null) {
        //   tk = new Token("INS "+bc[pi], 0, 1) {
        //     public String toRepr() {
        //       return source();
        //     }
        //   };
        // }
        APLError.Mg.add(mgs, tk, '^');
      }
      e.trace.add(new APLError.Frame(sc, mgs));
      throw e;
    }
  }
  
  public String fmt() {
    StringBuilder b = new StringBuilder("code:\n");
    int i = 0;
    try {
      while (i != bc.length) {
        int pi = i;
        i++;
        String cs;
        switch (bc[pi]) {
          case PUSH: cs = " PUSH " + safeObj(l7dec(bc, i)); i = l7end(bc, i); break;
          case VARO: cs = " VARO " + safeStr(l7dec(bc, i)); i = l7end(bc, i); break;
          case VARM: cs = " VARM " + safeStr(l7dec(bc, i)); i = l7end(bc, i); break;
          case DFND: cs = " DFND " +         l7dec(bc, i) ; i = l7end(bc, i); break;
          case ARRO: cs = " ARRO " +         l7dec(bc, i) ; i = l7end(bc, i); break;
          case ARRM: cs = " ARRM " +         l7dec(bc, i) ; i = l7end(bc, i); break;
          case FN1C: cs = " FN1C"; break;
          case FN2C: cs = " FN2C"; break;
          case OP1D: cs = " OP1D"; break;
          case OP2D: cs = " OP2D"; break;
          case TR2D: cs = " TR2D"; break;
          case TR3D: cs = " TR3D"; break;
          case SETN: cs = " SETN"; break;
          case SETU: cs = " SETU"; break;
          case SETM: cs = " SETM"; break;
          case POPS: cs = " POPS"; break;
          case FN1O: cs = " FN1O"; break;
          case FN2O: cs = " FN2O"; break;
          case CHKV: cs = " CHKV"; break;
          case TR3O: cs = " TR3O"; break;
          case OP2H: cs = " OP2H"; break;
          case LOCO: cs = " LOCO " + (bc[i++]&0xff) + " " + l7dec(bc, i); i = l7end(bc, i); break;
          case LOCM: cs = " LOCM " + (bc[i++]&0xff) + " " + l7dec(bc, i); i = l7end(bc, i); break;
          case RETN: cs = " RETN"; break;
          case SPEC: cs = " SPEC " + (bc[i++]&0xff); break;
          default  : cs = " unknown";
        }
        b.append(' ');
        for (int j = pi; j < i; j++) {
          int c = bc[j]&0xff;
          b.append(Integer.toHexString(c/16).toUpperCase());
          b.append(Integer.toHexString(c%16).toUpperCase());
          b.append(' ');
        }
        b.append(Main.repeat("   ", 3 - (i-pi))); // relies on this not erroring if the padding would need to be negative
        b.append(cs);
        b.append('\n');
      }
    } catch (Throwable t) {
      b.append("#ERR#\n");
    }
    if (objs.length > 0) {
      b.append("objs:\n");
      for (int j = 0; j < objs.length; j++) b.append(' ').append(j).append(": ").append(objs[j].oneliner()).append('\n');
    }
    if (strs.length > 0) {
      b.append("strs:\n");
      for (int j = 0; j < strs.length; j++) b.append(' ').append(j).append(": ").append(strs[j]).append('\n');
    }
    if (dfns.length > 0) {
      b.append("dfns:\n");
      for (int j = 0; j < dfns.length; j++) {
        DfnTok dfn = dfns[j];
        b.append(' ').append(j).append(": ").append(dfn.type=='f'? "function" : dfn.type=='d'? "2-modifier" : dfn.type=='m'? "1-modifier" : dfn.type=='a'? "immediate block" : String.valueOf(dfn.type)).append(" \n");
        b.append("  flags: ").append(dfn.flags).append('\n');
        Body b0 = dfn.bodies.get(0);
        if (dfn.bodies.size()>1 || b0.start!=0 || !b0.noHeader || b0.vars.length!=0) {
          ArrayList<Body> bodies = dfn.bodies;
          for (int k = 0; k < bodies.size(); k++) { // TODO move this around so it can also show for the top-level function
            Body bd = bodies.get(k);
            b.append("  body ").append(k).append(": ").append(bd.immediate? "immediate" : bd.ftype=='m'? "monadic" : bd.ftype=='d'? "dyadic" : "ambivalent").append('\n');
            b.append("    start: ").append(bd.start).append('\n');
            if (bd.self!=null) b.append("    self: ").append(bd.self).append('\n');
            if (bd.wM!=null) b.append("    𝕨: ").append(bd.wM.toRepr()).append('\n');
            if (bd.xM!=null) b.append("    𝕩: ").append(bd.xM.toRepr()).append('\n');
            if (bd.fM!=null) b.append("    𝕗: ").append(bd.fM.toRepr()).append('\n');
            if (bd.gM!=null) b.append("    𝕘: ").append(bd.gM.toRepr()).append('\n');
            if (bd.vars.length!=0) b.append("    vars: ").append(Arrays.toString(bd.vars)).append('\n');
          }
        }
        if (dfn.comp != this) {
          b.append("  ");
          b.append(dfn.comp.fmt().replace("\n", "\n  "));
          b.append('\n');
        }
      }
    }
    b.deleteCharAt(b.length()-1);
    return b.toString();
  }
  
  
  public int next(int i) {
    switch (bc[i]) {
      case PUSH: case DFND:
      case VARO: case VARM:
      case ARRO: case ARRM:
        return l7end(bc, i+1);
      case FN1C: case FN2C: case FN1O: case FN2O:
      case OP1D: case OP2D: case OP2H:
      case TR2D: case TR3D: case TR3O:
      case SETN: case SETU: case SETM:
      case POPS: case CHKV: case RETN:
        return i+1;
      case SPEC: return i+2;
      case LOCO: case LOCM:
        return l7end(bc, i+2);
      default  : return -1;
    }
  }
  
  
  private int l7dec(byte[] bc, int i) {
    int n=0, h=0;
    while (true) {
      if (i >= bc.length) return -1;
      n|= (bc[i]&0x7f)<<h;
      h+=7;
      if (bc[i]>=0) return n;
      i++;
    }
  }
  private int l7end(byte[] bc, int i) { // returns first index after end
    while (i<bc.length && bc[i]<0) i++;
    return i+1;
  }
  
  private String safeObj(int l) {
    if (l>=objs.length) return "INVALID";
    return "!"+objs[l].oneliner();
  }
  private String safeStr(int l) {
    if (l>=strs.length) return "INVALID";
    return "\""+strs[l]+"\"";
  }
  
  
  /* types:
    a - array
    A - array or ·
    f - function
    d - dop
    m - mop
    
    ← - new var
    ↩ - upd var
    
    _ - empty
    
   */
  
  
  
  public static class Mut {
    ArrayList<Value> objs = new ArrayList<>();
    ArrayList<DfnTok> dfns = new ArrayList<>();
    ArrayList<String> strs = new ArrayList<>();
    MutByteArr bc = new MutByteArr(10);
    ArrayList<Token> ref = new ArrayList<>();
    ArrayList<DfnTok> registered = new ArrayList<>();
  
    HashMap<String, Integer> vars; // map of varName→index
    ArrayList<String> varnames;
    public void newBody(String[] preset) {
      varnames = new ArrayList<>();
      Collections.addAll(varnames, preset);
      vars = new HashMap<>();
      for (int i = 0; i < preset.length; i++) vars.put(preset[i], i);
    }
    public String[] getVars() {
      return varnames.toArray(new String[0]);
    }
  
    public void addNum(int n) {
      leb128(bc, n, ref);
    }
    
    public void push(Value o) {
      add(o.token, PUSH);
      addNum(objs.size());
      objs.add(o);
    }
    
    public void push(DfnTok o) {
      add(o, DFND);
      addNum(dfns.size());
      dfns.add(o);
    }
    
    public void var(Token t, String s, boolean mut) {
      Integer pos = vars.get(s);
      if (pos == null) {
        add(t, mut? VARM : VARO);
        addNum(strs.size());
        strs.add(s);
      } else {
        add(t, mut? LOCM : LOCO);
        add((byte) 0);
        addNum(pos);
      }
    }
    
    public void add(byte... nbc) {
      for (byte b : nbc) {
        bc.u(b); ref.add(null);
      }
    }
    public void add(Token tk, byte... nbc) {
      for (byte b : nbc) {
        bc.u(b); ref.add(tk);
      }
    }
    
    public static void leb128(MutByteArr ba, int n, ArrayList<Token> ref) {
      do {
        byte b = (byte) (n&0x7f);
        n>>= 7;
        if (n!=0) b|= 0x80;
        ba.s(b);
        ref.add(null);
      } while (n != 0);
    }
  
    public Comp finish(Token tk) {
      assert bc.len == ref.size() : bc.len +" "+ ref.size();
      
      Comp comp = new Comp(bc.get(), objs.toArray(new Value[0]), strs.toArray(new String[0]), dfns.toArray(new DfnTok[0]), ref.toArray(new Token[0]), tk);
      for (DfnTok c : registered) c.comp = comp;
      return comp;
    }
  
    public void register(DfnTok dfn) {
      registered.add(dfn);
    }
  }
  
  
  
  public static Comp comp(TokArr<LineTok> lns, Scope sc) {
    Mut mut = new Mut();
    mut.newBody(sc.varNames);
    int sz = lns.tokens.size();
    for (int i = 0; i < sz; i++) {
      LineTok ln = lns.tokens.get(i); typeof(ln); flags(ln);
      compO(mut, ln);
      if (i!=sz-1) mut.add(POPS);
    }
    sc.varNames = mut.getVars();
    sc.varAm = sc.varNames.length;
    if (sc.vars.length < sc.varAm) sc.vars = Arrays.copyOf(sc.vars, sc.varAm);
    sc.removeMap();
    return mut.finish(lns);
  }
  
  public static int[] comp(Mut mut, ArrayList<Body> parts) {
    int[] offs = new int[parts.size()];
    for (int i = 0; i < parts.size(); i++) {
      Body b = parts.get(i);
      offs[i] = mut.bc.len;
      mut.newBody(b.defNames());
      
      int sz = b.lns.size();
      for (int j = 0; j < sz; j++) {
        LineTok ln = b.lns.get(j); typeof(ln); flags(ln);
        typeof(ln);
        compO(mut, ln);
        if (j!=sz-1) mut.add(POPS);
      }
      b.vars = mut.getVars();
      if (i!=parts.size()-1) mut.add(RETN); // +TODO insert CHKV if return could be a nothing
    }
    return offs;
  }
  
  private static boolean isE(LinkedList<Res> tps, String pt, boolean last) { // O=[aAf] in non-!, A ≡ a
    if (tps.size() > 4) return false;
    int pi = pt.length()-1;
    int ti = tps.size()-1;
    boolean qex = false;
    while (pi>=0) {
      // System.out.println(pt+" "+pi+" "+ti);
      char c = pt.charAt(pi--);
      if (c=='|') {
        if (last) qex = true;
      } else {
        if (ti==-1) return qex;
        char t = tps.get(ti--).type;
        if (t=='A') t = 'a';
        if (c=='!') {
          if (pt.charAt(pi) == ']') {
            do { pi--;
              if (t == pt.charAt(pi)) return false;
            } while(pt.charAt(pi) != '['); pi--;
          } else {
            if (t == pt.charAt(pi--)) return false;
          }
        } else if (c=='O') {
          if (t!='f' && t!='a') return false;
        } else if (c == ']') {
          boolean any = false;
          do { pi--;
            if (t == pt.charAt(pi)) any = true;
          } while(pt.charAt(pi) != '['); pi--;
          if (!any) return false;
        } else {
          if (t != c) return false;
        }
      }
    }
    return true;
  }
  private static boolean isS(LinkedList<Res> tps, String pt, int off) { // O=[aAf] in non-!
    int pi = 0;
    int ti = off;
    int tsz = tps.size();
    while (pi<pt.length()) {
      char c = pt.charAt(pi++);
      if (c != '|') {
        if (ti==tsz) return false;
        char t = tps.get(ti++).type;
        if (c=='O') {
          if (t!='f' && t!='a' && t!='A') return false;
        } else {
          if (c == '!') {
            if (t == pt.charAt(pi++)) return false;
          } else {
            if (t != c) return false;
          }
        }
      }
    }
    return true;
  }
  
  static abstract class Res {
    char type;
    Value c;
    public Res(char type) {
      this.type = type;
    }
    
    abstract void add(Mut m);
    Res mut(boolean create) { throw new Error(getClass()+" cannot be mutated"); }
    
    public abstract Token lastTok();
  }
  
  static class ResTk extends Res {
    Token tk;
    private boolean mut;
    private boolean create;
    
    public ResTk(Token tk) {
      super(tk.type);
      this.tk = tk;
      this.c = (tk.flags&1)!=0? constFold(tk) : null;
      type = tk.type;
    }
    
    void add(Mut m) {
      if (mut) compM(m, tk, create);
      else compO(m, tk);
    }
    
    Res mut(boolean create) {
      assert !mut;
      mut = true;
      this.create = create;
      return this;
    }
    
    public Token lastTok() {
      return tk;
    }
    
    public String toString() {
      return tk==null? type+"" : tk.source();
    }
  }
  static class ResBC extends Res {
    private final byte[] bc;
    private final Token tk;
    
    public ResBC(byte... bc) {
      super('\0');
      this.bc = bc;
      this.tk = null;
    }
    public ResBC(Token tk, byte... bc) {
      super('\0');
      this.bc = bc;
      this.tk = tk;
    }
    
    void add(Mut m) {
      m.add(tk, bc);
    }
    
    public Token lastTok() {
      return null;
    }
    
    public String toString() {
      return Arrays.toString(bc);
    }
  }
  static class ResMix extends Res {
    private final Res[] all;
    
    public ResMix(char type, Res... all) {
      super(type);
      this.all = all;
    }
    
    void add(Mut m) {
      for (Res r : all) r.add(m);
    }
    
    public Token lastTok() {
      for (int i = all.length-1; i >= 0; i--) {
        Res r = all[i];
        Token tk = r.lastTok();
        if (tk != null) return tk;
      }
      return null;
    }
    
    public String toString() {
      return Arrays.toString(all);
    }
  }
  static class ResCf extends Res {
    private final Token last;
  
    public ResCf(char type, Value val, Token last) {
      super(type);
      this.c = val;
      this.last = last;
    }
  
    void add(Mut m) {
      m.push(c);
    }
  
    public Token lastTok() {
      return last;
    }
  }
  
  public static byte[] cat(byte[][] bcs) {
    int am = 0;
    for (byte[] bc : bcs) am+= bc.length;
    byte[] bc = new byte[am];
    for (int i=0, j=0; i < bcs.length; i++) {
      System.arraycopy(bcs[i], 0, bc, j, bcs[i].length);
      j+= bcs[i].length;
    }
    return bc;
  }
  
  private static final byte[] NOBYTES = new byte[0];
  private static final byte[] CHKVBC = new byte[]{CHKV};
  
  
  private static void printlvl(String s) {
    System.out.println(Main.repeat(" ", Main.printlvl*2) + s);
  }
  public static void collect(LinkedList<Res> tps, boolean train, boolean last) {
    while (true) {
      if (Main.debug) printlvl(tps.toString());
      if (tps.size() <= 1) break;
      if (train) { // trains only
        if (isE(tps, "d!|Off", last)) {
          if (Main.debug) printlvl("match F F F");
          Res h = tps.removeLast();
          Res g = tps.removeLast();
          Res f = tps.removeLast();
          if (h.c!=null && g.c!=null && f.c!=null) {
            if (f.c instanceof Nothing) tps.addLast(new ResCf('f', new Atop(g.c.asFun(), h.c.asFun()), h.lastTok()));
            else tps.addLast(new ResCf('f', new Fork(f.c, g.c.asFun(), h.c.asFun()), h.lastTok()));
          } else {
            tps.addLast(new ResMix('f', h, g, f, new ResBC(f.type=='A'? TR3O : TR3D) ));
          }
          continue;
        }
        if (isE(tps, "[←↩]|ff", last)) {
          if (Main.debug) printlvl("match F F");
          Res h = tps.removeLast();
          Res g = tps.removeLast();
          if (h.c!=null && g.c!=null) tps.addLast(new ResCf('f', new Atop(g.c.asFun(), h.c.asFun()), h.lastTok()));
          else tps.addLast(new ResMix('f', h, g, new ResBC(TR2D)));
          continue;
        }
      } else { // value expressions
        if (isE(tps, "d!|afa", last)) {
          if (Main.debug) printlvl("match a F a");
          Res x = tps.removeLast();
          Res f = tps.removeLast();
          Res w = tps.removeLast();
          tps.addLast(new ResMix(x.type,
            x, f, w,
            new ResBC(f.lastTok(), x.type=='A' | w.type=='A'? FN2O : FN2C)
          ));
          continue;
        }
        if (isE(tps, "[da]!|fa", last)) {
          if (Main.debug) printlvl("match F a");
          Res x = tps.removeLast();
          Res f = tps.removeLast();
          tps.addLast(new ResMix(x.type,
            x, f,
            new ResBC(f.lastTok(), x.type=='A'? FN1O : FN1C)
          ));
          continue;
        }
      }
      // all
      
      int i = last? 0 : 1; // hopefully this doesn't need to be looping
      if (tps.get(0).type!='d') {
        if (isS(tps, "Om", i)) {
          if (Main.debug) printlvl("match O m");
          Res c=tps.remove(i+1);
          Res f=tps.remove(i  );
          if (c.c!=null && f.c!=null) {
            tps.add(i, new ResCf('f', ((Mop) c.c).derive(f.c), c.lastTok()));
          } else tps.add(i, new ResMix('f', c, f,
            new ResBC(f.lastTok(), f.type=='A'? CHKVBC : NOBYTES),
            new ResBC(c.lastTok(), OP1D)
          ));
          continue;
        }
        if (isS(tps, "OdO", i)) {
          if (Main.debug) printlvl("match O d O "+i);
          Res g=tps.remove(i+2);
          Res c=tps.remove(i+1);
          Res f=tps.remove(i  );
          if (g.c!=null && c.c!=null && f.c!=null) {
            if (g.c instanceof Nothing || f.c instanceof Nothing) throw new SyntaxError("didn't expect · here", g.c instanceof Nothing? g.lastTok() : f.lastTok() );
            tps.add(i, new ResCf('f', ((Dop) c.c).derive(f.c, g.c), f.lastTok()));
          } else tps.add(i, new ResMix('f',
            g, new ResBC(g.lastTok(), g.type=='A'? CHKVBC : NOBYTES),
            c,
            f, new ResBC(f.lastTok(), f.type=='A'? CHKVBC : NOBYTES),
            new ResBC(c.lastTok(), OP2D)
          ));
          continue;
        }
      }
      if (isS(tps, "dO", i)) {
        char t0 = tps.get(0).type;
        if (i==0 || t0!='a' && t0!='A' && t0!='f') {
          if (Main.debug) printlvl("match dO");
          Res f;
          tps.add(i, new ResMix('m',
            (f=tps.remove(i+1)),
            new ResBC(f.type=='A'? CHKVBC : NOBYTES),
            (  tps.remove(i  )),
            new ResBC(OP2H)
          ));
          continue;
        }
      }
      
      if (isE(tps, "af↩a", false)) {
        if (Main.debug) printlvl("af↩a");
        tps.addLast(new ResMix('a',
          tps.removeLast(),
          tps.removeLast(),
          tps.removeLast(), // empty
          tps.removeLast().mut(false),
          new ResBC(SETM)
        ));
        continue;
      }
      set: if (tps.size() >= (last? 3 : 4)) {
        char a = tps.get(tps.size()-2).type;
        if (a=='←' || a=='↩') {
          char k = tps.get(tps.size()-3).type;
          char v = tps.get(tps.size()-1).type;
          char p = tps.size()>=4? tps.get(tps.size()-4).type : 0;
          if (p=='d') break set;
          char ov = v;
          if (v=='A') v = 'a';
          if (k=='A') k = 'a'; // 𝕨↩ is a possibility
          if (k==v) {
            if (Main.debug) printlvl(k+" "+a+" "+v);
            tps.addLast(new ResMix(ov, // result is not v because typeof is stupid; +TODO
              tps.removeLast(),
              new ResBC(ov=='A'? CHKVBC : NOBYTES),
              tps.removeLast(), // empty
              tps.removeLast().mut(a=='←'),
              new ResBC(a=='←'? SETN : SETU)
            ));
            continue;
          } else if (v!='a' && k!='f') throw new SyntaxError(a+": Cannot assign with different types", ((ResTk) tps.get(tps.size() - 2)).tk);
        }
      }
      break;
    }
  }
  
  public static char typeof(Token t) {
    if (t.type != 0) return t.type; // handles NumTok, StrTok, ChrTok, SetTok, ModTok, NameTok & re-evaluations
    
    if (t instanceof ParenTok) {
      return t.type = typeof(((ParenTok) t).ln);
    } else if (t instanceof StrandTok) {
      for (Token c : ((StrandTok) t).tokens) typeof(c);
      return t.type = 'a';
    } else if (t instanceof ArrayTok) {
      for (Token c : ((ArrayTok) t).tokens) typeof(c);
      return t.type = 'a';
    } else if (t instanceof OpTok) {
      OpTok op = (OpTok) t;
      Value b = builtin(op);
      if (b==null) {
        String s = op.op;
        switch (s) {
          case "𝕨":
            return t.type = 'A';
          case "𝕘": case "𝕗": case "𝕩": case "𝕤": case "𝕣": case "•":
            return t.type = 'a';
          case "𝔾": case "𝔽": case "𝕏": case "𝕎": case "𝕊": case "ℝ": case "⍎":
            return t.type = 'f';
          default: throw new ImplementationError("Undefined unknown built-in "+s, op);
        }
      } else {
        return t.type = b instanceof Fun? 'f' : b instanceof Mop? 'm' : b instanceof Dop? 'd' : 'a';
      }
    } else if (t instanceof LineTok) {
      List<Token> tks = ((LineTok) t).tokens;
      char[] tps = new char[tks.size()];
      if (tps.length == 0) throw new SyntaxError("line with no tokens", t);
      for (int i = 0; i < tks.size(); i++) tps[i] = typeof(tks.get(i));
      char last = tps[tps.length-1];
      if (tps.length == 1) return t.type = last;
      char prev = tps[tps.length-2];
      
      // i hope these guesses are correct..
      if (prev == 'd') { // ends with d[fa], so equivalent to (last == 'm') below
        for (int i = 0; i < tps.length-2; i++) { // must not touch the last [fa] though (and while at it, d neither) 
          char tp = tps[i];
          if (tp=='a' || tp=='A' || tp=='f') return t.type = 'f';
        }
        return t.type = 'm';
      } else {
        if (last == 'd') return t.type = 'd'; // (_d_←{𝔽𝕘}) should be the only case (+ more variable assignment)
        if (last=='a' || last=='A') return t.type = last; // not as arg of dop/mop
        if (last == 'f') return t.type = 'f';
        
        if (last == 'm') { // complicated because (_a←_b←_c) vs (⊢+ ⊢+ +˜)
          for (char tp : tps) {
            if (tp=='a' || tp=='A' || tp=='f') return t.type = 'f';
          }
          return t.type = 'm';
        }
      }
    } else if (t instanceof BasicLines) {
      List<LineTok> ts = ((BasicLines) t).tokens;
      for (Token c : ts) typeof(c);
      return t.type = ts.get(ts.size()-1).type;
    }
    throw new ImplementationError("can't get type of "+t.getClass().getCanonicalName(), t);
  }
  
  public static byte flags(Token t) {
    if (t.flags != -1) return t.flags;
    if (t instanceof ConstTok || t instanceof NothingTok) return t.flags = 7;
    if (t instanceof ModTok || t instanceof SetTok || t instanceof NameTok) return t.flags = 6;
    
    if (t instanceof ParenTok) return t.flags = flags(((ParenTok) t).ln);
    if (t instanceof TokArr<?>) {
      List<? extends Token> ts = ((TokArr<?>) t).tokens;
      if (t instanceof ArrayTok || t instanceof StrandTok 
      ||  t instanceof LineTok && ts.size()==1) {
        t.flags = 7;
      } else t.flags = 6;
      for (Token c : ts) {
        if (c instanceof DfnTok) t.flags&= ~2;
        t.flags&= flags(c);
      }
      return t.flags;
    }
    if (t instanceof OpTok) {
      if (((OpTok) t).op.equals("⍎")) return t.flags = 0;
      if (builtin((OpTok) t)==null) return t.flags = 6;
      return t.flags = 7;
    }
    throw new ImplementationError("didn't check for "+t.getClass().getSimpleName());
  }
  
  public static void compM(Mut m, Token tk, boolean create) {
    assert tk.type != 0;
    if (tk instanceof NameTok) {
      String name = ((NameTok) tk).name;
      if (create) {
        if (m.vars.containsKey(name)) throw Local.redefine(name, tk);
        m.vars.put(name, m.varnames.size());
        m.varnames.add(name);
      }
      m.var(tk, name, true);
      return;
    }
    if (tk instanceof StrandTok) {
      List<Token> tks = ((StrandTok) tk).tokens;
      for (Token c : tks) compM(m, c, create);
      m.add(tk, ARRM); m.addNum(tks.size());
      return;
    }
    if (tk instanceof ArrayTok) {
      List<LineTok> tks = ((ArrayTok) tk).tokens;
      for (LineTok c : tks) compM(m, c, create);
      m.add(tk, ARRM); m.addNum(tks.size());
      return;
    }
    if (tk instanceof ParenTok) {
      compM(m, ((ParenTok) tk).ln, create);
      return;
    }
    if (tk instanceof LineTok) {
      if (((LineTok) tk).tokens.size() == 1) {
        compM(m, ((LineTok) tk).tokens.get(0), create);
        return;
      }
    }
    if (tk instanceof OpTok) {
      String op = ((OpTok) tk).op;
      if (op.equals("•")) {
        m.add(tk, SPEC, STDOUT);
        return;
      }
      int aid = Tokenizer.surrogateOps.indexOf(op);
      if (aid != -1) {
        aid = aid/4*4;
        m.var(tk, Tokenizer.surrogateOps.substring(aid, aid+2), true);
        return;
      }
    }
    throw new SyntaxError(tk.toRepr()+" cannot be mutated", tk);
  }
  
  public static void compO(Mut m, Token tk) { // assumes tk has been typechecked
    if ((tk.flags&1)!=0) { m.push(constFold(tk)); return; } // ConstTok, NothingTok
    if (tk instanceof ParenTok) {
      compO(m, ((ParenTok) tk).ln);
      return;
    }
    if (tk instanceof LineTok) {
      List<Token> ts = ((LineTok) tk).tokens;
      if (ts.size() == 0) return;
      if (ts.size() == 1) { compO(m, ts.get(0)); return; }
      int i = ts.size()-1;
      
      LinkedList<Res> tps = new LinkedList<>();
      Res t0 = new ResTk(ts.get(i));
      tps.addFirst(t0);
      i--;
      final boolean train = (t0.type!='a' && t0.type!='A')  ||  (ts.size()>=2 && ts.get(i).type=='d');
      
      
      if (Main.debug) {
        printlvl("parsing "+tk.source());
        Main.printlvl++;
      }
      
      while (i>=0) {
        Res c = new ResTk(ts.get(i));
        tps.addFirst(c);
        collect(tps, train, false);
        i--;
      }
      collect(tps, train, true);
      if (Main.debug) Main.printlvl--;
      
      if (tps.size()!=1) {
        Token t = null;
        for (int i1 = tps.size()-1; i1 >= 0; i1--) {
          Res tp = tps.get(i1);
          if (tp.lastTok() != null) {
            t = tp.lastTok();
            break;
          }
        }
        throw new SyntaxError("couldn't join everything to a single expression", t);
      }
      assert tps.get(0).type == tk.type : tps.get(0).type + "≠" + tk.type;
      tps.get(0).add(m);
      return;
    }
    if (tk instanceof OpTok) {
      OpTok op = (OpTok) tk;
      Value b = builtin(op);
      if (b != null) {
        b.token = tk;
        m.push(b);
        return;
      }
      
      String s = op.op;
      switch (s) {
        case "𝕨": case "𝕘": case "𝕗": case "𝕩": case "𝕤": case "𝕣":
          m.var(tk, s, false);
          return;
        case "𝕎": case "𝔾": case "𝔽": case "𝕏": case "𝕊": case "ℝ":
          m.var(tk, new String(new char[]{55349, (char) (s.charAt(1)+26)}), false); // lowercase
          return;
        case "⍎": m.add(op, SPEC, EVAL ); return;
        case "•": m.add(op, SPEC, STDIN); return;
        default: throw new ImplementationError("Undefined unknown built-in "+s, op);
      }
    }
    if (tk instanceof NameTok) {
      m.var(tk, ((NameTok) tk).name, false);
      return;
    }
    if (tk instanceof StrandTok) { // +TODO (+↓) check for type A
      if (Main.debug) { printlvl("parsing "+tk.source()); Main.printlvl++; }
      List<Token> tks = ((StrandTok) tk).tokens;
      for (Token c : tks) compO(m, c);
      if (Main.debug) Main.printlvl--;
      
      m.add(tk, ARRO); m.addNum(tks.size());
      return;
    }
    if (tk instanceof ArrayTok) {
      if (Main.debug) { printlvl("parsing "+tk.source()); Main.printlvl++; }
      List<LineTok> tks = ((ArrayTok) tk).tokens;
      for (LineTok c : tks) compO(m, c);
      if (Main.debug) Main.printlvl--;
      
      m.add(tk, ARRO); m.addNum(tks.size());
      return;
    }
    if (tk instanceof SetTok || tk instanceof ModTok) {
      return;
    }
    if (tk instanceof DfnTok) {
      m.push((DfnTok) tk);
      return;
    }
    throw new ImplementationError("can't compile "+tk.getClass());
  }
  
  public static Value constFold(Token t) {
    assert (t.flags&1)!=0 && t.flags!=-1;
    if (t instanceof ConstTok) return ((ConstTok) t).val;
    if (t instanceof ParenTok) return constFold(((ParenTok) t).ln);
    if (t instanceof LineTok) {
      List<Token> ts = ((LineTok) t).tokens;
      assert ts.size() == 1;
      return constFold(ts.get(0));
    }
    if (t instanceof StrandTok) {
      List<Token> ts = ((StrandTok) t).tokens;
      Value[] ps = new Value[ts.size()];
      for (int i = 0; i < ps.length; i++) ps[i] = constFold(ts.get(i));
      return Arr.create(ps);
    }
    if (t instanceof ArrayTok) {
      List<LineTok> ts = ((ArrayTok) t).tokens;
      Value[] ps = new Value[ts.size()];
      for (int i = 0; i < ps.length; i++) ps[i] = constFold(ts.get(i));
      return Arr.create(ps);
    }
    if (t instanceof OpTok) {
      Value builtin = builtin((OpTok) t);
      if (builtin == null) throw new ImplementationError(t.source());
      return builtin;
    }
    if (t instanceof NothingTok) return ((NothingTok) t).val;
    throw new ImplementationError("couldn't constant fold "+t.getClass().getSimpleName());
  }
  
  public static Value builtin(OpTok t) {
    switch (t.op.charAt(0)) {
      // fns
      // case '⍲': return new NandBuiltin(sc);
      // case '⍱': return new NorBuiltin(sc);
      // case '⊥': return new UTackBuiltin();
      // case '⊤': return new DTackBuiltin();
      // case '!': return new ExclBuiltin();
      
      // case '?': return new RandBuiltin(sc);
      // case '⍪': return new CommaBarBuiltin();
      
      // case '…': return new EllipsisBuiltin();
      // case '⍮': return new SemiUBBuiltin();
      // case '⍧': return new LShoeStileBuiltin();
      // case '%': return new MergeBuiltin();
  
  
  
  
      case '⍕': return new FormatBuiltin();
      case '⌽': return new ReverseBuiltin();
      case '+': return new PlusBuiltin();
      case '-': return new MinusBuiltin();
      case '×': return new MulBuiltin();
      case '÷': return new DivBuiltin();
      case '⋆':
      case '*': return new StarBuiltin();
      case '|': return new StileBuiltin();
      case '∧': return new AndBuiltin();
      case '∨': return new OrBuiltin();
      case '⌈': return new CeilingBuiltin();
      case '⌊': return new FloorBuiltin();
      case '√': return new RootBuiltin();
      case '¬': return new NotBuiltin();
      
      
      case '⊢': return new RTackBuiltin();
      case '⊣': return new LTackBuiltin();
      
      case '⥊': return new ShapeBuiltin();
      case '↑': return new UpArrowBuiltin();
      case '↓': return new DownArrowBuiltin();
      case '∾': return new JoinBuiltin();
      case '≍': return new LaminateBuiltin();
      case '⍉': return new TransposeBuiltin();
      
      case '/': return new SlashBuiltin();
      case '⊏': return new LBoxBuiltin();
      case '⊔': return new GroupBuiltin();
      case '⊑': return new LBoxUBBuiltin();
      case '⊐': return new RBoxBuiltin();
      case '⊒': return new RBoxUBBuiltin();
      case '↕': return new UDBuiltin();
      case '∊': return new EpsBuiltin();
      case '⍷': return new FindBuiltin();
      case '⍋': return new GradeUpBuiltin();
      case '⍒': return new GradeDownBuiltin();
      case '≢': return new TallyBuiltin();
      case '≡': return new MatchBuiltin();
      
      
      
      // comparisons
      case '<': return new LTBuiltin();
      case '≤': return new LEBuiltin();
      case '=': return new EQBuiltin();
      case '≥': return new GEBuiltin();
      case '>': return new GTBuiltin();
      case '≠': return new NEBuiltin();
      
      // mops
      case '´': return new FoldBuiltin();
      case '˝': return new InsertBuiltin();
      case '`': return new ScanBuiltin();
      case '¨': return new EachBuiltin();
      case '˜': return new SelfieBuiltin();
      case '⌜': return new TableBuiltin();
      case '⁼': return new InvBuiltin();
      case '˘': return new CellBuiltin();
      // case '⍩':
      // case 'ᐵ': return new EachLeft();
      // case 'ᑈ': return new EachRight();
      
      // dops
      // case '.': return new DotBuiltin();
      // case '⍡': return new CRepeatBuiltin(sc);
      case '○': return new OverBuiltin();
      case '∘': return new AtopBuiltin();
      case '⊸': return new BeforeBuiltin();
      case '⟜': return new AfterBuiltin();
      case '⌾': return new UnderBuiltin();
      case '⍟': return new RepeatBuiltin();
      case '⚇': return new DepthBuiltin();
      case '⊘': return new AmbivalentBuiltin();
      case '◶': return new CondBuiltin();
      case '⎉': return new NCellBuiltin();
      case '⎊': return new CatchBuiltin();
      
      
      // case '@': return new AtBuiltin(sc);
      // case '⍬': return new DoubleArr(DoubleArr.EMPTY);
  
  
      case '⍎': case '•': case 'ℝ': // the lone double-struck
      case 55349: // double-struck surrogate pair
        return null;
  
      default: throw new ImplementationError("no built-in " + t.op + " defined in exec", t);
    }
  }
}