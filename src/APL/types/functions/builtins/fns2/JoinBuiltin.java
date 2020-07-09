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
  
  public Value call(Value x) {
    if (x.rank == 1) {
      Value joined = JoinBuiltin.joinVec(x);
      if (joined != null) return joined;
    }
    throw new NYIError("monadic ∾ on rank>1", this, x);
  }
  
  public static Value joinVec(Value x) { // returns null if contents weren't rank 1
    assert x.rank == 1;
    if (x.ia == 0) return x;
    Value first = x.first();
    int am = 0;
    int chki = 0;
    typed: {
      if (first instanceof ChrArr) {
        for (Value v : x) {
          if (v.rank != 1) return null;
          if (!(v instanceof ChrArr)) break typed;
          am+= v.ia;
          chki++;
        }
        char[] cs = new char[am];
        int ri = 0;
        for (int i = 0; i < x.ia; i++) {
          Value v = x.get(i);
          String s = ((ChrArr) v).s;
          s.getChars(0, s.length(), cs, ri);
          ri+= s.length();
        }
        return Main.toAPL(new String(cs));
        
        
      } else if (first.quickDoubleArr()) {
        for (Value v : x) {
          if (v.rank != 1) return null;
          if (!v.quickDoubleArr()) break typed;
          am+= v.ia;
          chki++;
        }
        double[] ds = new double[am];
        
        int ri = 0;
        for (int i = 0; i < x.ia; i++) {
          Value v = x.get(i);
          System.arraycopy(v.asDoubleArr(), 0, ds, ri, v.ia);
          ri+= v.ia;
        }
        return new DoubleArr(ds);
      }
    }
    
    for (; chki < x.ia; chki++) {
      Value v = x.get(chki);
      if (v.rank != 1) return null;
      am+= v.ia;
    }
    
    Value[] vs = new Value[am];
    int ri = 0;
    for (Value v : x) {
      System.arraycopy(v.values(), 0, vs, ri, v.ia);
      ri+= v.ia;
    }
    return Arr.create(vs);
  }
  
  public Value call(Value w, Value x) {
    int a = w.rank, b = x.rank;
    int c = Math.max(1,Math.max(a,b));
    if (c-a > 1 || c-b > 1) throw new RankError("∾: argument ranks must differ by 1 or less (were "+a+" and "+b+")", this);
    
    int[] sh = new int[c];
    for (int i = 1; i < c; i++) {
      int s = x.shape[i+b-c];
      if (w.shape[i+a-c] != s) throw new LengthError("∾: lengths not matchable ("+new DoubleArr(w.shape)+" vs "+new DoubleArr(x.shape)+")", this);
      sh[i] = s;
    }
    sh[0] = (a==c? w.shape[0] : 1) + (b==c? x.shape[0] : 1);
    
    if ((w instanceof BitArr || Main.isBool(w))
      && (x instanceof BitArr || Main.isBool(x))) {
      return catBit(w, x, sh);
    }
    if (w instanceof DoubleArr && x instanceof DoubleArr) {
      double[] r = new double[w.ia + x.ia];
      System.arraycopy(w.asDoubleArr(), 0, r, 0, w.ia);
      System.arraycopy(x.asDoubleArr(), 0, r, w.ia, x.ia);
      return new DoubleArr(r, sh);
    }
    Value[] r = new Value[w.ia + x.ia];
    System.arraycopy(w.values(), 0, r, 0, w.ia);
    System.arraycopy(x.values(), 0, r, w.ia, x.ia);
    return Arr.create(r, sh);
  }
  
  
  
  
  
  
  
  private static BitArr catBit(Value w, Value x, int[] sh) { // for ravel concatenating
    boolean wb = w instanceof BitArr;
    boolean xb = x instanceof BitArr;
    
    BitArr.BA res = new BitArr.BA(sh);
    if (wb) res.add((BitArr) w);
    else    res.add(Main.bool(w));
    if (xb) res.add((BitArr) x);
    else    res.add(Main.bool(x));
    
    return res.finish();
  }
  
  public static Value cat(Value w, Value x, int k, Callable blame) {
    boolean wScalar = w.scalar(), xScalar = x.scalar();
    if (wScalar && xScalar) return cat(new Shape1Arr(w.first()), x, 0, blame);
    if (!wScalar && !xScalar) {
      if (w.rank != x.rank) throw new RankError("ranks not matchable", blame, x);
      for (int i = 0; i < w.rank; i++) {
        if (i != k && w.shape[i] != x.shape[i]) throw new LengthError("lengths not matchable ("+new DoubleArr(w.shape)+" vs "+new DoubleArr(x.shape)+")", blame, x);
      }
    }
    int[] rs = !wScalar? w.shape.clone() : x.shape.clone(); // shape of the result
    rs[k]+= wScalar || xScalar? 1 : x.shape[k];
    int n0 = 1; for (int i = 0; i < k; i++) n0*= rs[i];             // product of major dimensions
    int n1 = rs[k];                                                 // dimension to catenate on
    int n2 = 1; for (int i = k + 1; i < rs.length; i++) n2*= rs[i]; // product of minor dimensions
    int wd = wScalar? n2 : w.shape[k] * n2;                         // chunk size for 𝕨
    int xd = xScalar? n2 : x.shape[k] * n2;                         // chunk size for 𝕩
    
    if (w.quickDoubleArr() && x.quickDoubleArr()) {
      double[] rv = new double[n0 * n1 * n2];                            // result values
      copyChunksD(wScalar, w.asDoubleArr(), rv,  0, wd, wd + xd);
      copyChunksD(xScalar, x.asDoubleArr(), rv, wd, xd, wd + xd);
      return new DoubleArr(rv, rs);
    } else {
      Value[] rv = new Value[n0 * n1 * n2];                            // result values
      copyChunks(wScalar, w.values(), rv, 0, wd, wd + xd);
      copyChunks(xScalar, x.values(), rv, wd, xd, wd + xd);
      return Arr.create(rv, rs);
    }
  }
  
  private static void copyChunks(boolean scalar, Value[] av, Value[] rv, int offset, int ad, int rd) {
    if (scalar) {
      for (int i = offset; i < rv.length; i+= rd) {
        Arrays.fill(rv, i, i + ad, av[0]);
      }
    } else {
      for (int i = offset, j = 0; i < rv.length; i+= rd, j+= ad) { // i:position in rv, j:position in av
        System.arraycopy(av, j, rv, i, ad);
      }
    }
  }
  
  private static void copyChunksD(boolean scalar, double[] av, double[] rv, int offset, int ad, int rd) {
    if (scalar) {
      for (int i = offset; i < rv.length; i+= rd) {
        Arrays.fill(rv, i, i + ad, av[0]);
      }
    } else {
      for (int i = offset, j = 0; i < rv.length; i+= rd, j+= ad) { // i:position in rv, j:position in av
        System.arraycopy(av, j, rv, i, ad);
      }
    }
  }
  
  public Value under(Value o, Value x) {
    if (x.rank != 1) throw new NYIError("⌾∾ for rank>1", this, x); // doesn't work 
    Value joined = call(x);
    Value v = o instanceof Fun? ((Fun) o).call(joined) : o;
    Arr.eqShapes(joined.shape, v.shape, this);
    Value[] res = new Value[x.ia];
    Value[] vv = v.values();
    int oi = 0;
    int ii = 0;
    for (Value c : x) {
      Value[] cr = new Value[c.ia];
      System.arraycopy(vv, ii, cr, 0, cr.length);
      res[oi] = Arr.create(cr, c.shape);
      oi++; ii+= cr.length;
    }
    return new HArr(res, x.shape);
  }
}