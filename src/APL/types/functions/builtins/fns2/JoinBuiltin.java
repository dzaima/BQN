package APL.types.functions.builtins.fns2;

import APL.Main;
import APL.errors.*;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.functions.Builtin;

import java.util.Arrays;

public class JoinBuiltin extends Builtin {
  
  public String repr() {
    return "∾";
  }
  
  public Value call(Value w) {
    throw new NYIError("TODO monadic ∾", this, w);
  }
  
  public Value call(Value a, Value w) {
    return cat(a, w, 0, this);
  }
  
  
  
  
  
  
  
  private static BitArr catBit(Value a, Value w, int[] sh) { // for ravel concatenating
    boolean ab = a instanceof BitArr;
    boolean wb = w instanceof BitArr;
    
    BitArr.BA res = new BitArr.BA(sh);
    if (ab) res.add((BitArr) a);
    else    res.add(Main.bool(a));
    if (wb) res.add((BitArr) w);
    else    res.add(Main.bool(w));
    
    return res.finish();
  }
  
  public static Value cat(Value a, Value w, int k, Callable blame) {
    quick: if (k==0 && a.rank==w.rank && a.rank>0) {
      int[] sh = new int[a.rank];
      for (int i = 1; i < a.shape.length; i++) {
        if (a.shape[i]!=w.shape[i]) break quick; // leave proper checks for proper code
        sh[i] = a.shape[i];
      }
      sh[0] = a.shape[0]+w.shape[0];
      
      if ((a instanceof BitArr || Main.isBool(a))
        && (w instanceof BitArr || Main.isBool(w))) {
        return catBit(a, w, sh);
      }
      if (a instanceof DoubleArr && w instanceof DoubleArr) {
        double[] r = new double[a.ia + w.ia];
        System.arraycopy(a.asDoubleArr(), 0, r, 0, a.ia);
        System.arraycopy(w.asDoubleArr(), 0, r, a.ia, w.ia);
        return new DoubleArr(r, sh);
      }
      Value[] r = new Value[a.ia + w.ia];
      System.arraycopy(a.values(), 0, r, 0, a.ia);
      System.arraycopy(w.values(), 0, r, a.ia, w.ia);
      return Arr.create(r);
    }
    boolean aScalar = a.scalar(), wScalar = w.scalar();
    if (aScalar && wScalar) return cat(new Shape1Arr(a.first()), w, 0, blame);
    if (!aScalar && !wScalar) {
      if (a.rank != w.rank) throw new RankError("ranks not matchable", blame, w);
      for (int i = 0; i < a.rank; i++) {
        if (i != k && a.shape[i] != w.shape[i]) throw new LengthError("lengths not matchable ("+new DoubleArr(a.shape)+" vs "+new DoubleArr(w.shape)+")", blame, w);
      }
    }
    int[] rs = !aScalar ? a.shape.clone() : w.shape.clone(); // shape of the result
    rs[k] += aScalar || wScalar ? 1 : w.shape[k];
    int n0 = 1; for (int i = 0; i < k; i++) n0 *= rs[i];             // product of major dimensions
    int n1 = rs[k];                                                  // dimension to catenate on
    int n2 = 1; for (int i = k + 1; i < rs.length; i++) n2 *= rs[i]; // product of minor dimensions
    int ad = aScalar ? n2 : a.shape[k] * n2;                         // chunk size for ⍺
    int wd = wScalar ? n2 : w.shape[k] * n2;                         // chunk size for ⍵
    
    if (a.quickDoubleArr() && w.quickDoubleArr()) {
      double[] rv = new double[n0 * n1 * n2];                            // result values
      copyChunksD(aScalar, a.asDoubleArr(), rv,  0, ad, ad + wd);
      copyChunksD(wScalar, w.asDoubleArr(), rv, ad, wd, ad + wd);
      return new DoubleArr(rv, rs);
    } else {
      Value[] rv = new Value[n0 * n1 * n2];                            // result values
      copyChunks(aScalar, a.values(), rv, 0, ad, ad + wd);
      copyChunks(wScalar, w.values(), rv, ad, wd, ad + wd);
      return Arr.create(rv, rs);
    }
  }
  
  private static void copyChunks(boolean scalar, Value[] av, Value[] rv, int offset, int ad, int rd) {
    if (scalar) {
      for (int i = offset; i < rv.length; i += rd) {
        Arrays.fill(rv, i, i + ad, av[0]);
      }
    } else {
      for (int i = offset, j = 0; i < rv.length; i += rd, j += ad) { // i:position in rv, j:position in av
        System.arraycopy(av, j, rv, i, ad);
      }
    }
  }
  
  private static void copyChunksD(boolean scalar, double[] av, double[] rv, int offset, int ad, int rd) {
    if (scalar) {
      for (int i = offset; i < rv.length; i += rd) {
        Arrays.fill(rv, i, i + ad, av[0]);
      }
    } else {
      for (int i = offset, j = 0; i < rv.length; i += rd, j += ad) { // i:position in rv, j:position in av
        System.arraycopy(av, j, rv, i, ad);
      }
    }
  }
}
