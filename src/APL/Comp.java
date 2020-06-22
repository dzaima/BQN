package APL;

import APL.errors.*;
import APL.tokenizer.Token;
import APL.tokenizer.types.*;
import APL.types.*;
import APL.types.functions.*;
import APL.types.functions.builtins.Quad;
import APL.types.functions.builtins.fns2.EvalBuiltin;
import APL.types.functions.trains.*;
import APL.types.functions.userDefined.UserDefined;

import java.util.*;

public class Comp {
  public final byte[] bc;
  private final Value[] objs;
  private final String[] strs;
  private final DfnTok[] dfns;
  private final Token tk;
  
  Comp(byte[] bc, Value[] objs, String[] strs, DfnTok[] dfns, Token tk) {
    this.bc = bc;
    this.objs = objs;
    this.strs = strs;
    this.dfns = dfns;
    this.tk = tk;
  }
  
  public static final byte PUSH =  0; // 1B; 2
  public static final byte VARO =  1; // 1B; x/𝕨/𝕏
  public static final byte VARM =  2; // 1B; mutable x/𝕨/𝕏
  public static final byte ARRO =  3; // 1B; 1‿2‿3 / ⟨1⋄2⋄3⟩; compilers job to extend past 255 (or maybe another op?)
  public static final byte ARRM =  4; // 1B; mutable x‿y‿z / ⟨x⋄y⋄z)
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
  public static final byte DFND = 15; // 1B; derive dfn with current scope; {𝕩}; {𝔽}; {𝔽𝔾}
  public static final byte FN1O = 16; // optional monadic call
  public static final byte FN2O = 17; // optional dyadic call
  public static final byte CHKV = 18; // error if ToS is ·
  public static final byte TR3O = 19; // derive 3-train aka fork, with optional 𝕨
  public static final byte OP2H = 20; // derive composition to modifier
  public static final byte DFND2= 21; // 2B; derive dfn with current scope; {𝕩}; {𝔽}; {𝔽𝔾}
  public static final byte VARO2= 22; // 2B; x/𝕨/𝕏
  public static final byte VARM2= 23; // 2B; mutable x/𝕨/𝕏
// public static final byte ____ = 6;
  
  public static final byte SPEC = -1; // special
  public static final byte   EVAL = 0; // ⍎
  public static final byte   STDIN = 1; // •
  public static final byte   STDOUT = 2; // •←
  
  
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
  
  public static final boolean DBGPROG = true;
  public Value exec(Scope sc) {
  
    Value last = null;
    try {
    Stk s = new Stk();
    int i = 0;
    while (i!= bc.length) {
      int pi = i;
      i++;
      switch (bc[pi]) {
        case PUSH: {
          s.push(objs[bc[i++] & 0xff]);
          break;
        }
        case VARO: {
          Value got = sc.get(strs[bc[i++] & 0xff]);
          if (got == null) throw new ValueError("Unknown variable \"" + strs[bc[i - 1] & 0xff] + "\"");
          s.push(got);
          break;
        }
        case VARO2: {
          Value got = sc.get(strs[((bc[i++] & 0xff)<<8) | (bc[i++] & 0xff)]);
          if (got == null) throw new ValueError("Unknown variable \"" + strs[bc[i - 1] & 0xff] + "\"");
          s.push(got);
          break;
        }
        case VARM: {
          s.push(new Variable(sc, strs[bc[i++] & 0xff]));
          break;
        }
        case VARM2: {
          s.push(new Variable(sc, strs[((bc[i++] & 0xff)<<8) | (bc[i++] & 0xff)]));
          break;
        }
        case ARRO: {
          int am = bc[i++]&0xff;
          Value[] vs = new Value[am];
          for (int j = 0; j < am; j++) {
            vs[am-j-1] = (Value) s.pop();
          }
          s.push(Arr.create(vs));
          break;
        }
        case ARRM: {
          int am = bc[i++]&0xff;
          Settable[] vs = new Settable[am];
          for (int j = 0; j < am; j++) {
            vs[am-j-1] = (Settable) s.pop();
          }
          s.push(new SettableArr(vs));
          break;
        }
        case FN1C: {
          Value f = (Value) s.pop();
          Value w = (Value) s.pop();
          if (DBGPROG) { Main.faulty = f; last = f; }
          s.push(f.asFun().call(w));
          break;
        }
        case FN2C: {
          Value a = (Value) s.pop();
          Value f = (Value) s.pop();
          Value w = (Value) s.pop();
          if (DBGPROG) { Main.faulty = f; last = f; }
          s.push(f.asFun().call(a, w));
          break;
        }
        case FN1O: {
          Value f = (Value) s.pop();
          Value w = (Value) s.pop();
          if (DBGPROG) { Main.faulty = f; last = f; }
          if (w instanceof Nothing) s.push(w);
          else s.push(f.asFun().call(w));
          break;
        }
        case FN2O: {
          Value a = (Value) s.pop();
          Value f = (Value) s.pop();
          Value w = (Value) s.pop();
          if (DBGPROG) { Main.faulty = f; last = f; }
          if (w instanceof Nothing) s.push(w);
          else if (a instanceof Nothing) s.push(f.asFun().call(w));
          else s.push(f.asFun().call(a, w));
          break;
        }
        case OP1D: {
          Value f = (Value) s.pop();
          Mop   o = (Mop  ) s.pop(); // +TODO (+↓ & ↓↓) don't cast to Mop/Dop for stuff like F←+ ⋄ 1_f
          if (DBGPROG) { Main.faulty = f; last = f; }
          Fun d = o.derive(f); d.token = o.token;
          s.push(d);
          break;
        }
        case OP2D: {
          Value f = (Value) s.pop();
          Dop   o = (Dop  ) s.pop();
          Value g = (Value) s.pop();
          if (DBGPROG) { Main.faulty = o; last = o; }
          Fun d = o.derive(f, g); d.token = o.token;
          s.push(d);
          break;
        }
        case OP2H: {
          Dop   o = (Dop  ) s.pop();
          Value g = (Value) s.pop();
          if (DBGPROG) { Main.faulty = o; last = o; }
          Mop d = o.derive(g); d.token = o.token;
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
        case TR3O: {
          Value f = (Value) s.pop();
          Value g = (Value) s.pop();
          Value h = (Value) s.pop();
          Obj d = f instanceof Nothing? new Atop(g, h.asFun()) : new Fork(f, g.asFun(), h.asFun()); d.token = f.token;
          s.push(d);
          break;
        }
        case SETN: {
          Settable k = (Settable) s.pop();
          Value    v = (Value   ) s.pop();
          k.set(v, false, null);
          s.push(v);
          break;
        }
        case SETU: {
          Settable k = (Settable) s.pop();
          Value    v = (Value   ) s.pop();
          k.set(v, true, null);
          s.push(v);
          break;
        }
        case SETM: {
          Settable k = (Settable) s.pop();
          Value    f = (Value   ) s.pop();
          Value    v = (Value   ) s.pop();
          k.set(f.asFun().call(k.get(), v), true, null);
          s.push(v);
          break;
        }
        case POPS: {
          s.pop();
          break;
        }
        case DFND: {
          DfnTok dfn = dfns[bc[i++] & 0xff];
          s.push(UserDefined.of(dfn, sc));
          break;
        }
        case DFND2: {
          DfnTok dfn = dfns[((bc[i++] & 0xff)<<8) | (bc[i++] & 0xff)];
          s.push(UserDefined.of(dfn, sc));
          break;
        }
        case CHKV: {
          Obj v = s.peek();
          if (v instanceof Nothing) throw new SyntaxError("Didn't expect · here", v);
          break;
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
              s.push(new Quad().get());
              break;
            default:
              throw new InternalError("Unknown special "+ bc[i-1]);
          }
          break;
        }
        default: throw new InternalError("Unknown bytecode "+ bc[pi]);
      }
    }
    assert s.peek() instanceof Value;
    return Main.san(s.peek()); // +todo just cast?
    } catch (APLError e) {
      ArrayList<APLError.Mg> mgs = new ArrayList<>();
      APLError.Mg.add(mgs, tk, '¯');
      if (last != null) {
        if (last.token == null) {
          if (last instanceof DerivedDop) last = ((DerivedDop) last).op;
          else if (last instanceof DerivedMop) last = ((DerivedMop) last).op;
        }
        APLError.Mg.add(mgs, last, '^');
      }
      e.trace.add(mgs);
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
          case PUSH: cs = " PUSH " + safeObj(bc[i++]); break;
          case VARO: cs = " VARO " + safeStr(bc[i++]); break;
          case VARM: cs = " VARM " + safeStr(bc[i++]); break;
          case ARRO: cs = " ARRO " + (bc[i++]&0xff); break;
          case ARRM: cs = " ARRM " + (bc[i++]&0xff); break;
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
          case DFND: cs = " DFND " + (bc[i++]&0xff); break;
          case FN1O: cs = " FN1O"; break;
          case FN2O: cs = " FN2O"; break;
          case CHKV: cs = " CHKV"; break;
          case TR3O: cs = " TR3O"; break;
          case OP2H: cs = " OP2H"; break;
          case DFND2:cs = " DFND2 " + ((bc[i++]&0xff)*256 + (bc[i++]&0xff)); break;
          case VARO2:cs = " VARO2 " + ((bc[i++]&0xff)*256 + (bc[i++]&0xff)); break;
          
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
        b.append(Main.repeat("   ", 2 - (i-pi)));
        b.append(cs);
        b.append('\n');
      }
    } catch (Throwable t) {
      b.append("#ERR#\n");
    }
    if (objs.length > 0) {
      b.append("objs:\n");
      for (int j = 0; j < objs.length; j++) b.append(' ').append(j).append(": ").append(objs[j]).append('\n');
    }
    if (strs.length > 0) {
      b.append("strs:\n");
      for (int j = 0; j < strs.length; j++) b.append(' ').append(j).append(": ").append(strs[j]).append('\n');
    }
    if (dfns.length > 0) {
      b.append("dfns:\n");
      for (int j = 0; j < dfns.length; j++) {
        DfnTok dfn = dfns[j];
        b.append(' ').append(j).append(":\n  type ").append(dfn.type).append(" \n  ");
        b.append(dfn.comp.fmt().replace("\n", "\n  "));
        b.append('\n');
      }
    }
    b.deleteCharAt(b.length()-1);
    return b.toString();
  }
  
  private String safeObj(byte i) {
    int l = i&0xff;
    if (l>=objs.length) return "INVALID";
    return "!"+objs[l];
  }
  private String safeDfn(byte i) {
    int l = i&0xff;
    if (l>=dfns.length) return "INVALID";
    return "!"+dfns[l];
  }
  private String safeStr(byte i) {
    int l = i&0xff;
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
  
  
  
  static class Mut {
    ArrayList<Value> objs = new ArrayList<>();
    ArrayList<DfnTok> dfns = new ArrayList<>();
    ArrayList<String> strs = new ArrayList<>();
  
    public byte[] push(Value o) {
      if (objs.size() >= 256) throw new SyntaxError(">256 objects in one unit");
      byte[] res = {PUSH, (byte) objs.size()};
      objs.add(o);
      return res;
    }
    public byte[] push(DfnTok o) {
      int sz = dfns.size();
      if (sz >= 256) {
        if (sz >= 65536) throw new SyntaxError(">65536 dfns in one unit");
        byte[] res = {DFND2, (byte) (sz/256), (byte) (sz%256)};
        dfns.add(o);
        return res;
      }
      byte[] res = {DFND, (byte) sz};
      dfns.add(o);
      return res;
    }
    
    public byte[] varo(String s) {
      int sz = strs.size();
      if (sz >= 256) {
        if (sz >= 65536) throw new SyntaxError(">65536 variables in one unit");
        byte[] res = {VARO2, (byte) (sz/256), (byte) (sz%256)};
        strs.add(s);
        return res;
      }
      byte[] res = {VARO, (byte) sz};
      strs.add(s);
      return res;
    }
    public byte[] varm(String s) {
      int sz = strs.size();
      if (sz >= 256) {
        if (sz >= 65536) throw new SyntaxError(">65536 mutable variables in one unit");
        byte[] res = {VARM2, (byte) (sz/256), (byte) (sz%256)};
        strs.add(s);
        return res;
      }
      byte[] res = {VARM, (byte) sz};
      strs.add(s);
      return res;
    }
  }
  
  
  public static Comp comp(DfnTok t) {
    Mut mut = new Mut();
    List<LineTok> lns = t.tokens;
    byte[][] bcs = new byte[lns.size()*2][];
    int lret = -1;
    for (int i = 0; i < lns.size(); i++) {
      LineTok ln = lns.get(i);
      if (typeof(ln) != '_') lret = i;
      bcs[i*2] = compP(mut, ln, false);
    }
    if (lret == -1) throw new SyntaxError("Dfns must return something", t);
    for (int i = 0; i < lns.size(); i++) {
      bcs[i*2+1] = lret==i || lns.get(i).type=='_'? NOBYTES : new byte[]{POPS};
    }
    return new Comp(cat(bcs), mut.objs.toArray(new Value[0]), mut.strs.toArray(new String[0]), mut.dfns.toArray(new DfnTok[0]), t);
  }
  
  public static Comp comp(LineTok ln) {
    // return example();
    typeof(ln);
    Mut mut = new Mut();
    byte[] bc = compP(mut, ln, false);
    return new Comp(bc, mut.objs.toArray(new Value[0]), mut.strs.toArray(new String[0]), mut.dfns.toArray(new DfnTok[0]), ln);
  }
  
  public static Comp comp(TokArr<LineTok> lns) {
    Mut mut = new Mut();
    byte[][] bcs = new byte[lns.tokens.size()][];
    for (int i = 0; i < lns.tokens.size(); i++) {
      LineTok ln = lns.tokens.get(i);
      typeof(ln);
      bcs[i] = compP(mut, ln, false);
    }
    return new Comp(cat(bcs), mut.objs.toArray(new Value[0]), mut.strs.toArray(new String[0]), mut.dfns.toArray(new DfnTok[0]), lns);
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
  
  static class Res {
    char type;
    Token tk;
    byte[] bc;
  
    public Res(Token tk) {
      this.tk = tk;
      type = tk.type;
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
      return tk==null? type+"" : tk.source();
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
  public static void collect(LinkedList<Res> tps, Mut m, boolean train, boolean last) {
    while (true) {
      if (Main.debug) printlvl(tps.toString());
      if (tps.size() <= 1) break;
      if (train) { // trains only
        if (isE(tps, "d!|Off", last)) {
          if (Main.debug) printlvl("match F F F");
          Res f;
          tps.addLast(new Res('f',
            (  tps.removeLast()).comp(m, false),
            (  tps.removeLast()).comp(m, false),
            (f=tps.removeLast()).comp(m, false),
            new byte[]{f.type=='A'? TR3O : TR3D}
          ));
          continue;
        }
        if (isE(tps, "[←↩]|ff", last)) {
          if (Main.debug) printlvl("match F F");
          tps.addLast(new Res('f',
            tps.removeLast().comp(m, false),
            tps.removeLast().comp(m, false),
            new byte[]{TR2D}
          ));
          continue;
        }
      } else { // value expressions
        if (isE(tps, "d!|afa", last)) {
          if (Main.debug) printlvl("match a F a");
          Res x = tps.removeLast();
          Res f = tps.removeLast();
          Res w = tps.removeLast();
          tps.addLast(new Res(x.type,
            x.comp(m, false),
            f.comp(m, false),
            w.comp(m, false),
            new byte[]{x.type=='A' | w.type=='A'? FN2O : FN2C}
          ));
          continue;
        }
        if (isE(tps, "[da]!|fa", last)) {
          if (Main.debug) printlvl("match F a");
          Res x = tps.removeLast();
          Res f = tps.removeLast();
          tps.addLast(new Res(x.type,
            x.comp(m, false),
            f.comp(m, false),
            new byte[]{x.type=='A'? FN1O : FN1C}
          ));
          continue;
        }
      }
      // all
      
      int i = last? 0 : 1; // hopefully this doesn't need to be looping
      if (tps.get(0).type!='d') {
        if (isS(tps, "Om", i)) {
          if (Main.debug) printlvl("match O m");
          Res f;
          tps.add(i, new Res('f',
            (  tps.remove(i+1)).comp(m, false),
            (f=tps.remove(i  )).comp(m, false),
            f.type=='A'? CHKVBC : NOBYTES,
            new byte[]{OP1D}
          ));
          continue;
        }
        if (isS(tps, "OdO", i)) {
          if (Main.debug) printlvl("match O d O "+i);
          Res f, g;
          tps.add(i, new Res('f',
            (f=tps.remove(i+2)).comp(m, false),
            f.type=='A'? CHKVBC : NOBYTES,
            (  tps.remove(i+1)).comp(m, false),
            (g=tps.remove(i  )).comp(m, false),
            g.type=='A'? CHKVBC : NOBYTES,
            new byte[]{OP2D}
          ));
          continue;
        }
      }
      if (isS(tps, "dO", i)) {
        char t0 = tps.get(0).type;
        if (i==0 || t0!='a' && t0!='A' && t0!='f') {
          if (Main.debug) printlvl("match dO");
          Res f;
          tps.add(i, new Res('m',
            (f=tps.remove(i+1)).comp(m, false),
            f.type=='A'? CHKVBC : NOBYTES,
            (  tps.remove(i  )).comp(m, false),
            new byte[]{OP2H}
          ));
          continue;
        }
      }
      
      if (isE(tps, "af↩a", false)) {
        if (Main.debug) printlvl("af↩a");
        tps.addLast(new Res('a',
          tps.removeLast().comp(m, false),
          tps.removeLast().comp(m, false),
          tps.removeLast().comp(m, false), // empty
          tps.removeLast().comp(m, true),
          new byte[]{SETM}
        ));
        continue;
      }
      if (tps.size() >= 3) {
        char a = tps.get(tps.size()-2).type;
        if (a=='←' || a=='↩') {
          char k = tps.get(tps.size()-3).type;
          char v = tps.get(tps.size()-1).type;
          char ov = v;
          if (v=='A') v = 'a';
          if (k=='A') k = 'a'; // 𝕨↩ is a possibility
          if (k==v) {
            if (Main.debug) printlvl(k+" "+a+" "+v);
            tps.addLast(new Res(ov, // result is not v because typeof is stupid; +TODO
              tps.removeLast().comp(m, false),
              ov=='A'? CHKVBC : NOBYTES,
              tps.removeLast().comp(m, false), // empty
              tps.removeLast().comp(m, true),
              new byte[]{a=='←'? SETN : SETU}
            ));
            continue;
          } else if (v!='a' && k!='f') throw new SyntaxError(a+": Cannot assign with different types", tps.get(tps.size()-2).tk);
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
      Value b = Exec.builtin(op, null);
      if (b==null) {
        String s = op.op;
        switch (s) {
          case "𝕨":
            return t.type = 'A';
          case "𝕘": case "𝕗": case "𝕩": case "•":
            return t.type = 'a';
          case "𝔾": case "𝔽": case "𝕏": case "⍎": case "𝕎":
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
    }
    throw new ImplementationError("can't get type of "+t.getClass().getCanonicalName());
  }
  
  
  // @interface CheckReturnValue{}@CheckReturnValue
  public static byte[] compP(Mut m, Token tk, boolean mut) { // assumes tk has been typechecked
    assert tk.type != 0;
    if (mut) {
      if (tk instanceof NameTok) {
        return m.varm(((NameTok) tk).name);
      }
      if (tk instanceof StrandTok) {
        List<Token> tks = ((StrandTok) tk).tokens;
        int sz = tks.size();
        byte[][] bcs = new byte[sz+1][];
        for (int i = 0; i < tks.size(); i++) {
          bcs[i] = compP(m, tks.get(i), true);
        }
        if (sz > 255) throw new NYIError("array constants with >255 items", tk);
        bcs[bcs.length-1] = new byte[]{ARRM, (byte) sz};
        return cat(bcs);
      }
      if (tk instanceof ParenTok) {
        return compP(m, ((ParenTok) tk).ln, true);
      }
      if (tk instanceof LineTok) {
        if (((LineTok) tk).tokens.size() == 1) return compP(m, ((LineTok) tk).tokens.get(0), true);
      }
      if (tk instanceof OpTok) {
        String op = ((OpTok) tk).op;
        if (op.equals("•")) return new byte[]{SPEC, STDOUT};
        int aid = "𝕨𝕘𝕗𝕩𝕎𝔾𝔽𝕏".indexOf(op);
        if (aid != -1) {
          aid = aid % 8; // not %4 because surrogate pairs
          return m.varm("𝕨𝕘𝕗𝕩".substring(aid, aid+2));
        }
      }
      throw new SyntaxError(tk.toRepr()+" cannot be mutated", tk);
    }
    if (tk instanceof ParenTok) {
      return compP(m, ((ParenTok) tk).ln, false);
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
      if (ts.size() == 1) { return compP(m, ts.get(0), false); }
      int i = ts.size()-1;
  
      LinkedList<Res> tps = new LinkedList<>();
      Res t0 = new Res(ts.get(i));
      tps.addFirst(t0);
      i--;
      final boolean train = (t0.type!='a' && t0.type!='A')  ||  (ts.size()>=2 && ts.get(i).type=='d');
      
      
      if (Main.debug) {
        printlvl("parsing "+tk.source());
        Main.printlvl++;
      }
      
      while (i>=0) {
        Res c = new Res(ts.get(i));
        tps.addFirst(c);
        collect(tps, m, train, false);
        i--;
      }
      collect(tps, m, train, true);
      if (Main.debug) Main.printlvl--;
      
      if (tps.size()!=1) {
        Token t = null;
        for (int i1 = tps.size()-1; i1 >= 0; i1--) {
          Res tp = tps.get(i1);
          if (tp.tk != null) {
            t = tp.tk;
            break;
          }
        }
        throw new SyntaxError("couldn't join everything to a single expression", t);
      }
      assert tps.get(0).type == tk.type : tps.get(0).type + "≠" + tk.type;
      return tps.get(0).comp(m, false);
    }
    if (tk instanceof OpTok) {
      OpTok op = (OpTok) tk;
      Value b = Exec.builtin(op, null);
      if (b != null) {
        b.token = tk;
        return m.push(b);
      }
      
      String s = op.op;
      switch (s) {
        case "𝕨": case "𝕘": case "𝕗": case "𝕩":
          return m.varo(s);
        case "𝕎": case "𝔾": case "𝔽": case "𝕏":
          return m.varo(new String(new char[]{55349, (char) (s.charAt(1)+26)})); // lowercase
        case "⍎": return new byte[]{SPEC, EVAL };
        case "•": return new byte[]{SPEC, STDIN};
        default: throw new ImplementationError("Undefined unknown built-in "+s, op);
      }
    }
    if (tk instanceof NameTok) {
      return m.varo(((NameTok) tk).name);
    }
    if (tk instanceof StrandTok) { // +TODO (+↓) check for type A
      if (Main.debug) { printlvl("parsing "+tk.source()); Main.printlvl++; }
      List<Token> tks = ((StrandTok) tk).tokens;
      byte[][] bs = new byte[tks.size()+1][];
      for (int i = 0; i < tks.size(); i++) {
        bs[i] = compP(m, tks.get(i), false);
      }
      if (Main.debug) Main.printlvl--;
      
      int sz = tks.size();
      if (sz > 255) throw new NYIError("array constants with >255 items", tk);
      bs[bs.length-1] = new byte[]{ARRO, (byte) sz};
      return cat(bs);
    }
    if (tk instanceof ArrayTok) {
      if (Main.debug) { printlvl("parsing "+tk.source()); Main.printlvl++; }
      List<LineTok> tks = ((ArrayTok) tk).tokens;
      byte[][] bs = new byte[tks.size()+1][];
      for (int i = 0; i < tks.size(); i++) {
        bs[i] = compP(m, tks.get(i), false);
      }
      if (Main.debug) Main.printlvl--;
      
      int sz = tks.size();
      if (sz > 255) throw new NYIError("array constants with >255 items", tk);
      bs[bs.length-1] = new byte[]{ARRO, (byte) sz};
      return cat(bs);
    }
    if (tk instanceof SetTok || tk instanceof ModTok) {
      return NOBYTES;
    }
    if (tk instanceof DfnTok) {
      return m.push((DfnTok) tk);
    }
    if (tk instanceof NothingTok) {
      return m.push(((NothingTok) tk).val);
    }
    throw new ImplementationError("can't compile "+tk.getClass());
  }
}