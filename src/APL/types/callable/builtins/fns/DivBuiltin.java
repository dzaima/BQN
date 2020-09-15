package APL.types.callable.builtins.fns2;

import APL.errors.DomainError;
import APL.tools.Pervasion;
import APL.types.*;
import APL.types.callable.builtins.FnBuiltin;

public class DivBuiltin extends FnBuiltin {
  @Override public String repr() {
    return "÷";
  }
  
  
  
  private static final NumMV NF = new NumMV() {
    public Value call(Num x) {
      return new Num(1/x.num);
    }
    public void call(double[] res, double[] x) {
      for (int i = 0; i < x.length; i++) res[i] = 1/x[i];
    }
    public Value call(BigValue x) {
      throw new DomainError("reciprocal of biginteger", x);
    }
  };
  public Value call(Value x) {
    return numM(NF, x);
  }
  
  public Pervasion.NN2N dyNum() { return DF; }
  @SuppressWarnings("UnnecessaryLocalVariable") // prettier this way
  public static final Pervasion.NN2N DF = new Pervasion.NN2N() {
    public Value on(BigValue w, BigValue x) { return new BigValue(w.i.divide(x.i)); }
    public double on(double w, double x) { return w / x; }
    public void on(double   w, double[] x, double[] res) { for (int i = 0; i < x.length; i++) res[i] = w    / x[i]; }
    public void on(double[] w, double   x, double[] res) { for (int i = 0; i < w.length; i++) res[i] = w[i] / x   ; }
    public void on(double[] w, double[] x, double[] res) { for (int i = 0; i < w.length; i++) res[i] = w[i] / x[i]; }
    
                                       // ↓ x=0 for ∞; x=¯1 for (-2*31)÷¯1
    public int[] on(int   w, int[] x) {                     int[]res=new int[x.length];for(int i=0;i<x.length;i++) {int cw=w   ,cx=x[i];if(cx==-cx)return null;int r=cw/cx,m=cw%cx;res[i]=r;if(m!=0)return null;}return res; }
    public int[] on(int[] w, int   x) {if(x==-x)return null;int[]res=new int[w.length];for(int i=0;i<w.length;i++) {int cw=w[i],cx=x   ;                       int r=cw/cx,m=cw%cx;res[i]=r;if(m!=0)return null;}return res; }
    public int[] on(int[] w, int[] x) {                     int[]res=new int[x.length];for(int i=0;i<x.length;i++) {int cw=w[i],cx=x[i];if(cx==-cx)return null;int r=cw/cx,m=cw%cx;res[i]=r;if(m!=0)return null;}return res; }
    // public int[] on(int   w, int[] x) {int[]res=new int[x.length];for(int i=0;i<x.length;i++) {int cw=w   ,cx=x[i],r=cw/cx;res[i]=r;if(r*cx!=cw)return null;}return res;}
    // public int[] on(int[] w, int   x) {int[]res=new int[w.length];for(int i=0;i<w.length;i++) {int cw=w[i],cx=x   ,r=cw/cx;res[i]=r;if(r*cx!=cw)return null;}return res;}
    // public int[] on(int[] w, int[] x) {int[]res=new int[x.length];for(int i=0;i<x.length;i++) {int cw=w[i],cx=x[i],r=cw/cx;res[i]=r;if(r*cx!=cw)return null;}return res;}
  };
  
  public Value call(Value w, Value x) {
    return DF.call(w, x);
  }
  
  public Value callInv(Value x) { return call(x); }
  public Value callInvX(Value w, Value x) { return call(w, x); }
  
  public Value callInvW(Value w, Value x) {
    return MulBuiltin.DF.call(w, x);
  }
}