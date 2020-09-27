package APL.types;

import APL.Main;
import APL.errors.*;
import APL.types.arrs.*;
import APL.types.callable.builtins.fns.LBoxBuiltin;

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
      String mr = safePrototype() instanceof Char? "\"\"" : "⟨⟩";
      if (r() == 1) return mr;
      else return Main.formatAPL(shape) + "⥊" + mr;
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
  public String toString() {
    String f = basicFormat(Main.quotestrings);
    if (f != null) return f;
    
    if (Main.quotestrings) {
      boolean str = true;
      for (Value v : this) {
        if (!(v instanceof Char)) {
          str = false;
          break;
        }
      }
      if (str) return oneliner();
    }
    
    if (r() == 0) return "<"+first().toString().replace("\n", "\n ");
    if (ia == 1) {
      Value c = get(0);
      if (c instanceof Primitive || r() > 2) {
        String enc = c instanceof Primitive? "" : "<";
        if (r()==1) return "⟨"+c+"⟩";
        String pre = Main.formatAPL(shape);
        return pre + "⥊" + enc + c.toString().replace("\n", "\n" + Main.repeat(" ", pre.length()+2));
      }
    }
    if (r() == 1) { // simple vectors
      StringBuilder res = new StringBuilder();
      boolean simple = true;
      for (Value v : this) {
        if (res.length() > 0) res.append('‿');
        if (v != null) {
          if (!simple(v)) { simple = false; break; }
          res.append(v.oneliner());
        } else res.append("NULLPTR");
      }
      if (simple) return res.toString();
    }
    
    if (r() == 2) {
      boolean charmat = true;
      if (!(this instanceof ChrArr)) {
        for (Value v : this) {
          if (!(v instanceof Char)) {
            charmat = false;
            break;
          }
        }
      }
      
      if (charmat) {
        StringBuilder b = new StringBuilder();
        int i = 0;
        for (Value v : this) {
          if (i++ % shape[1] == 0 && i!=1) b.append('\n');
          b.append(((Char) v).chr);
        }
        return b.toString();
      }
    }
    
    if (r() < 3) { // boxed arrays
      int w = r()==1? shape[0] : shape[1];
      int h = r()==1? 1 : shape[0];
      String[][][] stringified = new String[w][h][];
      int[][] itemWidths = new int[w][h];
      int[] widths = new int[w];
      int[] heights = new int[h];
      boolean simple = true;
      int x=0, y=0;
      for (Value v : this) {
        if (v == null) v = Main.toAPL("NULLPTR");
        simple&= simple(v);
        String[] c = v.toString().split("\n");
        int cw = 0;
        for (String ln : c) cw = Math.max(ln.length(), cw);
        itemWidths[x][y] = cw;
        
        widths[x] = Math.max(widths[x], cw);
        heights[y] = Math.max(heights[y], c.length);
        
        stringified[x][y] = c;
        x++;
        if (x==w) {
          x = 0;
          y++;
        }
      }
      int borderSize = simple? 0 : 1;
      int rw = simple? -1 : 1,
      rh = borderSize ; // result w&h;
      for (x = 0; x < w; x++) rw+= widths[x]+1;
      for (y = 0; y < h; y++) rh+= heights[y]+borderSize;
      char[][] chars = new char[rh][rw];
      int rx = borderSize , ry; // x&y in chars
      for (x = 0; x < w; x++) {
        ry = borderSize;
        for (y = 0; y < h; y++) {
          String[] cobj = stringified[x][y];
          for (int cy = 0; cy < cobj.length; cy++) {
            String s = cobj[cy];
            char[] line = s.toCharArray();
            int sx = get(y*w + x) instanceof Num? rx+widths[x]-itemWidths[x][y] : rx;
            System.arraycopy(line, 0, chars[ry + cy], sx, line.length);
          }
          ry+= heights[y]+borderSize;
        }
        rx+= widths[x]+1;
      }
      if (!simple) { // draw borders
        rx = 0;
        for (x = 0; x < w; x++) {
          ry = 0;
          for (y = 0; y < h; y++) {
            chars[ry][rx] = '┼';
            for (int cx = 1; cx <=  widths[x]; cx++) chars[ry][rx+cx] = '─';
            for (int cy = 1; cy <= heights[y]; cy++) chars[ry+cy][rx] = '│';
            if (x == 0) {
              for (int cy = 1; cy <= heights[y]; cy++) chars[ry+cy][rw-1] = '│';
              chars[ry][rw-1] = y==0? '┐' : '┤';
              chars[ry][0] = '├';
            }
            ry+= heights[y]+borderSize;
          }
          chars[0][rx] = '┬';
          chars[rh-1][rx] = x==0?'└' : '┴';
          for (int cx = 1; cx <=  widths[x]; cx++) chars[rh-1][rx+cx] = '─';
          rx+= widths[x]+1;
        }
        chars[0][0] = '┌';
        chars[rh-1][rw-1] = '┘';
      }
      for (char[] ca : chars) {
        for (int i = 0; i < ca.length; i++) {
          if (ca[i] == 0) ca[i] = ' ';
        }
      }
      StringBuilder res = new StringBuilder();
      boolean next = false;
      for (char[] ln : chars) {
        if (next) res.append('\n');
        res.append(ln);
        next = true;
      }
      return res.toString();
    } else return oneliner();
  }
  public String oneliner() {
    String f = basicFormat(true);
    if (f != null) return f;
    if (r() == 0) return "<" + get(0).oneliner();
    if (r() == 1) {
      if (ia == 1) return "⟨"+get(0).oneliner()+"⟩";
      boolean vec = false;
      for (Value c : this) {
        if (!simple(c)) { vec = true; break; }
      }
      StringBuilder b = new StringBuilder();
      if (vec) b.append('⟨');
      boolean first = true;
      for (Value v : this) {
        if (first) first = false;
        else b.append(vec? ',' : '‿');
        b.append(v.oneliner());
      }
      if (vec) b.append('⟩');
      return b.toString();
    }
    
    StringBuilder b = new StringBuilder(">⟨");
    for (int i = 0; i < shape[0]; i++) {
      if (i != 0) b.append(",");
      b.append(LBoxBuiltin.getCell(i, this, null).oneliner());
    }
    b.append("⟩");
    return b.toString();
  }
  private static boolean simple(Value v) {
    return v instanceof Num || v instanceof Char || v instanceof BigValue;
  }
  public Arr reverseOn(int dim) {
    if (r() == 0) {
      if (dim != 0) throw new DomainError("rotating a scalar with a non-0 axis", this);
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
    if (!Arrays.equals(shape, x.shape)) return false;
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
    if (ws.length != xs.length) throw new RankError("ranks don't equal (shapes: " + Main.formatAPL(ws) + " vs " + Main.formatAPL(xs) + ")", x);
    for (int i = 0; i < ws.length; i++) {
      if (ws[i] != xs[i]) throw new LengthError("shapes don't match (" + Main.formatAPL(ws) + " vs " + Main.formatAPL(xs) + ")", x);
    }
  }
  public static void eqShapes(int[] w, int[] x, Callable blame) {
    if (w.length != x.length) throw new RankError("ranks don't equal (shapes: " + Main.formatAPL(w) + " vs " + Main.formatAPL(x) + ")", blame);
    for (int i = 0; i < w.length; i++) {
      if (w[i] != x[i]) throw new LengthError("shapes don't match (" + Main.formatAPL(w) + " vs " + Main.formatAPL(x) + ")", blame);
    }
  }
}