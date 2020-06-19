package APL;

import APL.errors.*;
import APL.tokenizer.Token;
import APL.tokenizer.types.*;
import APL.types.*;
import APL.types.functions.*;
import APL.types.functions.builtins.fns.EvalBuiltin;
import APL.types.functions.builtins.fns2.*;
import APL.types.functions.builtins.mops.ReduceBuiltin;
import APL.types.functions.trains.*;

import java.util.*;

public class Comp extends Obj {
  private static final byte[] NOBYTES = new byte[0];
  private final byte[] mutbc;
  private final Value[] objs;
  private final String[] strs;
  
  Comp(byte[] bc, Value[] objs, String[] strs) {
    this.mutbc = bc;
    this.objs = objs;
    this.strs = strs;
  }
  
  public static final byte PUSH = 0; // 1O; 2
  public static final byte VARO = 1; // 1S; x/𝕨/𝕏
  public static final byte ARRO = 2; // 1B; 1‿2‿3 / ⟨1⋄2⋄3⟩; compilers job to extend past 255 (or maybe another op?)
  public static final byte FN1C = 3; // monadic call
  public static final byte FN2C = 4; // dyadic call
  public static final byte OP1D = 5; // derive modifier
  public static final byte OP2D = 6; // derive composition
  public static final byte TR2D = 7; // derive 2-train aka atop
  public static final byte TR3D = 8; // derive 3-train aka fork
  // public static final byte ____ = 6;
  
  public static final byte SPEC = -1; // special
  public static final byte   EVBT = 0; // ⍎ needs special attention
  
  static class Stk {
    Obj[] vals = new Obj[4];
    int sz = 0;
    void push(Obj o) {
      if (sz==vals.length) dbl();
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
  
    private void dbl() {
      vals = Arrays.copyOf(vals, vals.length<<1);
    }
  }
  
  public Value exec(Scope sc) {
    Stk s = new Stk();
    int i = 0;
    while (i!= mutbc.length) {
      int pi = i;
      i++;
      switch (mutbc[pi]) {
        case PUSH:
          s.push(objs[mutbc[i++]&0xff]);
          break;
        case VARO:
          Value got = sc.get(strs[mutbc[i++] & 0xff]);
          if (got == null) throw new ValueError("Unknown variable "+strs[mutbc[i-1] & 0xff]);
          s.push(got);
          break;
        case FN1C: {
          Value f = (Value) s.pop();
          Value w = (Value) s.pop();
          s.push(f.asFun().call(w));
          break;
        }
        case FN2C: {
          Value a = (Value) s.pop();
          Value f = (Value) s.pop();
          Value w = (Value) s.pop();
          s.push(f.asFun().call(a, w));
          break;
        }
        case ARRO: {
          int am = mutbc[i++]&0xff;
          Value[] vs = new Value[am];
          for (int j = 0; j < am; j++) {
            vs[am-j-1] = (Value) s.pop(); // +TODO better alg
          }
          s.push(Arr.create(vs));
          break;
        }
        case OP1D: {
          Value f = (Value) s.pop();
          Mop   o = (Mop  ) s.pop();
          DerivedMop d = o.derive(f); d.token = o.token;
          s.push(d);
          break;
        }
        case OP2D: {
          Value f = (Value) s.pop();
          Dop   o = (Dop  ) s.pop();
          Value g = (Value) s.pop();
          DerivedDop d = o.derive(f, g); d.token = o.token;
          s.push(d);
          break;
        }
        case TR2D: {
          Value f = (Value) s.pop();
          Value g = (Value) s.pop();
          Atop d = new Atop(f, g.asFun()); d.token = f.token;
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
        case SPEC: {
          switch(mutbc[i++]) {
            case EVBT:
              s.push(new EvalBuiltin(sc));
              break;
            default:
              throw new InternalError("Unknown special "+ mutbc[i-1]);
          }
          break;
        }
        default: throw new InternalError("Unknown bytecode "+ mutbc[pi]);
      }
    }
    return Main.san(s.peek()); // +todo just cast?
  }
  
  public String fmt() {
    StringBuilder b = new StringBuilder("code:\n");
    int i = 0;
    try {
      while (i != mutbc.length) {
        int pi = i;
        i++;
        switch (mutbc[pi]) {
          case PUSH: b.append(" PUSH ").append(safeObj(mutbc[i++])); break;
          case VARO: b.append(" VARO ").append(safeStr(mutbc[i++])); break;
          case ARRO: b.append(" ARRO ").append(mutbc[i++]&0xff); break;
          case FN1C: b.append(" FN1C"); break;
          case FN2C: b.append(" FN2C"); break;
          case OP1D: b.append(" OP1D"); break;
          case OP2D: b.append(" OP2D"); break;
          case TR2D: b.append(" TR2D"); break;
          case TR3D: b.append(" TR3D"); break;
          case SPEC: b.append(" SPEC ").append(mutbc[i++]&0xff); break;
          default  : b.append(" unknown ").append(mutbc[i++]&0xff);
        }
        b.append('\n');
      }
    } catch (Throwable t) {
      b.append("#ERR#");
    }
    if (objs.length > 0) {
      b.append("objs:\n");
      for (int j = 0; j < objs.length; j++) b.append(' ').append(j).append(": ").append(objs[j]).append('\n');
    }
    if (strs.length > 0) {
      b.append("strs:\n");
      for (int j = 0; j < strs.length; j++) b.append(' ').append(j).append(": ").append(strs[j]).append('\n');
    }
    b.deleteCharAt(b.length()-1);
    return b.toString();
  }
  
  private String safeObj(byte i) {
    int l = i&0xff;
    if (l>=objs.length) return i+": INVALID";
    return i+": "+objs[l];
  }
  private String safeStr(byte i) {
    int l = i&0xff;
    if (l>=strs.length) return i+": INVALID";
    return i+": "+strs[l];
  }
  
  
  /* types:
    a - array +TODO possibly-· type
    f - function
    d - dop
    m - mop
    
    ← - new var
    ↩ - upd var
    
    _ - empty
    
   */
  
  static class Mut {
    ArrayList<Value> objs = new ArrayList<>();
    ArrayList<String> strs = new ArrayList<>();
    
    public byte[] pushVar(String s) {
      byte[] res = {PUSH, (byte) strs.size()};
      strs.add(s);
      return res;
    }
  
    public byte[] push(Value o) {
      byte[] res = {PUSH, (byte) objs.size()};
      objs.add(o);
      return res;
    }
  }
  
  public static Comp comp(LineTok ln) {
    // return example();
    typeof(ln);
    Mut mut = new Mut();
    byte[] bc = compP(mut, ln, false);
    return new Comp(bc, mut.objs.toArray(new Value[0]), mut.strs.toArray(new String[0]));
  }
  
  public static Comp comp(BasicLines lns) {
    Mut mut = new Mut();
    byte[][] bcs = new byte[lns.tokens.size()][];
    for (int i = 0; i < lns.tokens.size(); i++) {
      LineTok ln = lns.tokens.get(i);
      typeof(ln);
      bcs[i] = compP(mut, ln, false);
    }
    return new Comp(cat(bcs), mut.objs.toArray(new Value[0]), mut.strs.toArray(new String[0]));
  }
  
  private static boolean isE(LinkedList<Res> tps, String pt, boolean last) { // [] only for !; O=[af] in non-!
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
        } else {
          if (t != c) return false;
        }
      }
    }
    return true;
  }
  private static boolean isS(LinkedList<Res> tps, String pt, int off) { // O=[af] in non-!
    int pi = 0;
    int ti = off;
    int tsz = tps.size();
    while (pi<pt.length()) {
      char c = pt.charAt(pi++);
      if (c != '|') {
        if (ti==tsz) return false;
        char t = tps.get(ti++).type;
        if (c=='O') {
          if (t!='f' && t!='a') return false;
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
  
  static class Res {
    char type;
    Token tk;
    byte[] bc;
  
    public Res(Token tk) {
      this.tk = tk;
      type = tk.type;
    }
    
    public Res(char type, byte[] bc) {
      this.type = type;
      this.bc = bc;
    }
    public Res(char type, byte[]... bcs) {
      this.type = type;
      this.bc = cat(bcs);
    }
    
    byte[] comp(Mut m, boolean mut) {
      if (bc != null) return bc;
      return compP(m, tk, mut);
    }
  
    public String toString() {
      return type+"";
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
  
  public static boolean DBGCOMP = false;
  public static void collect(LinkedList<Res> tps, Mut m, boolean train, boolean last) {
    while (tps.size() > 1) {
      if (DBGCOMP) System.out.println(tps);
      if (train) { // trains only
        if (isE(tps, "d!|Off", last)) {
          if (DBGCOMP) System.out.println("match F F F");
          tps.addLast(new Res('f',
            tps.removeLast().comp(m, false),
            tps.removeLast().comp(m, false),
            tps.removeLast().comp(m, false),
            new byte[]{TR3D}
          ));
          continue;
        }
        if (isE(tps, "←|ff", last)) {
          if (DBGCOMP) System.out.println("match F F");
          tps.addLast(new Res('f',
            tps.removeLast().comp(m, false),
            tps.removeLast().comp(m, false),
            new byte[]{TR2D}
          ));
          continue;
        }
      } else { // regular expressions only
        if (isE(tps, "d!|afa", last)) {
          if (DBGCOMP) System.out.println("match a F a");
          tps.addLast(new Res('a',
            tps.removeLast().comp(m, false),
            tps.removeLast().comp(m, false),
            tps.removeLast().comp(m, false),
            new byte[]{FN2C}
          ));
          continue;
        }
        if (isE(tps, "[da]!|fa", last)) {
          if (DBGCOMP) System.out.println("match F a");
          tps.addLast(new Res('a',
            tps.removeLast().comp(m, false),
            tps.removeLast().comp(m, false),
            new byte[]{FN1C}
          ));
          continue;
        }
      }
      { // all
        int i = tps.get(0).type=='d'? 1 : last?0 : 1;
        if (isS(tps, "Om", i)) {
          if (DBGCOMP) System.out.println("O m");
          tps.add(i, new Res('f',
            tps.remove(i+1).comp(m, false),
            tps.remove(i  ).comp(m, false),
            new byte[]{OP1D}
          ));
          continue;
        }
        if (isS(tps, "OdO", i)) {
          if (DBGCOMP) System.out.println("O d O");
          tps.add(i, new Res('f',
            tps.remove(i+2).comp(m, false),
            tps.remove(i+1).comp(m, false),
            tps.remove(i  ).comp(m, false),
            new byte[]{OP2D}
          ));
          continue;
        }
      }
      break;
    }
  }
  
  public static char typeof(Token t) {
    if (t.type != 0) return t.type; // handles NumTok, StrTok, ChrTok & re-evaluations
    
    if (t instanceof ParenTok) {
      return t.type = typeof(((ParenTok) t).ln);
    } else if (t instanceof StrandTok) {
      for (Token c : ((StrandTok) t).tokens) typeof(c);
      return t.type = 'a';
    } else if (t instanceof NameTok) {
      return t.type = varType(((NameTok) t).name);
    } else if (t instanceof OpTok) {
      OpTok op = (OpTok) t;
      Value b = Exec.builtin(op, null);
      if (b==null) {
        String s = op.op;
        switch (s) {
          case "𝕨": case "𝕘": case "𝕗": case "𝕩":
            return t.type = 'a';
          case "𝕎": case "𝔾": case "𝔽": case "𝕏":
          case "⍎":
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
      if (last == 'a') return t.type = 'a';
      if (last == 'f') return t.type = 'f';
      if (last == 'd') { // i hope this and the 'm' case are correct..
        if (tps.length==1) return t.type = 'd';
        throw new SyntaxError("dop can't be the last token of a line", tks.get(tks.size()-1));
      }
      if (last == 'm') return t.type = tps.length==1? 'm' : 'f';
    }
    throw new ImplementationError("can't get type of "+t.getClass().getCanonicalName());
  }
  
  
  @interface CheckReturnValue{}@CheckReturnValue
  public static byte[] compP(Mut m, Token tk, boolean mut) { // assumes tk has been typechecked
    assert tk.type != 0;
    if (tk instanceof ParenTok) {
      return compP(m, ((ParenTok) tk).ln, mut);
    }
    if (tk instanceof NumTok) {
      return m.push(((NumTok) tk).val);
    }
    if (tk instanceof ChrTok) {
      return m.push(((ChrTok) tk).val);
    }
    if (tk instanceof StrTok) {
      return m.push(((StrTok) tk).val);
    }
    if (tk instanceof LineTok) {
      List<Token> ts = ((LineTok) tk).tokens;
      if (ts.size() == 0) return NOBYTES;
      if (ts.size() == 1) { return compP(m, ts.get(0), mut); }
      int i = ts.size()-1;
  
      LinkedList<Res> tps = new LinkedList<>();
      Res t0 = new Res(ts.get(i));
      tps.addFirst(t0);
      final boolean train = t0.type=='f';
      i--;
      
      
      while (i>=0) {
        Res c = new Res(ts.get(i));
        tps.addFirst(c);
        // if (DBGCOMP) System.out.println(tps);
        collect(tps, m, train, i==0);
        i--;
      }
      // if (DBGCOMP) System.out.println(tps);
      if (tps.size()!=1) throw new SyntaxError("couldn't join everything to a single expression");
      assert tps.get(0).type == tk.type;
      return tps.get(0).comp(m, false);
    }
    if (tk instanceof OpTok) {
      OpTok op = (OpTok) tk;
      Value b = Exec.builtin(op, null);
      if (b != null) return m.push(b);
      
      String s = op.op;
      switch (s) {
        case "𝕨": case "𝕘": case "𝕗": case "𝕩":
        case "𝕎": case "𝔾": case "𝔽": case "𝕏":
          return m.pushVar(s);
        case "⍎": // +TODO handle better
          return new byte[]{SPEC, EVBT};
        default: throw new ImplementationError("Undefined unknown built-in "+s, op);
      }
    }
    if (tk instanceof NameTok) {
      String name = ((NameTok) tk).name;
      return m.pushVar(name);
    }
    if (tk instanceof StrandTok) {
      List<Token> tks = ((StrandTok) tk).tokens;
      byte[][] bs = new byte[tks.size()+1][];
      for (int i = 0; i < tks.size(); i++) {
        bs[i] = compP(m, tks.get(i), mut);
      }
      int sz = tks.size();
      if (sz > 255) throw new NYIError("array constants with >255 items", tk);
      bs[bs.length-1] = new byte[]{ARRO, (byte) sz};
      return cat(bs);
    }
    throw new ImplementationError("can't compile "+tk.getClass());
  }
  
  public static char varType(String name) {
    char s = name.charAt(0);
    if (s=='_') {
      char e = name.charAt(name.charAt(name.length()-1));
      return e=='_'? 'd' : 'm';
    }
    if (s>='A' && s<='Z') return 'f';
    return 'a';
  }
  
  private static Comp example() {
    Value[] os = new Value[4];
    os[0] = new Num(5);
    os[1] = new DivBuiltin();
    os[2] = new PlusBuiltin();
    os[3] = new ReduceBuiltin();
    byte[] bcs = {
      PUSH, 0, // 5
      PUSH, 1, // ÷ 5
      FN1C,    // .2
      PUSH, 0, // .2 5
      PUSH, 0, // .2 5 5
      ARRO, 3, // ⟨.2,5,5⟩
  
      PUSH, 3, // ⟨.2,5,5⟩ /
      PUSH, 2, // ⟨.2,5,5⟩ / +
      OP1D,    // ⟨.2,5,5⟩ +/
      FN1C,    // +/⟨.2,5,5⟩ ≡ 10.2
  
      PUSH, 2,
      ARRO, 2,
    };
    return new Comp(bcs, os, new String[0]);
  }
  
  
  public Type type() { // +TODO?
    return Type.cmp;
  }
}