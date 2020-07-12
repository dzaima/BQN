package APL.types.functions.builtins.fns2;

import APL.Main;
import APL.errors.DomainError;
import APL.types.*;
import APL.types.arrs.BitArr;
import APL.types.functions.Builtin;

import java.math.BigInteger;

public class MulBuiltin extends Builtin {
  public String repr() {
    return "×";
  }
  
  
  
  public Value identity() {
    return Num.ONE;
  }
  
  private static final NumMV NF = new NumMV() {
    public Value call(Num x) {
      double d = x.num;
      return d>0? Num.ONE : d<0? Num.MINUS_ONE : Num.ZERO;
    }
    public void call(double[] res, double[] x) {
      for (int i = 0; i < x.length; i++) res[i] = x[i]>0? 1 : x[i]<0? -1 : 0;
    }
    public Value call(BigValue x) {
      return Num.of(x.i.signum());
    }
  };
  public Value call(Value x) {
    return numChrMapM(NF, c -> Num.of(c.getCase()), c -> c.size()>0? Num.ONE : Num.ZERO, x);
  }
  
  public static final Pervasion.NN2N DF = new Pervasion.NN2NpB() {
    public Value on(BigValue w, BigValue x) { return new BigValue(w.i.multiply(x.i)); }
    public double on(double w, double x) { return w * x; }
    public void on(double   w, double[] x, double[] res) { for (int i = 0; i < x.length; i++) res[i] = w    * x[i]; }
    public void on(double[] w, double   x, double[] res) { for (int i = 0; i < w.length; i++) res[i] = w[i] * x   ; }
    public void on(double[] w, double[] x, double[] res) { for (int i = 0; i < w.length; i++) res[i] = w[i] * x[i]; }
    
    public int[] on(int   w, int[] x) {try{int[]res=new int[x.length];for(int i=0;i<x.length;i++) {int cw=w   ,cx=x[i];res[i]=Math.multiplyExact(cw,cx);}return res;}catch(ArithmeticException e){return null;}}
    public int[] on(int[] w, int   x) {try{int[]res=new int[w.length];for(int i=0;i<w.length;i++) {int cw=w[i],cx=x   ;res[i]=Math.multiplyExact(cw,cx);}return res;}catch(ArithmeticException e){return null;}}
    public int[] on(int[] w, int[] x) {try{int[]res=new int[x.length];for(int i=0;i<x.length;i++) {int cw=w[i],cx=x[i];res[i]=Math.multiplyExact(cw,cx);}return res;}catch(ArithmeticException e){return null;}}
    
    public Value on(boolean w, BitArr x) { return w? x : BitArr.fill(x, false); }
    public Value on(BitArr w, boolean x) { return x? w : BitArr.fill(w, false); }
    public Value on(BitArr w, BitArr x) {
      BitArr.BC bc = new BitArr.BC(w.shape);
      for (int i = 0; i < w.arr.length; i++) bc.arr[i] = w.arr[i] & x.arr[i];
      return bc.finish();
    }
  };
  
  public Value call(Value w, Value x) {
    return DF.call(w, x);
  }
  public Value callInvW(Value w, Value x) {
    try {
      return new DivBuiltin().call(x, w);
    } catch (DomainError e) {
      throw new DomainError(e.getMessage(), this, e.cause);
    }
  }
  public Value callInvA(Value w, Value x) {
    return callInvW(x, w);
  }
  
  
  private static final D_NNeN SET_SGN = new D_NNeN() {
    public double on(double w, double x) {
      if (x==0) return 0;
      if (w==0) throw new DomainError("⌾×: cannot set sign of 0 to "+Num.format(x));
      if (x== 1) return  Math.abs(w);
      if (x==-1) return -Math.abs(w);
      throw new DomainError("⌾×: cannot set sign to "+x);
    }
    public void on(double[] res, double w, double[] x) {
      for (int i = 0; i < res.length; i++) {
        double nc = x[i];
        if (w==0 && nc!=0) throw new DomainError("⌾×: cannot set sign of 0 to "+Num.format(nc));
        if (nc==0 || nc==1 || nc==-1) res[i] = Math.abs(w)*nc;
        else throw new DomainError("⌾×: cannot set sign to "+nc);
      }
    }
    public void on(double[] res, double[] w, double x) {
      for (int i = 0; i < res.length; i++) {
        double oc = w[i];
        if (oc==0 && x!=0) throw new DomainError("⌾×: cannot set sign of 0 to "+Num.format(x));
        if (x==0 || x==1 || x==-1) res[i] = Math.abs(oc)*x;
        else throw new DomainError("⌾×: cannot set sign to "+x);
      }
    }
    public void on(double[] res, double[] w, double[] x) {
      for (int i = 0; i < res.length; i++) {
        double oc = w[i];
        double nc = x[i];
        if (oc==0 && nc!=0) throw new DomainError("⌾×: cannot set sign of 0 to "+Num.format(nc));
        if (nc==0 || nc==1 || nc==-1) res[i] = Math.abs(oc)*nc;
        else throw new DomainError("⌾×: cannot set sign to "+nc);
      }
    }
    public Value call(BigValue w, BigValue x) {
      BigInteger oi = w.i;
      int ni = BigValue.safeInt(x.i);
      if (oi.signum()==0 && ni!=0) throw new DomainError("⌾×: cannot set sign of 0 to "+ni);
      if (ni== 0) return BigValue.ZERO;
      boolean neg = oi.signum() == -1;
      if (ni== 1 ^ neg) return w;
      if (ni==-1 ^ neg) return new BigValue(oi.negate());
      else throw new DomainError("⌾×: cannot set sign to "+ni);
    }
  };
  public Value under(Value o, Value x) {
    Main.faulty = this;
    Value v = o instanceof Fun? ((Fun) o).call(call(x)) : o;
    return numD(SET_SGN, x, v);
  }
}