package BQN.types.callable.builtins.fns;

import BQN.Main;
import BQN.errors.*;
import BQN.tools.*;
import BQN.types.*;
import BQN.types.arrs.*;
import BQN.types.callable.builtins.FnBuiltin;
import BQN.types.callable.builtins.md2.AtopBuiltin;

import java.util.Arrays;

public class ShapeBuiltin extends FnBuiltin {
  public String ln(FmtInfo f) { return "⥊"; }
  
  public Value call(Value x) {
    if (x instanceof Primitive) {
      if (x instanceof Num) return x.ofShape(SingleItemArr.SH1);
      if (x instanceof Char) return new ChrArr(String.valueOf(((Char) x).chr));
      return SingleItemArr.sh1(x);
    }
    return x.ofShape(new int[]{x.ia});
  }
  
  
  
  public Value call(Value w, Value x) {
    if (w.r() > 1) throw new DomainError("⥊: multidimensional shape (≢𝕨 is "+Main.fArr(w.shape)+")", this);
    int[] sh;
    int emptyPos = -1;
    int emptyMode = 2; // 0-∘(exact); 1-⌊(discard); 2-⌽(recycle); 3-↑(pad)
    int ia = 1;
    if (w.quickDoubleArr()) {
      sh = w.asIntVec();
      for (int c : sh) {
        ia*= c;
        if (c < 0) throw new DomainError("⥊: didn't expect "+Num.formatInt(c)+" in shape", this);
      }
    } else {
      sh = new int[w.ia];
      for (int i = 0; i < sh.length; i++) {
        Value v = w.get(i);
        if (v instanceof Num) {
          int c = v.asInt();
          sh[i] = c;
          ia*= c;
          if (c < 0) throw new DomainError("⥊: didn't expect "+Num.formatInt(c)+" in shape", this);
        } else {
          if (emptyPos!=-1) throw new DomainError("⥊: contained multiple specials", this);
          emptyPos = i;
               if (v instanceof    AtopBuiltin) emptyMode = 0;
          else if (v instanceof   FloorBuiltin) emptyMode = 1;
          else if (v instanceof ReverseBuiltin) emptyMode = 2;
          else if (v instanceof UpArrowBuiltin) emptyMode = 3;
          else throw new DomainError("⥊: shape contained "+v, this);
        }
      }
    }
    
    int mod = 0;
    if (emptyPos!=-1) {
      if (ia==0) throw new DomainError("⥊: cannot compute axis if the resulting array is empty", this);
      int div = x.ia/ia;
      mod = x.ia%ia;
      int r = div;
      if (emptyMode==0) {
        if (mod != 0) throw new LengthError("⥊: empty dimension not perfect (𝕨 ≡ "+w.ln(FmtInfo.def)+"; "+(x.ia)+" = ≢𝕩)", this);
      } else if (emptyMode!=1) {
        if (mod != 0) r++;
      }
      sh[emptyPos] = r;
      ia*= r;
    }
    
    if (ia == 0) return new EmptyArr(sh, x.fItemS());
    if (x.ia == 0) throw new DomainError("⥊: resizing empty array to non-empty (𝕨 ≡ "+w.ln(FmtInfo.def)+")", this);
    if (ia == x.ia) return x.ofShape(sh);
    
    if (emptyMode==3) {
      MutVal v = new MutVal(sh, x);
      v.copy(x, 0, 0, x.ia);
      if (mod != 0) v.fill(x.fItemS(), x.ia, v.ia); // x won't be empty, so there must be a prototype
      return v.get();
    }
    
    
    if (x.scalar()) {
      return new SingleItemArr(x.first(), sh);
    } else if (x instanceof BitArr) {
      BitArr xb = (BitArr) x;
      BitArr.BA res = new BitArr.BA(sh, false);
      int full = ia/xb.ia;
      int frac = ia%xb.ia;
      for (int i = 0; i < full; i++) res.add(xb);
      res.add(xb, 0, frac);
      return res.finish();
    } else if (x.quickIntArr()) {
      assert !(x instanceof Primitive);
      int[] inp = x.asIntArr();
      int[] res = new int[ia];
      int p = 0;
      for (int i = 0; i < ia; i++) {
        res[i] = inp[p++];
        if (p == x.ia) p = 0;
      }
      return new IntArr(res, sh);
    } else if (x.quickDoubleArr()) {
      assert !(x instanceof Primitive);
      double[] inp = x.asDoubleArr();
      double[] res = new double[ia];
      int p = 0;
      for (int i = 0; i < ia; i++) {
        res[i] = inp[p++];
        if (p == x.ia) p = 0;
      }
      return new DoubleArr(res, sh);
    } else if (x instanceof ChrArr) {
      String inp = ((ChrArr) x).s;
      char[] res = new char[ia];
      int p = 0;
      for (int i = 0; i < ia; i++) {
        res[i] = inp.charAt(p++);
        if (p == x.ia) p = 0;
      }
      return new ChrArr(res, sh);
    } else {
      Value[] arr = new Value[ia];
      int index = 0;
      for (int i = 0; i < ia; i++) {
        arr[i] = x.get(index++);
        if (index == x.ia) index = 0;
      }
      return Arr.create(arr, sh);
    }
  }
  
  public Value underW(Value o, Value w, Value x) {
    Value cr = call(w, x);
    Value v = o instanceof Fun? o.call(cr) : o;
    if (!Arrays.equals(cr.shape, v.shape)) throw new DomainError("F⌾⥊: Expected F to not change its arguments shape", this);
    if (cr.ia > x.ia) throw new DomainError("⌾⥊: Result of ⥊ had more elements than 𝕩", this);
    MutVal r = new MutVal(x.shape, x, x.ia);
    r.copy(v, 0, 0, v.ia);
    r.copy(x, v.ia, v.ia, x.ia-v.ia);
    return r.get();
  }
  
  
  public Value under(Value o, Value x) {
    Value v = o instanceof Fun? o.call(call(x)) : o;
    if (v.ia != x.ia) throw new DomainError("⌾⥊: Expected equal amount of output & output items", this);
    return v.ofShape(x.shape);
  }
  
}
