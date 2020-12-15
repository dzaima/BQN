package APL.types;

import APL.Main;
import APL.errors.*;
import APL.tools.*;
import APL.types.arrs.*;
import APL.types.callable.builtins.fns.*;
import APL.types.callable.builtins.md1.CellBuiltin;
import APL.types.callable.builtins.md2.NCellBuiltin;

import java.util.*;

public abstract class Arr extends Value {
  public Arr(int[] shape) {
    super(shape);
  }
  public Arr(int[] shape, int ia) {
    super(shape, ia);
  }
  
  public Value call(         Value x) { return this; }
  public Value call(Value w, Value x) { return this; }
  
  public String basicFormat(boolean quote) {
    if (ia == 0) {
      Value pr = safePrototype();
      if (r() == 1) return pr instanceof Char? "\"\"" : pr instanceof Num? "0⥊0" : "⟨⟩";
      else return Main.formatAPL(shape) + "⥊" + (pr instanceof Char? "@" : pr instanceof Num? "0" : "⟨⟩");
    }
    if (r() == 1) { // strings
      StringBuilder all = new StringBuilder();
      for (Value v : this) {
        if (v instanceof Char) {
          char c = ((Char) v).chr;
          if (quote && c == '\"') all.append("\"\"");
          else all.append(c);
        } else return null;
      }
      if (quote) return "\"" + all + "\"";
      else return all.toString();
    }
    return null;
  }
  public String ln(FmtInfo fi) {
    String f = basicFormat(true);
    if (f != null) return f;
    if (r() == 0) return "<" + get(0).ln(fi);
    if (r() == 1) {
      if (ia == 1) return "⟨"+get(0).ln(fi)+"⟩";
      boolean vec = false;
      for (Value c : this) {
        if (!simple(c)) { vec = true; break; }
      }
      StringBuilder b = new StringBuilder();
      char c = vec? (MatchBuiltin.full(this)<=2? ',' : '⋄') : '‿';
      if (vec) b.append('⟨');
      boolean first = true;
      for (Value v : this) {
        if (first) first = false;
        else b.append(c);
        b.append(v.ln(fi));
      }
      if (vec) b.append('⟩');
      return b.toString();
    }
    
    StringBuilder b = new StringBuilder(">⟨");
    for (int i = 0; i < shape[0]; i++) {
      if (i != 0) b.append(",");
      b.append(LBoxBuiltin.getCell(i, this, null).ln(fi));
    }
    b.append("⟩");
    return b.toString();
  }
  public Arr reverseOn(int dim) {
    if (r() == 0) {
      if (dim != 0) throw new DomainError("rotating a scalar with a non-0 axis");
      return this;
    }
    if (dim < 0) dim+= r();
    // 2×3×4:
    // 0 - 3×4s for 2
    // 1 - 4s for 3
    // 2 - 1s for 4
    int chunkS = 1;
    int cPSec = shape[dim]; // chunks per section
    for (int i = r()-1; i > dim; i--) {
      chunkS*= shape[i];
    }
    int sec = chunkS * cPSec; // section length
    Value[] res = new Value[ia];
    int c = 0;
    while (c < ia) {
      for (int i = 0; i < cPSec; i++) {
        for (int j = 0; j < chunkS; j++) {
          res[c + (cPSec-i-1)*chunkS + j] = get(c + i*chunkS + j);
        }
      }
      c+= sec;
    }
    return Arr.create(res, shape);
  }
  
  
  public static Arr create(Value[] v) {
    return create(v, new int[]{v.length});
  }
  public static Arr create(Value[] v, int[] sh) { // note, doesn't attempt individual item squeezing
    assert Arr.prod(sh) == v.length : v.length+" ≢ ×´"+Main.formatAPL(sh);
    if (v.length == 0) return new EmptyArr(sh, null);
    da: if (v[0] instanceof Num) {
      ia: if (Num.isInt(((Num) v[0]).num)) {
        ba: if (Num.isBool(((Num) v[0]).num)) {
          BitArr.BA res = new BitArr.BA(sh);
          for (Value c : v) {
            if (!(c instanceof Num)) break da;
            double d = ((Num) c).num;
            if (d!=1 && Double.doubleToRawLongBits(d)!=0) break ba;
            res.add(d!=0);
          }
          return res.finish();
        }
        int[] is = new int[v.length];
        for (int i = 0; i < v.length; i++) {
          if (!(v[i] instanceof Num)) break da;
          double d = ((Num) v[i]).num;
          int n = (int) d;
          if (n != d || Double.doubleToRawLongBits(d)==Double.doubleToRawLongBits(-0.0d)) break ia;
          is[i] = n;
        }
        return new IntArr(is, sh);
      }
      double[] da = new double[v.length];
      for (int i = 0; i < v.length; i++) {
        if (v[i] instanceof Num) da[i] = ((Num)v[i]).num;
        else break da;
      }
      return new DoubleArr(da, sh);
    }
    ca: if (v[0] instanceof Char) {
      StringBuilder s = new StringBuilder();
      for (Value cv : v) {
        if (cv instanceof Char) s.append(((Char) cv).chr);
        else break ca;
      }
      return new ChrArr(s.toString(), sh);
    }
    return new HArr(v, sh);
  }
  
  public static Arr create(ArrayList<Value> v) {
    return create(v, new int[]{v.size()});
  }
  public static Arr create(ArrayList<Value> v, int[] sh) { // note, doesn't attempt individual item squeezing
    if (v.size() == 0) return new EmptyArr(sh, null);
    Value f = v.get(0);
    if (f instanceof Num) {
      double[] da = new double[v.size()];
      for (int i = 0; i < v.size(); i++) {
        if (v.get(i) instanceof Num) da[i] = ((Num) v.get(i)).num;
        else {
          da = null;
          break;
        }
      }
      if (da != null) return new DoubleArr(da, sh);
    }
    if (f instanceof Char) {
      StringBuilder s = new StringBuilder();
      for (Value cv : v) {
        if (cv instanceof Char) s.append(((Char) cv).chr);
        else {
          s = null;
          break;
        }
      }
      if (s != null) return new ChrArr(s.toString(), sh);
    }
    return new HArr(v, sh);
  }
  
  public boolean eq(Value x) {
    if (!Arrays.equals(shape, x.shape) || !(x instanceof Arr)) return false;
    int xh = ((Arr) x).hash;
    if (hash!=0 && xh!=0 && hash!=xh) return false;
    
    if (quickDoubleArr() && x.quickDoubleArr()) {
      int sm = (quickIntArr()?1:0)+(x.quickIntArr()?1:0);
      if (sm==0) {
        double[] wd =   asDoubleArr();
        double[] xd = x.asDoubleArr();
        for (int i = 0; i < ia; i++) if (wd[i]!=xd[i]) return false;
      } else if (sm==1) {
        boolean ti = quickIntArr();
        int   [] _i = ti?   asIntArr()    : x.asIntArr();
        double[] _d = ti? x.asDoubleArr() :   asDoubleArr();
        for (int i = 0; i < ia; i++) if (_i[i]!=_d[i]) return false;
      } else {
        int[] wi =   asIntArr();
        int[] xi = x.asIntArr();
        for (int i = 0; i < ia; i++) if (wi[i]!=xi[i]) return false;
      }
    } else {
      Value[] mvs =   values();
      Value[] ovs = x.values();
      for (int i = 0; i < mvs.length; i++) if (!mvs[i].eq(ovs[i])) return false;
    }
    return true;
  }
  
  public int hash; // 0 is uninitialized
  
  public int hashCode() {
    if (hash == 0) {
      for (Value v : this) hash = hash*31 + v.hashCode();
      hash = shapeHash(hash);
    }
    return hash;
  }
  
  protected int shapeHash(int hash) {
    int h = 0;
    for (int i : shape) {
      h = h*31 + i;
    }
    int res = hash*113 + h;
    if (res == 0) return 100003;
    return res;
  }
  
  public static int prod(int[] ia) {
    int r = 1;
    for (int i : ia) r*= i;
    return r;
  }
  public static int prod(int[] is, int s, int e) {
    int r = 1;
    for (int i = s; i < e; i++) r*= is[i];
    return r;
  }
  
  public static boolean eqPrefix(int[] w, int[] x, int prefix) {
    assert prefix <= w.length && prefix <= x.length;
    for (int i = 0; i < prefix; i++) if (w[i] != x[i]) return false;
    return true;
  }
  
  public static void eqShapes(Value w, Value x) {
    int[] ws = w.shape;
    int[] xs = x.shape;
    if (ws.length != xs.length) throw new RankError("ranks don't equal (shapes: " + Main.formatAPL(ws) + " vs " + Main.formatAPL(xs) + ")");
    for (int i = 0; i < ws.length; i++) {
      if (ws[i] != xs[i]) throw new LengthError("shapes don't match (" + Main.formatAPL(ws) + " vs " + Main.formatAPL(xs) + ")");
    }
  }
  public static void eqShapes(int[] w, int[] x, Callable blame) {
    if (w.length != x.length) throw new RankError("ranks don't equal (shapes: " + Main.formatAPL(w) + " vs " + Main.formatAPL(x) + ")", blame);
    for (int i = 0; i < w.length; i++) {
      if (w[i] != x[i]) throw new LengthError("shapes don't match (" + Main.formatAPL(w) + " vs " + Main.formatAPL(x) + ")", blame);
    }
  }
  
  
  public Value pretty(FmtInfo f) { // assumes a single Char is guaranteed to be a single character
    if (ia==0) return Format.str(ln(f));
    int r = r();
    Value g0 = first();
    
    spec: if (g0 instanceof Char && r<=2) { // strings
      String s;
      if (this instanceof ChrArr) s = ((ChrArr) this).s;
      else {
        StringBuilder b = new StringBuilder(ia);
        for (Value c : this) {
          if (!(c instanceof Char)) break spec;
          b.append(((Char) c).chr);
        }
        s = b.toString();
      }
      int sl = s.length();
  
      if (r==1) {
        MutVal m = new MutVal(new int[]{2+ia}, Char.SPACE);
        int i = 0;
        int o = 0;
        
        m.set(o++, Char.ASCII['"']);
        while (i != sl) {
          int c = s.codePointAt(i);
          int csz = Character.charCount(c);
          if (c < 32) c+= '␀';
          if (c == 127) c = '␡';
          m.set(o++, Format.chr(c, csz));
          i+= csz;
        }
        m.set(o++, Char.ASCII['"']);
        Value mv = m.get();
        if (i==o-2) return mv;
        return MutVal.cut(mv, 0, o, new int[]{o});
      } else if (r==2) {
        int w = shape[1];
        int h = shape[0];
        MutVal m = new MutVal(new int[]{h+2, w+4}); simpleBox(m);
        m.set(w+5     , Char.ASCII['"']);
        m.set(m.ia-w-6, Char.ASCII['"']);
        int o = w+6;
        for (int y = 0; y < h; y++) {
          for (int x = y*w; x < (y+1)*w; x++) {
            char g = s.charAt(x);
            if (g < 32) g+= '␀';
            if (g == 127) g = '␡';
            boolean isH = Character.isHighSurrogate(g);
            boolean isD = isH && x+1<sl && Character.isLowSurrogate(s.charAt(x+1));
            m.set(o++, isD? new ChrArr(s.substring(x,x+2)) : Character.isLowSurrogate(g)? Char.of('␠') : Char.of(g));
          }
          o+= 4;
        }
        return m.get();
      }
    }
    spec: if (r==2 && g0 instanceof Num) { // number matrix
      if (!this.quickDoubleArr()) for (Value c : this) if (!(c instanceof Num)) break spec;
      int w = shape[1]; int h = shape[0];
      Value[] v = new Value[ia];
      for (int i = 0; i < ia; i++) v[i] = get(i).pretty(f);
      int[] ws = new int[w];
      int[] hs = new int[h];
      for (int y = 0; y < h; y++) {
        for (int x = 0; x < w; x++) {
          Value c = v[x+y*w];
          ws[x] = Math.max(ws[x], c.shape[c.shape.length-1]);
          hs[y] = Math.max(hs[y], c.r()==1? 1 : c.shape[0]);
        }
      }
      int fw = w+1; for (int c : ws) fw+= c;
      int fh = 2  ; for (int c : hs) fh+= c;
      
      MutVal m = new MutVal(new int[]{fh, fw}); simpleBox(m);
      int cy = 1;
      for (int y = 0; y < h; y++) {
        int cx = 1;
        for (int x = 0; x < w; x++) {
          Value c = v[x + y*w];
          int dx = ws[x] - c.shape[c.shape.length-1];
          if (c.r()==2) m.copy(c, new int[]{cy, cx+dx});
          else m.copy(c, 0, cx + dx + cy*fw, c.ia);
          cx+= ws[x]+1;
        }
        cy+= hs[y];
      }
      return m.get();
    }
    
    spec: if (r==1 && simple(g0)) { // simple vectors; assumes simple always make simple strings, which might break
      if (!this.quickDoubleArr()) for (Value c : this) if (!simple(c)) break spec;
      if (ia == 1) return new ChrArr("⟨"+g0.pretty(f).asString()+"⟩");
      StringBuilder b = new StringBuilder(ia*2);
      boolean first = true;
      for (Value c : this) {
        if (first) first = false;
        else b.append("‿");
        b.append(c.pretty(f).asString());
      }
      return new ChrArr(b.toString());
    }
    
    if (r     == 0) return box(f, values(), 1, 1, reg, 0);
    if (r     == 1) return box(f, values(), ia, 1, reg, 1);
    if (r     == 2) return box(f, values(), shape[1], shape[0], reg, 2);
    if ((r&1) == 0) return box(f, NCellBuiltin.cells(this, 2), shape[1], shape[0], nst, 2); // even dimensions
    /*  (r&1) == 1*/return box(f, CellBuiltin.cells(this), shape[0], 1, nst, 1); // odd dimensions
  }
  static Char[] reg = chrs("┌┬┐├┼┤└┴┘─│");
  static Char[] nst = chrs("┏┳┓┣╋┫┗┻┛━┃");
  // static Char[] reg = chrs("╔╦╗╠╬╣╚╩╝═║");
  static Char[] chrs(String s) {
    Char[] r = new Char[s.length()];
    for (int i = 0; i < r.length; i++) r[i] = Char.of(s.charAt(i));
    return r;
  }
  
  static Value box(FmtInfo fi, Value[] vs, int w, int h, Char[] b, int rnk) { // b = {┌,┬,┐, ├,┼,┤, └,┴,┘, ─,│}
    Value[] f = new Value[vs.length];
    for (int i = 0; i < vs.length; i++) f[i] = vs[i].pretty(fi);
    int[] ws = new int[w];
    int[] hs = new int[h];
    for (int y = 0; y < h; y++) {
      for (int x = 0; x < w; x++) {
        Value c = f[x+y*w];
        ws[x] = Math.max(ws[x], c.shape[c.shape.length-1]);
        hs[y] = Math.max(hs[y], c.r()==1? 1 : c.shape[0]);
      }
    }
    int fw = w+1; for (int c : ws) fw+= c;
    int fh = h+1; for (int c : hs) fh+= c;
    MutVal m = new MutVal(new int[]{fh, fw}, f[0]);
    m.fill(Char.SPACE, 0, m.ia);
    
    int cy = 1;
    for (int y = 0; y < h; y++) {
      int cx = 1;
      for (int x = 0; x < w; x++) {
        Value c = f[x + y*w];
        if (c.r()==2) m.copy(c, new int[]{cy, cx});
        else m.copy(c, 0, cx + cy*fw, c.ia);
        cx+= ws[x]+1;
        for (int dy = 0; dy < hs[y]; dy++) m.set((cy+dy)*fw+cx-1, b[10]); // vline
      } for (int dy = 0; dy < hs[y]; dy++) m.set((cy+dy)*fw     , b[10]); // vline final 
      cy+= hs[y]+1;
      m.set(cy*fw-fw, b[3]); // ├
      m.set(cy*fw-1 , b[5]); // ┤
      m.fill(b[9], (cy-1)*fw+1, cy*fw-1); // hline
    } m.fill(b[9],           1,  fw  -1); // hline final
    
    cy = 0;
    for (int y = 0; y <= h; y++) {
      if (y>0) cy+= hs[y-1]+1;
      Char mc = y==0? b[1] : y==h? b[7] : b[4];
      int cx = 0;
      for (int x = 1; x < w; x++) {
        cx+= ws[x-1]+1;
        m.set(cy*fw + cx, mc);
      }
    }
    m.set(0   , b[0]); m.set(m.ia-1 , b[8]); // corners
    m.set(fw-1, b[2]); m.set(m.ia-fw, b[6]);
    if (rnk>0) m.set(1 , Char.of('→'));
    if (rnk>1) m.set(fw, Char.of('↓'));
    
    return m.get();
  }
  
  private static void simpleBox(MutVal m) {
    int w = m.sh[1];
    int h = m.sh[0];
    m.fill(Char.SPACE, 0, m.ia);
    m.set(w-1   , nst[2]); m.set(0     , nst[0]);
    m.set(m.ia-w, nst[6]); m.set(m.ia-1, nst[8]);
    m.fill(nst[9], 1, w-1); m.fill(nst[9], 1+m.ia-w, m.ia-1);
    for (int y = 1; y < h-1; y++) { m.set(y*w, nst[10]); m.set(y*w+w-1, nst[10]); }
  }
  
  private static boolean simple(Value v) {
    if (v instanceof Char) return !((Char) v).spec();
    return v instanceof Num || v instanceof BigValue;
  }
}