package APL.types.functions.builtins.fns2;

import APL.Main;
import APL.errors.*;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.functions.Builtin;

import java.util.Arrays;

public class ShapeBuiltin extends Builtin {
  public String repr() {
    return "⥊";
  }
  
  
  
  public Value call(Value x) {
    if (x instanceof Primitive) {
      if (x instanceof Num) return new DoubleArr(new double[]{((Num) x).num});
      if (x instanceof Char) return new ChrArr(String.valueOf(((Char) x).chr));
      return new Shape1Arr(x);
    }
    return x.ofShape(new int[]{x.ia});
  }
  
  
  
  public Value call(Value w, Value x) {
    if (w.rank > 1) throw new DomainError("⥊: multidimensional shape (≢𝕨 is "+Main.formatAPL(w.shape)+")", this, w);
    int[] sh;
    int ia;
    Integer emptyPos = null;
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
        } else if (v.ia == 0) {
          if (emptyPos == null) emptyPos = i;
          else throw new DomainError("⥊: shape contained multiple ⟨⟩s", this, v);
        } else throw new DomainError("⥊: shape contained "+v.humanType(true), this, v);
      }
    }
    
    if (emptyPos != null) {
      if (x.ia % ia != 0) {
        StringBuilder b = new StringBuilder();
        for (Value v : w) b.append(v).append(' ');
        b.deleteCharAt(b.length()-1);
        throw new LengthError("⥊: empty dimension not perfect (𝕨 ≡ "+b+"; "+(x.ia)+" = ≢𝕩)", this, x);
      }
      sh[emptyPos] = x.ia/ia;
      return x.ofShape(sh);
    } else if (ia == x.ia) return x.ofShape(sh);
    
    if (x.ia == 0) {
      return new SingleItemArr(x.prototype(), sh);
      
    } else if (x.scalar()) {
      return new SingleItemArr(x.first(), sh);
      
    } else if (x instanceof BitArr) {
      if (sh.length == 0 && !Main.enclosePrimitives) return x.get(0);
      BitArr wb = (BitArr) x;
      BitArr.BA res = new BitArr.BA(sh);
      int full = ia/wb.ia;
      int frac = ia%wb.ia;
      for (int i = 0; i < full; i++) res.add(wb);
      res.add(wb, 0, frac);
      return res.finish();
    } else if (x.quickDoubleArr()) {
      assert !(x instanceof Primitive);
      if (sh.length == 0 && !Main.enclosePrimitives) return x.get(0);
      double[] inp = x.asDoubleArr();
      double[] res = new double[ia];
      int p = 0;
      for (int i = 0; i < ia; i++) {
        res[i] = inp[p++];
        if (p == x.ia) p = 0;
      }
      return new DoubleArr(res, sh);
    } else if (x instanceof ChrArr) {
      if (sh.length == 0 && !Main.enclosePrimitives) return x.get(0);
      String inp = ((ChrArr) x).s;
      char[] res = new char[ia];
      int p = 0;
      for (int i = 0; i < ia; i++) {
        res[i] = inp.charAt(p++);
        if (p == x.ia) p = 0;
      }
      return new ChrArr(res, sh);
    } else {
      if (sh.length == 0 && x.first() instanceof Primitive && !Main.enclosePrimitives) return x.get(0);
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
    Value v = o instanceof Fun? ((Fun) o).call(call(w, x)) : o;
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
    Value v = o instanceof Fun? ((Fun) o).call(call(x)) : o;
    if (v.ia != x.ia) throw new DomainError("⌾⥊ expected equal amount of output & output items", this);
    return v.ofShape(x.shape);
  }
  
}
