package APL.types.functions.builtins.fns2;

import APL.Main;
import APL.errors.*;
import APL.tools.MutVal;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.functions.builtins.FnBuiltin;
import APL.types.functions.builtins.dops.AtopBuiltin;

import java.util.Arrays;

public class ShapeBuiltin extends FnBuiltin {
  public String repr() {
    return "⥊";
  }
  
  
  
  public Value call(Value x) {
    if (x instanceof Primitive) {
      if (x instanceof Num) return x.ofShape(SingleItemArr.SH1);
      if (x instanceof Char) return new ChrArr(String.valueOf(((Char) x).chr));
      return SingleItemArr.sh1(x);
    }
    return x.ofShape(new int[]{x.ia});
  }
  
  
  
  public Value call(Value w, Value x) {
    if (w.rank > 1) throw new DomainError("⥊: multidimensional shape (≢𝕨 is "+Main.formatAPL(w.shape)+")", this, w);
    int[] sh;
    int ia;
    int emptyPos = -1;
    int emptyMode = 2; // 0-∘(exact); 1-⌊(discard); 2-⌽(recycle); 3-↑(pad)
    if (w.quickDoubleArr()) {
      sh = w.asIntVec();
      ia = Arr.prod(sh);
    } else {
      sh = new int[w.ia];
      ia = 1;
      for (int i = 0; i < sh.length; i++) {
        Value v = w.get(i);
        if (v instanceof Num) {
          int c = v.asInt();
          sh[i] = c;
          ia*= c;
        } else {
          if (emptyPos!=-1) throw new DomainError("⥊: contained multiple specials", this);
          emptyPos = i;
               if (v instanceof    AtopBuiltin) emptyMode = 0;
          else if (v instanceof   FloorBuiltin) emptyMode = 1;
          else if (v instanceof ReverseBuiltin) emptyMode = 2;
          else if (v instanceof UpArrowBuiltin) emptyMode = 3;
          else throw new DomainError("⥊: shape contained "+v, this, v);
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
        if (mod != 0) throw new LengthError("⥊: empty dimension not perfect (𝕨 ≡ "+w.oneliner()+"; "+(x.ia)+" = ≢𝕩)", this);
      } else if (emptyMode!=1) {
        if (mod != 0) r++;
      }
      sh[emptyPos] = r;
      ia*= r;
    }
  
    if (x.ia == 0) {
      Value proto = x.safePrototype();
      if (ia==0) return new EmptyArr(sh, proto);
      if (proto==null) throw new DomainError("⥊: unknown prototype when resizing empty array to shape "+w.oneliner(), this);
      return new SingleItemArr(proto, sh);
    }
    if (ia == x.ia) return x.ofShape(sh);
    
    if (emptyMode==3) {
      MutVal v = new MutVal(sh, x);
      v.copy(x, 0, 0, x.ia);
      if (mod != 0) v.fill(x.safePrototype(), x.ia, v.ia); // x won't be empty, so there must be a prototype
      return v.get();
    }
    
    
    if (x.scalar()) {
      return new SingleItemArr(x.first(), sh);
    } else if (x instanceof BitArr) {
      BitArr xb = (BitArr) x;
      BitArr.BA res = new BitArr.BA(sh);
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
    Value v = o instanceof Fun? o.call(call(w, x)) : o;
    for (int i = 0; i < w.ia; i++) {
      Value c = w.get(i);
      if (!(c instanceof Num)) { // a‿⟨⟩‿b ⥊ w - must use all items
        if (x.rank == 0 && v.first() instanceof Primitive) return v.first();
        if (v.ia != x.ia) throw new DomainError("⌾⥊ expected equal amount of output & output items", this);
        return v.ofShape(x.shape);
      }
    }
    int[] sh = w.asIntVec();
    int am = Arr.prod(sh);
    if (am > x.ia) throw new DomainError("⌾("+Main.formatAPL(sh)+"⥊) applied on array with "+x.ia+" items", this);
    if (!Arrays.equals(sh, v.shape)) throw new DomainError("⌾⥊ expected equal amount of output & output items", this);
    Value[] vs = new Value[x.ia];
    System.arraycopy(v.values(), 0, vs, 0, am);
    System.arraycopy(x.values(), am, vs, am, vs.length-am);
    return Arr.create(vs, x.shape);
  }
  
  
  public Value under(Value o, Value x) {
    Value v = o instanceof Fun? o.call(call(x)) : o;
    if (v.ia != x.ia) throw new DomainError("⌾⥊ expected equal amount of output & output items", this);
    return v.ofShape(x.shape);
  }
  
}