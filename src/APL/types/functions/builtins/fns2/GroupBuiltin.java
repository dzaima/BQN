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
      if (sz >= ds.length) {
        ds = Arrays.copyOf(ds, ds.length*2);
      }
      ds[sz] = i;
      sz++;
    }
  }
  
  public Value call(Value x) {
    if (x.rank != 1) throw new DomainError("⊔: rank of 𝕩 should be 1 (was "+x.rank+")", this, x);
    int[] xi = x.asIntVec();
    int sz = 0;
    for (int d : xi) sz = Math.max(sz, d);
    sz++;
    
    MutDA[] ds = new MutDA[sz];
    for (int i = 0; i < sz; i++) ds[i] = new MutDA();
    for (int i = 0; i < xi.length; i++) {
      int c = xi[i];
      if (c>=0) ds[c].add(i);
      else if (c!=-1) throw new DomainError("⊔: didn't expect "+c+" in 𝕨", this, x);
    }
    Value[] res = new Value[sz];
    for (int i = 0; i < sz; i++) {
      res[i] = new DoubleArr(Arrays.copyOf(ds[i].ds, ds[i].sz));
    }
    return new HArr(res);
  }
  
  
  
  @SuppressWarnings("unchecked") // no. bad java. bad.
  public Value call(Value w, Value x) {
    int[] poss = w.asIntVec();
    int sz = 0;
    for (int i : poss) sz = Math.max(sz, i);
    sz++;
    ArrayList<Value>[] vs = new ArrayList[sz];
    for (int i = 0; i < sz; i++) {
      vs[i] = new ArrayList<>();
    }
    for (int i = 0; i < x.ia; i++) {
      int c = poss[i];
      if (c>=0) {
        vs[c].add(x.get(i));
      } else if (c!=-1) throw new DomainError("⊔: didn't expect "+c+" in 𝕨", this, w);
    }
    Value[] res = new Value[sz];
    for (int i = 0; i < sz; i++) {
      res[i] = Arr.create(vs[i].toArray(new Value[0]));
    }
    return new HArr(res);
  }
}