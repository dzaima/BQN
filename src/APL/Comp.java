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
  private final byte[] bc;
  private final Value[] objs;
  private final String[] strs;
  
  Comp(byte[] bc, Value[] objs, String[] strs) {
    this.bc = bc;
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
  
  
  public Value exec(Scope sc) {
    Stack<Obj> s = new Stack<>();
    int i = 0;
    while (i!=bc.length) {
      int pi = i;
      i++;
      switch (bc[pi]) {
        case PUSH:
          s.push(objs[bc[i++]&0xff]);
          break;
        case VARO:
          Value got = sc.get(strs[bc[i++] & 0xff]);
          if (got == null) throw new ValueError("Unknown variable "+strs[bc[i-1] & 0xff]);
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
          int am = bc[i++]&0xff;
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
          switch(bc[i++]) {
            case EVBT:
              s.push(new EvalBuiltin(sc));
              break;
            default:
              throw new InternalError("Unknown special "+bc[i-1]);
          }
          break;
        }
        default: throw new InternalError("Unknown bytecode "+bc[pi]);
      }
    }
    return Main.san(s.peek()); // +todo just cast?
  }
  
  public String fmt() {
    StringBuilder b = new StringBuilder("code:\n");
    int i = 0;
    while (i != bc.length) {
      int pi = i;
      i++;
      switch (bc[pi]) {
        case PUSH: b.append(" PUSH ").append(safeObj(bc[i++])); break;
        case VARO: b.append(" VARO ").append(safeStr(bc[i++])); break;
        case ARRO: b.append(" ARRO ").append(bc[i++]&0xff); break;
        case FN1C: b.append(" FN1C"); break;
        case FN2C: b.append(" FN2C"); break;
        case OP1D: b.append(" OP1D"); break;
        case OP2D: b.append(" OP2D"); break;
        case TR2D: b.append(" TR2D"); break;
        case TR3D: b.append(" TR3D"); break;
        case SPEC: b.append(" SPEC ").append(bc[i++]&0xff); break;
        default  : b.append(" unknown ").append(bc[i++]&0xff);
      }
      b.append('\n');
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
    a - array
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
  
    private void push(Res r, Value o) {
      r.bc.add(PUSH);
      r.bc.add((byte) this.objs.size());
      this.objs.add(o);
    }
    private void pushVar(Res r, String s) {
      r.bc.add(VARO);
      r.bc.add((byte) this.strs.size());
      this.strs.add(s);
    }
  }
  
  public static Comp comp(LineTok ln) {
    // return example();
    Mut mut = new Mut();
    Res r = compP(mut, ln);
    byte[] bcr = new byte[r.bc.size()];
    for (int i = 0; i < r.bc.size(); i++) bcr[i] = r.bc.get(i);
    return new Comp(bcr, mut.objs.toArray(new Value[0]), mut.strs.toArray(new String[0]));
  }
  
  public static Comp comp(BasicLines lns) {
    Mut mut = new Mut();
    ArrayList<Byte> bc = new ArrayList<>();
    for (LineTok ln : lns.tokens) {
      bc.addAll(compP(mut, ln).bc);
    }
    byte[] bcr = new byte[bc.size()];
    for (int i = 0; i < bc.size(); i++) bcr[i] = bc.get(i);
    return new Comp(bcr, mut.objs.toArray(new Value[0]), mut.strs.toArray(new String[0]));
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
        char t = tps.get(ti--).t;
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
        char t = tps.get(ti++).t;
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
  
  public static boolean DBGCOMP = false;
  public static void collect(LinkedList<Res> tps, boolean train, boolean last) {
    while (true) {
      if (train) {
        if (isE(tps, "d!|Off", last)) {
          if (DBGCOMP) System.out.println("match F F F");
          Res r = new Res('f');
          r.add(tps.removeLast());
          r.add(tps.removeLast());
          r.add(tps.removeLast());
          r.add(TR3D);
          tps.addLast(r);
          continue;
        }
        if (isE(tps, "←|ff", last)) {
          if (DBGCOMP) System.out.println("match F F");
          Res r = new Res('f');
          r.add(tps.removeLast());
          r.add(tps.removeLast());
          r.add(TR2D);
          tps.addLast(r);
          continue;
        }
      } else {
        if (isE(tps, "d!|afa", last)) {
          if (DBGCOMP) System.out.println("match a F a");
          Res r = new Res('a');
          r.add(tps.removeLast());
          r.add(tps.removeLast());
          r.add(tps.removeLast());
          r.add(FN2C);
          tps.addLast(r);
          continue;
        }
        if (isE(tps, "[da]!|fa", last)) {
          if (DBGCOMP) System.out.println("match F a");
          Res r = new Res('a');
          r.add(tps.removeLast());
          r.add(tps.removeLast());
          r.add(FN1C);
          tps.addLast(r);
          continue;
        }
      }
      {
        int i = tps.get(0).t=='d'? 1 : last?0 : 1;
        if (isS(tps, "Om", i)) {
          if (DBGCOMP) System.out.println("O m");
          Res r = new Res('f');
          r.add(tps.remove(i+1));
          r.add(tps.remove(i));
          r.add(OP1D);
          tps.add(i, r);
          continue;
        }
        if (isS(tps, "OdO", i)) {
          if (DBGCOMP) System.out.println("O d O");
          Res r = new Res('f');
          r.add(tps.remove(i+2));
          r.add(tps.remove(i+1));
          r.add(tps.remove(i));
          r.add(OP2D);
          tps.add(i, r);
          continue;
        }
      }
      break;
    }
  }
  
  private static class Res {
    private final char t;
    private final ArrayList<Byte> bc;
    public Res(char t, ArrayList<Byte> bs) {
      this.t = t;
      this.bc = bs;
    }
  
    public Res(char t) {
      this.t = t;
      bc = new ArrayList<>();
    }
  
    public void add(Res r) {
      bc.addAll(r.bc);
    }
    public void add(byte b) {
      bc.add(b);
    }
  
    public String toString() {
      return t+""+bc;
    }
  }
  
  private static final ArrayList<Byte> EMPTY = new ArrayList<>();
  
  
  // @interface CheckReturnValue{}@CheckReturnValue
  public static Res compP(Mut m, Token tk) {
    if (tk instanceof ParenTok) {
      return compP(m, ((ParenTok) tk).ln);
    }
    if (tk instanceof NumTok) {
      Res r = new Res('a');
      m.push(r, ((NumTok) tk).num);
      return r;
    }
    if (tk instanceof ChrTok) {
      Res r = new Res('a');
      m.push(r, ((ChrTok) tk).val);
      return r;
    }
    if (tk instanceof StrTok) {
      Res r = new Res('a');
      m.push(r, ((StrTok) tk).val);
      return r;
    }
    if (tk instanceof LineTok) {
      List<Token> ts = ((LineTok) tk).tokens;
      if (ts.size() == 0) return new Res('_', EMPTY);
      int i = ts.size()-1;
  
      LinkedList<Res> tps = new LinkedList<>();
      Res c0 = compP(m, ts.get(i--));
      tps.addFirst(c0);
      final boolean train = c0.t =='f';
      
      while (i>=0) {
        Res c = compP(m, ts.get(i));
        tps.addFirst(c);
        if (DBGCOMP) System.out.println(tps);
        collect(tps, train, i==0);
        i--;
      }
      if (DBGCOMP) System.out.println(tps);
      if (tps.size()!=1) throw new SyntaxError("couldn't join everything to a single expression");
      return tps.get(0);
    }
    if (tk instanceof OpTok) {
      OpTok op = (OpTok) tk;
      Value b = Exec.builtin(op, null);
      Res r;
      if (b==null) {
        String s = op.op;
        switch (s) {
          case "𝕨": case "𝕘": case "𝕗": case "𝕩":
            r = new Res('a');
            m.pushVar(r, s);
            break;
          case "𝕎": case "𝔾": case "𝔽": case "𝕏":
            r = new Res('f');
            m.pushVar(r, s);
            break;
          case "⍎": // +TODO handle better
            r = new Res('f');
            r.bc.add(SPEC);
            r.bc.add(EVBT);
            break;
          default: throw new ImplementationError("Undefined unknown built-in "+s, op);
        }
      } else {
        r = new Res(b instanceof Fun? 'f' : b instanceof Mop? 'm' : b instanceof Dop? 'd' : 'a');
        b.token = op;
        m.push(r, b);
      }
      return r;
    }
    if (tk instanceof NameTok) {
      String name = ((NameTok) tk).name;
      Res r = new Res(varType(name));
      m.pushVar(r, name);
      return r;
    }
    if (tk instanceof StrandTok) {
      List<Token> tks = ((StrandTok) tk).tokens;
      Res r = new Res('a');
      for (Token c : tks) r.add(compP(m, c));
      r.bc.add(ARRO);
      int sz = tks.size();
      if (sz > 255) throw new NYIError("array constants with >255 items", tk);
      r.bc.add((byte) sz);
      return r;
    }
    
    throw new ImplementationError("can't compile "+tk.getClass().getCanonicalName());
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