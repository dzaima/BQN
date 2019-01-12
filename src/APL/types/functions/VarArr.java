package APL.types.functions;

import APL.*;
import APL.types.*;
import APL.types.arrs.*;

import java.util.*;

public class VarArr extends Obj {
  public final ArrayList<Obj> arr;
  public final int ia;
  public VarArr(ArrayList<Obj> arr) {
    ia = arr.size();
    if (arr.size() > 0) this.token = arr.get(0).token;
    Collections.reverse(arr);
    this.arr = arr;
  }
  
  public Arr materialize() {
    if (this.token != null) Main.faulty = this;
    Value[] res = new Value[arr.size()];
    for (int i = 0; i < ia; i++) {
      Obj c = arr.get(i);
      res[i] = c instanceof VarArr? ((VarArr) c).materialize() : (Value) (c instanceof Value? c : ((Settable) c).get());
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
    return materialize().toString();
  }
  
  public static Obj of (ArrayList<Obj> vs) {
    if (vs.size() == 0) return EmptyArr.SHAPE0;
    if (vs.get(0) instanceof Num) {
      double[] a = new double[vs.size()];
      int i = 0;
      while (i < a.length) {
        Obj c = vs.get(i);
        if (c instanceof Num) {
          a[a.length-i-1] = ((Num) c).num;
          i++;
        } else {
          a = null;
          break;
        }
      }
      if (a != null) return new DoubleArr(a);
    } else {
      String s = "";
      int i = vs.size()-1;
      while (i >= 0) {
        Obj c = vs.get(i);
        if (c instanceof Char) {
          s+= ((Char) c).chr;
          i--;
        } else {
          s = null;
          break;
        }
      }
      if (s != null) return new ChrArr(s);
    }
    return new VarArr(vs);
  }
  
}
