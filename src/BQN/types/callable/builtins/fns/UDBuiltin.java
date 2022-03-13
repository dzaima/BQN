package BQN.types.callable.builtins.fns;

import BQN.Main;
import BQN.errors.*;
import BQN.tools.*;
import BQN.types.*;
import BQN.types.arrs.*;
import BQN.types.callable.builtins.FnBuiltin;

public class UDBuiltin extends FnBuiltin {
  public String ln(FmtInfo f) { return "↕"; }
  
  public Value call(Value x) {
    return on(x, this);
  }
  public static Value on(Value x, Callable blame) {
    if (x instanceof Primitive) {
      int n = x.asInt();
      if (n<0) throw new DomainError(blame+": negative argument", blame);
      if (x instanceof Num) {
        return new IntArr(on(n));
      } else if (x instanceof BigValue) {
        Value[] res = new Value[n];
        for (int i = 0; i < res.length; i++) {
          res[i] = new BigValue(i);
        }
        return new HArr(res);
      }
    }
    if (x.r()!=1) throw new DomainError(blame+": argument must be an atom or vector ("+Main.fArr(x.shape)+" ≡ ≢𝕩)", blame);
    if (Main.vind) { // •VI←1
      int dim = x.ia;
      int[] shape = x.asIntVec();
      int prod = Arr.prod(shape);
      Value[] res = new Value[dim];
      int blockSize = 1;
      for (int i = dim-1; i >= 0; i--) {
        double[] ds = new double[prod];
        int len = shape[i];
        int csz = 0;
        double val = 0;
        for (int k = 0; k < len; k++) {
          for (int l = 0; l < blockSize; l++) ds[csz++] = val;
          val++;
        }
        int j = csz;
        while (j < prod) {
          System.arraycopy(ds, 0, ds, j, csz);
          j+= csz;
        }
        res[i] = new DoubleArr(ds, shape);
        blockSize*= shape[i];
      }
      return new HArr(res);
    } else { // •VI←0
      int[] sh = x.asIntArr();
      for (int c : sh) {
        if (c<0) throw new DomainError(blame+": didn't expect negative numbers in argument", blame);
      }
      long iat = 1;
      for (int c : sh) {
        iat*= c;
        if (iat>Integer.MAX_VALUE) throw new DomainError(blame+": argument too large", blame);
      }
      int ia = (int)iat;
      Value[] res = new Value[ia];
      int i = 0;
      for (int[] c : new Indexer(sh)) {
        res[i] = new IntArr(c.clone());
        i++;
      }
      if (res.length==0) return new EmptyArr(sh, new SingleItemArr(Num.ZERO, Arr.vecsh(sh.length)));
      return new HArr(res, sh);
    }
  }
  public static int[] on(int am) {
    int[] res = new int[am];
    for (int i = 0; i < am; i++) res[i] = i;
    return res;
  }
  
  public Value call(Value w, Value x) {
    int[] wsh = w.asIntVec();
    int wr = wsh.length;
    if (wr == 0) return x;
    int xr = x.r();
    if (wr>xr) throw new RankError("↕: length of 𝕨 must be less than or equal to rank of 𝕩 ("+wr+" ≡ ≠𝕨, "+Main.fArr(x.shape)+" ≡ ≢𝕩)", this);

    int[] sh = new int[wr+xr];
    int ia = 1;
    for (int i = 0; i < wr; i++) {
      int wl = wsh[i];
      if (wl<0) throw new LengthError("↕: negative entry in 𝕨 ("+wl+" ≡ "+i+"⊑𝕨)", this);
      ia *= sh[wr+i] = wl;
      ia *= sh[i] = 1 + x.shape[i] - wl;
      if (sh[i]<0) throw new LengthError("↕: window length can be at most 1 more than existing shape ("+wl+" ≡ "+i+"⊑𝕨, "+x.shape[i]+" ≡ "+i+"⊑≢𝕩)", this);
    }
    for (int i = wr; i < xr; i++) {
      ia *= sh[wr+i] = x.shape[i];
    }

    Value[] xv = x.values();
    Value[] res = new Value[ia];
    for (int[] c : new Indexer(sh)) {
      int[] d = new int[xr];
      for (int i = 0; i < wr; i++) {
        d[i] = c[i] + c[wr+i];
      }
      System.arraycopy(d, wr, c, 2*wr, xr-wr);
      res[Indexer.fromShape(sh, c)] = x.simpleAt(d);
    }
    return Arr.create(res, sh);
  }
}
