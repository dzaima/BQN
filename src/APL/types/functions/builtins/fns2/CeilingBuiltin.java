package APL.types.functions.builtins.fns2;

import APL.types.*;
import APL.types.arrs.*;
import APL.types.functions.Builtin;

public class CeilingBuiltin extends Builtin {
  @Override public String repr() {
    return "⌈";
  }
  
  
  
  public Value identity() {
    return Num.NEGINF;
  }
  
  private static final NumMV NF = new NumMV() {
    public Value call(Num x) {
      return Num.of(Math.ceil(x.num));
    }
    public void call(double[] res, double[] x) {
      for (int i = 0; i < x.length; i++) res[i] = Math.ceil(x[i]);
    }
    public Value call(int[] x, int[] sh) {
      return new IntArr(x, sh);
    }
  };
  public Value call(Value x) {
    return numChrM(NF, Char::upper, x);
  }
  
  public Pervasion.NN2N dyNum() { return DF; };
  public static final Pervasion.NN2NpB DF = new Pervasion.NN2NpB() {
    public Value on(BigValue w, BigValue x) { return w.i.compareTo(x.i)>0? w : x; }
    public double on(double w, double x) { return Math.max(w, x); }
    public void on(double   w, double[] x, double[] res) { for (int i = 0; i < x.length; i++) res[i] = Math.max(w   , x[i]); }
    public void on(double[] w, double   x, double[] res) { for (int i = 0; i < w.length; i++) res[i] = Math.max(w[i], x   ); }
    public void on(double[] w, double[] x, double[] res) { for (int i = 0; i < w.length; i++) res[i] = Math.max(w[i], x[i]); }
    
    public int[] on(int   w, int[] x) {int[]res=new int[x.length]; for(int i=0;i<x.length;i++) {res[i]=Math.max(w   ,x[i]);}return res;}
    public int[] on(int[] w, int   x) {int[]res=new int[w.length]; for(int i=0;i<w.length;i++) {res[i]=Math.max(w[i],x   );}return res;}
    public int[] on(int[] w, int[] x) {int[]res=new int[x.length]; for(int i=0;i<x.length;i++) {res[i]=Math.max(w[i],x[i]);}return res;}
    
    // OrBuiltin.DF
    public Value on(boolean w, BitArr x) { return w? BitArr.fill(x, true) : x; }
    public Value on(BitArr w, boolean x) { return x? BitArr.fill(w, true) : w; }
    public Value on(BitArr w, BitArr x) {
      BitArr.BC bc = new BitArr.BC(w.shape);
      for (int i = 0; i < w.arr.length; i++) bc.arr[i] = w.arr[i] | x.arr[i];
      return bc.finish();
    }
  };
  public Value call(Value w, Value x) {
    return DF.call(w, x);
  }
}