package APL.types.functions.builtins.fns2;

import APL.errors.DomainError;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.functions.Builtin;

import java.util.*;

public class GroupBuiltin extends Builtin {
  public String repr() {
    return "⊔";
  }
  
  
  private static class MutDA {
    double[] ds = new double[2];
    int sz;
    void add(double i) {
      if (sz>= ds.length) {
        ds = Arrays.copyOf(ds, ds.length*2);
      }
      ds[sz] = i;
      sz++;
    }
  }
  
  public Value call(Value w) {
    if (w.rank != 1) throw new DomainError("⊔: rank of \uD835\uDD69 should be 1 (was "+w.rank+")", this, w);
    int[] wi = w.asIntVec();
    int sz = 0;
    for (int d : wi) sz = Math.max(sz, d);
    sz++;
    
    MutDA[] ds = new MutDA[sz];
    for (int i = 0; i < sz; i++) ds[i] = new MutDA();
    for (int i = 0; i < wi.length; i++) {
      int c = wi[i];
      if (c>=0) ds[c].add(i);
      else if (c!=-1) throw new DomainError("⊔: didn't expect "+c+" in \uD835\uDD68", this, w);
    }
    Value[] res = new Value[sz];
    for (int i = 0; i < sz; i++) {
      res[i] = new DoubleArr(Arrays.copyOf(ds[i].ds, ds[i].sz));
    }
    return new HArr(res);
  }
  
  
  
  @SuppressWarnings("unchecked") // no. bad java. bad.
  public Value call(Value a, Value w) {
    int[] poss = a.asIntVec();
    int sz = 0;
    for (int i : poss) sz = Math.max(sz, i);
    sz++;
    ArrayList<Value>[] vs = new ArrayList[sz];
    for (int i = 0; i < sz; i++) {
      vs[i] = new ArrayList<>();
    }
    for (int i = 0; i < w.ia; i++) {
      int c = poss[i];
      if (c>=0) {
        vs[c].add(w.get(i));
      } else if (c!=-1) throw new DomainError("⊔: didn't expect "+c+" in \uD835\uDD68", this, a);
    }
    Value[] res = new Value[sz];
    for (int i = 0; i < sz; i++) {
      res[i] = Arr.create(vs[i].toArray(new Value[0]));
    }
    return new HArr(res);
  }
}
