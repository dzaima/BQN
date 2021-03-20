package BQN.types.callable.builtins.fns;

import BQN.errors.DomainError;
import BQN.tools.*;
import BQN.types.*;
import BQN.types.arrs.IntArr;
import BQN.types.callable.builtins.FnBuiltin;

import java.math.BigInteger;

public class StileBuiltin extends FnBuiltin {
  public String ln(FmtInfo f) { return "|"; }
  
  private static final NumMV NF = new NumMV() {
    public Value call(Num x) {
      return x.num<0? Num.of(-x.num) : x;
    }
    public void call(double[] res, double[] x) {
      for (int i = 0; i < x.length; i++) res[i] = Math.abs(x[i]);
    }
    public Value call(BigValue x) {
      return new BigValue(x.i.abs());
    }
  
    public Value call(int[] x, int[] sh) {
      int[] res = new int[x.length];
      for (int i = 0; i < res.length; i++) {
        int c = x[i];
        if (c==Integer.MIN_VALUE) return super.call(x, sh);
        res[i] = Math.abs(c);
      }
      return new IntArr(res, sh);
    }
  };
  
  public Value call(Value x) {
    return numChrMapM(NF, c->{ throw new DomainError("|char", this); }, c -> Num.of(c.size()), x);
  }
  
  
  
  public Pervasion.NN2N dyNum() { return DF; }
  public static final Pervasion.NN2N DF = new Pervasion.NN2N() {
    public Value on(BigValue w, BigValue x) {
      BigInteger r = x.i.remainder(w.i);
      if (r.signum()!=0  &  (w.i.signum()>=0 ^ x.i.signum()>=0)) r = r.add(w.i);
      return new BigValue(r);
    }
    public double on(double w, double x) {
      double r=x%w;
      if (r!=0  &  (w>=0 ^ x>=0)) r+= w;
      return r;
    }
    public void on(double   w, double[] x, double[] res) { for(int i=0; i<x.length; i++) { double cw=w   ,cx=x[i],r=cx%cw;if (r!=0 & (cw>=0^cx>=0))r+= cw; res[i]=r; } }
    public void on(double[] w, double   x, double[] res) { for(int i=0; i<w.length; i++) { double cw=w[i],cx=x   ,r=cx%cw;if (r!=0 & (cw>=0^cx>=0))r+= cw; res[i]=r; } }
    public void on(double[] w, double[] x, double[] res) { for(int i=0; i<w.length; i++) { double cw=w[i],cx=x[i],r=cx%cw;if (r!=0 & (cw>=0^cx>=0))r+= cw; res[i]=r; } }
    
    public int[] on(int   w, int[] x) {int[]res=new int[x.length];for(int i=0;i<x.length;i++) {int cw=w   ,cx=x[i];if(cw==-cw)return null;int r=cx%cw;if (r!=0 & (cw>=0^cx>=0))r+= cw; res[i]=r;}return res;}
    public int[] on(int[] w, int   x) {int[]res=new int[w.length];for(int i=0;i<w.length;i++) {int cw=w[i],cx=x   ;if(cw==-cw)return null;int r=cx%cw;if (r!=0 & (cw>=0^cx>=0))r+= cw; res[i]=r;}return res;}
    public int[] on(int[] w, int[] x) {int[]res=new int[x.length];for(int i=0;i<x.length;i++) {int cw=w[i],cx=x[i];if(cw==-cw)return null;int r=cx%cw;if (r!=0 & (cw>=0^cx>=0))r+= cw; res[i]=r;}return res;}
  };
  public Value call(Value w, Value x) {
    return DF.call(w, x);
  }
  
  
  
  private static final Pervasion.NN2N CPY_SGN = new Pervasion.NN2N() {
    public double on(double o, double n) {
      if (o==0 && n!=0) throw new DomainError("⌾|: cannot add sign to "+n+" as original was 0");
      return o<0? -n : n;
    }
    public void on(double o, double[] n, double[] res) {
      for (int i = 0; i < res.length; i++) {
        double nc = n[i];
        if (o==0 && nc!=0) throw new DomainError("⌾|: cannot add sign to "+nc+" as original was 0");
        res[i] = o<0? -nc : nc;
      }
    }
    public void on(double[] o, double n, double[] res) {
      for (int i = 0; i < res.length; i++) {
        double oc = o[i];
        if (oc==0 && n!=0) throw new DomainError("⌾|: cannot add sign to "+n+" as original was 0");
        res[i] = oc<0? -n : n;
      }
    }
    public void on(double[] o, double[] n, double[] res) {
      for (int i = 0; i < res.length; i++) {
        double oc = o[i];
        double nc = n[i];
        if (oc==0 && nc!=0) throw new DomainError("⌾|: cannot add sign to "+nc+" as original was 0");
        res[i] = oc<0? -nc : nc;
      }
    }
    public Value on(BigValue o, BigValue n) {
      BigInteger oi = o.i;
      BigInteger ni = n.i;
      if (oi.signum()==0 && ni.signum()!=0) throw new DomainError("⌾|: cannot add sign to "+ni+" as original was 0");
      return oi.signum()<0? new BigValue(ni.negate()) : n;
    }
  };
  public Value under(Value o, Value x) {
    Value v = o.call(call(x));
    return CPY_SGN.call(x, v);
  }
}