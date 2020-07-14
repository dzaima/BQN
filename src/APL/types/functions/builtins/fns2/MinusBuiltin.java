package APL.types.functions.builtins.fns2;

import APL.algs.Pervasion;
import APL.types.*;
import APL.types.arrs.IntArr;
import APL.types.functions.Builtin;

public class MinusBuiltin extends Builtin {
  @Override public String repr() {
    return "-";
  }
  
  
  
  public static final NumMV NF = new NumMV() {
    public Value call(Num x) {
      return Num.of(-x.num);
    }
    public void call(double[] res, double[] x) {
      for (int i = 0; i < x.length; i++) res[i] = -x[i];
    }
    public Value call(BigValue x) {
      return new BigValue(x.i.negate());
    }
  
    public Value call(int[] x, int[] sh) {
      int[] r = new int[x.length];
      for (int i = 0; i < r.length; i++) {
        int c = x[i];
        if (c==-c) return super.call(x, sh); // handles Integer.MIN_VALUE and 0
        r[i] = -c;
      }
      return new IntArr(r, sh);
    }
  };
  
  public Value call(Value x) {
    return numChrM(NF, Char::swap, x);
  }
  
  public Pervasion.NN2N dyNum() { return DF; };
  public static final Pervasion.NN2N DF = new Pervasion.NN2N() {
    public Value on(BigValue w, BigValue x) { return new BigValue(w.i.subtract(x.i)); }
    public double on(double w, double x) { return w - x; }
    public void on(double   w, double[] x, double[] res) { for (int i = 0; i < x.length; i++) res[i] = w    - x[i]; }
    public void on(double[] w, double   x, double[] res) { for (int i = 0; i < w.length; i++) res[i] = w[i] - x   ; }
    public void on(double[] w, double[] x, double[] res) { for (int i = 0; i < w.length; i++) res[i] = w[i] - x[i]; }
    
    public int[] on(int   w, int[] x) {try{int[]res=new int[x.length];for(int i=0;i<x.length;i++) {res[i]=Math.subtractExact(w   ,x[i]);}return res;}catch(ArithmeticException e){return null;}}
    public int[] on(int[] w, int   x) {try{int[]res=new int[w.length];for(int i=0;i<w.length;i++) {res[i]=Math.subtractExact(w[i],x   );}return res;}catch(ArithmeticException e){return null;}}
    public int[] on(int[] w, int[] x) {try{int[]res=new int[x.length];for(int i=0;i<x.length;i++) {res[i]=Math.subtractExact(w[i],x[i]);}return res;}catch(ArithmeticException e){return null;}}
  };
  
  public Value call(Value w, Value x) {
    return DF.call(w, x);
  }
  public Value callInv(Value x) { return call(x); }
  public Value callInvW(Value w, Value x) { return call(w, x); }
  public Value callInvA(Value w, Value x) {
    return PlusBuiltin.DF.call(w, x);
  }
}