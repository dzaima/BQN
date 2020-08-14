package APL.tools;

import APL.types.*;
import APL.types.arrs.*;

import java.util.Arrays;

public class MutVal { // inserts can be in any order (might change to a sequential requirement later), but must not override previous ones
  private final int[] sh;
  public final int ia;
  
  long  [] ls; // 1 
  double[] ds; // 2
  char  [] cs; // 3
  int   [] is; // 4
  Value [] vs; // 5
  
  int mode; // 0 - unknown; 1 - ls; 2 - ds; 3 - cs; 4 - is; 5 - vs
  public MutVal(int[] sh) {
    this.sh = sh;
    ia = Arr.prod(sh);
    mode = 0;
  }
  public MutVal(int[] sh, Value base) {
    this.sh = sh;
    ia = Arr.prod(sh);
    guess(base);
  }
  
  public MutVal(int[] sh, Value base, int len) {
    this.sh = sh;
    ia = len;
    guess(base);
  }
  
  public Value get() {
    switch (mode) { default: assert ia==0; return new EmptyArr(sh, null);
      case 1: return new BitArr(ls, sh);
      case 2: return new DoubleArr(ds, sh);
      case 3: return new ChrArr(cs, sh);
      case 4: return new IntArr(is, sh);
      case 5: return new HArr(vs, sh);
    }
  }
  
  public void copy(Value x, int xS, int rS, int len) { // TODO be careful about ¯0
    // System.out.println(mode);
    if (len==0) return;
    switch (mode) { default: throw new IllegalStateException();
      case 0:
        guess(x);
        copy(x, xS, rS, len);
        break;
        
        
      case 1:
        if (x instanceof BitArr) {
          BitArr.copy(((BitArr) x).arr, xS, ls, rS, len); // todo special-case small insertions?
          return;
        } else if (x.quickDoubleArr()) {
          if (x instanceof IntArr) {
            int rSc = rS;
            for (int i = 0; i < len; i++) {
              int[] xi = ((IntArr) x).arr;
              int d = xi[xS+i];
              if (d!=1 & d!=0) { move(4); copy(x, xS+i, rSc, len-i); return; }
              if (d==1) ls[rSc>>6]|= 1L<<(rSc&63);
              rSc++;
            }
            return;
          } else if (x instanceof DoubleArr) {
            int rSc = rS;
            for (int i = 0; i < len; i++) {
              double[] xd = ((DoubleArr) x).arr;
              double d = xd[xS+i];
              if (d!=1 & d!=0) { move(Num.isInt(d)? 4 : 2); copy(x, xS+i, rSc, len-i); return; }
              if (d==1) ls[rSc>>6]|= 1L<<(rSc&63);
              rSc++;
            }
            return;
          } else if (x instanceof Num) {
            double d = ((Num) x).num;
            if (d!=1 & d!=0) { move(Num.isInt(d)? 4 : 2); copy(x, xS, rS, len); return; }
            if (d==1) ls[rS>>6]|= 1L<<(rS&63);
            return;
          }
        }
        int rSc = rS;
        for (int i = 0; i < len; i++) {
          Value c = x.get(xS+i);
          if (c instanceof Num) {
            double d = ((Num) c).num;
            if (d!=1 & d!=0) { move(Num.isInt(d)? 4 : 2); copy(x, xS+i, rS+i, len-i); return; }
            if (d==1) ls[rSc>>6]|= 1L<<(rSc&63);
            rSc++;
          } else { move(5); copy(x, xS+i, rS+i, len-i); return; }
        }
        break;
        
        
      case 2:
        if (x.quickDoubleArr()) {
          if (x instanceof DoubleArr) {
            System.arraycopy(x.asDoubleArr(), xS, ds, rS, len);
            return;
          } else if (x instanceof IntArr) {
            int[] arr = ((IntArr) x).arr;
            for (int i = 0; i < len; i++) ds[i+rS] = arr[i+xS];
            return;
          } else if (x instanceof Num) {
            ds[rS] = ((Num) x).num;
            return;
          } else if (x instanceof SingleItemArr) {
            double d = ((Num) x.get(0)).num;
            Arrays.fill(ds, rS, rS+len, d);
            return;
          }
        }
        
        for (int i = 0; i < len; i++) {
          Value c = x.get(xS+i);
          if (c instanceof Num) ds[rS+i] = ((Num) c).num;
          else { move(5); copy(x, xS+i, rS+i, len-i); return; }
        }
        
        break;
        
        
      case 3:
        if (x instanceof ChrArr) {
          String s = ((ChrArr) x).s;
          s.getChars(xS, xS+len, cs, rS);
        } else if (x instanceof Char) {
          cs[rS] = ((Char) x).chr;
        } else {
          for (int i = 0; i < len; i++) {
            Value c = x.get(xS+i);
            if (c instanceof Char) cs[rS+i] = ((Char) c).chr;
            else { move(5); copy(x, xS+i, rS, len-i); return; }
          }
        }
        break;
      case 4:
        
        if (x.quickIntArr()) {
          if (x instanceof IntArr) {
            System.arraycopy(x.asIntArr(), xS, is, rS, len);
            return;
          } else if (x instanceof SingleItemArr) {
            double d = ((Num) x.get(0)).num;
            Arrays.fill(is, rS, rS+len, (int) d);
            return;
          } else if (x instanceof BitArr) {
            BitArr.BR xb = ((BitArr) x).read();
            for (int i = 0; i < len; i++) is[rS + i] = xb.read()? 1 : 0;
            return;
          }
        }
        if (x.quickDoubleArr()) {
          if (x instanceof DoubleArr) {
            double[] xd = x.asDoubleArr();
            for (int i = 0; i < len; i++) {
              double n = xd[i+xS];
              if ((int)n != n) { move(2); copy(x, xS+i, rS+i, len-i); return; }
              is[rS+i] = (int) n;
            }
            return;
          } else if (x instanceof Num) {
            double n = ((Num) x).num;
            if ((int)n != n) { move(2); copy(x, xS, rS, len); return; }
            is[rS] = (int) n;
            return;
          }
        }
        
        for (int i = 0; i < len; i++) {
          Value c = x.get(xS+i);
          if (c instanceof Num) {
            double n = ((Num) c).num;
            if ((int)n != n) { move(2); copy(x, xS+i, rS+i, len-i); return; }
            is[rS+i] = (int) n;
          }
          else { move(5); copy(x, xS+i, rS+i, len-i); return; }
        }
        break;
        
      case 5:
        if (x instanceof HArr) {
          System.arraycopy(x.values(), xS, vs, rS, len);
        } else if (x instanceof SingleItemArr) {
          Arrays.fill(vs, rS, rS+len, x.get(0));
        } else {
          for (int i = 0; i < len; i++) vs[rS+i] = x.get(xS+i);
        }
        break;
    }
  }
  
  
  
  public void set(int p, Value x) {
    switch (mode) { default: throw new IllegalStateException();
      case 0:
        guess(x);
        set(p, x);
        return;
      case 1:
        if (x instanceof Num) {
          double d = ((Num) x).num;
          if (d!=1 & d!=0) { move(Num.isInt(d)? 4 : 2); set(p, x); return; }
          if (d==1) ls[p>>6]|= 1L<<(p&63);
        } else { move(5); set(p, x); }
        return;
      case 2:
        if (x instanceof Num) {
          ds[p] = ((Num) x).num;
        } else { move(5); set(p, x); }
        return;
      case 3:
        if (x instanceof Char) {
          cs[p] = ((Char) x).chr;
        } else { move(5); set(p, x); }
        return;
      case 4:
        if (x instanceof Num) {
          double d = ((Num) x).num;
          if (Num.isInt(d)) is[p] = (int) d;
          else { move(2); set(p, x); }
        } else { move(5); set(p, x); }
        return;
      case 5:
        vs[p] = x;
    }
  }
  
  
  
  public Value get(int i) {
    switch (mode) { default: throw new IllegalStateException();
      case 1: return Num.NUMS[(int) (ls[i>>6]>>(i&63) & 1)];
      case 2: return Num.of(ds[i]);
      case 3: return Char.of(cs[i]);
      case 4: return Num.of(is[i]);
      case 5: return vs[i];
    }
  }
  
  
  
  public void fill(Value x, int s, int e) {
    switch (mode) { default: throw new IllegalStateException();
      case 0: guess(x); fill(x, s, e); return;
      case 1: {
        if (!(x instanceof Num)) { move(5); fill(x, s, e); return; }
        double d = ((Num) x).num; if (d==0) return;
        if (d==1) { BitArr.fill(ls, s, e); return; }
        if (Num.isInt(d)) { move(4); fill(x, s, e); return; }
        move(2); fill(x, s, e); return;
      }
      case 2: {
        if (!(x instanceof Num)) { move(5); fill(x, s, e); return; }
        double d = ((Num) x).num; if (d==0) return;
        for (int i = s; i < e; i++) ds[i] = d;
        break;
      }
      case 3: {
        if (!(x instanceof Char)) { move(5); fill(x, s, e); return; }
        char c = ((Char) x).chr;
        for (int i = s; i < e; i++) cs[i] = c;
        return;
      }
      case 4: {
        if (!(x instanceof Num)) { move(5); fill(x, s, e); return; }
        double d = ((Num) x).num; if (d==0) return;
        if (!Num.isInt(d)) { move(2); fill(x, s, e); return; }
        int xi = (int) d;
        for (int i = s; i < e; i++) is[i] = xi;
        return;
      }
      case 5:
        for (int i = s; i < e; i++) vs[i] = x;
    }
  }
  
  
  
  
  
  
  
  
  private void init() {
    switch (mode) { default: throw new IllegalStateException();
      case 1: ls = new long[BitArr.sizeof(ia)]; break;
      case 2: ds = new double[ia]; break;
      case 3: cs = new char[ia]; break;
      case 4: is = new int[ia]; break;
      case 5: vs = new Value[ia]; break;
    }
  }
  
  void guess(Value base) {
    assert mode == 0;
    if (base instanceof SingleItemArr) {
      base = base.first();
      if (!(base instanceof Primitive)) { mode = 5; init(); return; }
    }
    if (base.quickDoubleArr()) {
      mode = 2;
      if (base.quickIntArr()) mode = 4;
      if (base instanceof BitArr) mode = 1;
      else if (base instanceof Num) {
        double d = ((Num) base).num;
        if (d==0 || d==1) mode = 1;
      }
    } else if (base instanceof ChrArr || base instanceof Char) mode = 3;
    else mode = 5;
    init();
  }
  
  private void move(int nm) {
    assert nm!=mode;
    switch (nm) { default: throw new IllegalStateException();
      case 2: ds = new double[ia]; break;
      case 4: is = new int[ia]; break;
      case 5: vs = new Value[ia]; break;
    }
    // System.out.println("to "+nm);
    switch (mode) { default: throw new IllegalStateException();
      case 1:
        if (nm==2) {
          int r = 0;
          for (int i = 0; i < ia/64; i++) { long c = ls[i];
            for (int j = 0; j < 64     ; j++) { ds[r++] = c&1; c>>= 1; }
          }
          if ((ia&63) != 0) { long c = ls[ls.length-1];
            for (int i = 0; i < (ia&63); i++) { ds[r++] = c&1; c>>= 1; }
          }
        } else if (nm==4) {
          int r = 0;
          for (int i = 0; i < ia/64; i++) { long c = ls[i];
            for (int j = 0; j < 64     ; j++) { is[r++] = (int) (c&1); c>>= 1; }
          }
          if ((ia&63) != 0) { long c = ls[ls.length-1];
            for (int i = 0; i < (ia&63); i++) { is[r++] = (int) (c&1); c>>= 1; }
          }
        } else {
          assert nm==5;
          int r = 0;
          for (int i = 0; i < ia/64; i++) {
            long c = ls[i];
            for (int j = 0; j < 64     ; j++) { vs[r++] = Num.NUMS[(int) (c&1)]; c>>= 1; }
          }
          if ((ia&63) != 0) { long c = ls[ls.length-1];
            for (int i = 0; i < (ia&63); i++) { vs[r++] = Num.NUMS[(int) (c&1)]; c>>= 1; }
          }
        }
        break;
      case 2:
        assert nm==5;
        for (int i = 0; i < ia; i++) vs[i] = Num.of(ds[i]);
        break;
      case 3:
        assert nm==5;
        for (int i = 0; i < ia; i++) vs[i] = Char.of(cs[i]);
        break;
      case 4:
        if (nm==2) {
          for (int i = 0; i < is.length; i++) ds[i] = is[i];
        } else {
          assert nm==5;
          for (int i = 0; i < is.length; i++) vs[i] = Num.of(is[i]);
        }
        break;
    }
    switch (mode) { default: throw new IllegalStateException();
      case 1: ls=null; break;
      case 2: ds=null; break;
      case 3: cs=null; break;
      case 4: is=null; break;
      case 5: vs=null; break;
    }
    mode = nm;
  }
  
  public static Value cut(Value src, int sP, int len, int[] sh) {
    MutVal v = new MutVal(sh, src, len);
    v.copy(src, sP, 0, len);
    return v.get();
  }
}
