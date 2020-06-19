package APL.types.functions;

import APL.*;
import APL.errors.LengthError;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.functions.builtins.SetBuiltin;

import java.util.ArrayList;

public class MutArr extends Settable { // old version of SettableArr
  public final ArrayList<Obj> arr;
  public final int ia;
  public MutArr(ArrayList<Obj> arr) {
    super(null);
    ia = arr.size();
    if (arr.size() > 0) this.token = arr.get(0).token;
    this.arr = arr;
  }
  
  public Arr get() {
    if (this.token != null) Main.faulty = this;
    Value[] res = new Value[arr.size()];
    for (int i = 0; i < ia; i++) {
      Obj c = arr.get(i);
      res[i] = c instanceof MutArr? ((MutArr) c).get() : (Value) (c instanceof Value? c : ((Settable) c).get());
    }
    return Arr.create(res);
  }
  
  @Override
  public Type type() {
    return Type.array;
  }
  
  
  @Override
  public String toString() {
    if (Main.debug) return "vararr:"+arr;
    return get().toString();
  }
  
  
  public static Obj of(ArrayList<Obj> vs) {
    int sz = vs.size();
    if (sz == 0) return EmptyArr.SHAPE0Q;
    Obj fst = vs.get(0);
    if (fst instanceof Num) {
      if (((Num) fst).num == 0 || ((Num) fst).num == 1) {
        BitArr.BA bc = new BitArr.BA(sz);
        for (Obj c : vs) {
          if (c instanceof Num) {
            double n = ((Num) c).num;
            if (Double.doubleToRawLongBits(n)==0 || n == 1) { // don't convert negative zero!
              bc.add(n == 1);
            } else { bc = null; break; }
          } else { bc = null; break; }
        }
        if (bc != null) return bc.finish();
      }
      double[] a = new double[sz];
      int i = 0;
      while (i < a.length) {
        Obj c = vs.get(i);
        if (c instanceof Num) {
          a[i] = ((Num) c).num;
          i++;
        } else {
          a = null;
          break;
        }
      }
      if (a != null) return new DoubleArr(a);
    } else if (fst instanceof Char) {
      String s = "";
      for (Obj c : vs) {
        if (c instanceof Char) {
          s += ((Char) c).chr;
        } else {
          s = null;
          break;
        }
      }
      if (s != null) return new ChrArr(s);
    }
    return new MutArr(vs);
  }
  
  public void set(Value w, boolean update, Callable blame) {
    if (w.rank == 0) {
      for (int i = 0; i < ia; i++) {
        SetBuiltin.set(arr.get(i), w, update);
      }
    } else {
      if (w.rank != 1) throw new LengthError((update?'↩':'←')+": scatter rank ≠1", w);
      if (w.ia != ia) throw new LengthError((update?'↩':'←')+": scatter argument lengths not equal", w);
      for (int i = 0; i < ia; i++) {
        SetBuiltin.set(arr.get(i), w.get(i), update);
      }
    }
  }
}