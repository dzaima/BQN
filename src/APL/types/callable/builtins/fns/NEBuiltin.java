package APL.types.callable.builtins.fns;

import APL.tools.Pervasion;
import APL.types.*;
import APL.types.arrs.BitArr;
import APL.types.callable.builtins.FnBuiltin;


public class NEBuiltin extends FnBuiltin {
  @Override public String repr() {
    return "≠";
  }
  
  
  public Value call(Value x) {
    if (x.rank==0) return Num.ONE;
    return Num.of(x.shape[0]);
  }
  

  private static final Pervasion.NN2N DFn = new Pervasion.NN2N() { // purely for dyNum
    public double on(double w, double x) { return w==x? 0 : 1; }
    public void on(double   w, double[] x, double[] res) { for (int i = 0; i < x.length; i++) { res[i] = (w   !=x[i])?1:0; } }
    public void on(double[] w, double   x, double[] res) { for (int i = 0; i < w.length; i++) { res[i] = (w[i]!=x   )?1:0; } }
    public void on(double[] w, double[] x, double[] res) { for (int i = 0; i < w.length; i++) { res[i] = (w[i]!=x[i])?1:0; } }
  };
  public Pervasion.NN2N dyNum() { return DFn; }
  
  public static final Pervasion.VV2B DF = new Pervasion.VV2B() {
    public Value on(Primitive w, Primitive x) { return w.eq(x)? Num.ZERO : Num.ONE; }
    public void on(double   w, double[] x, BitArr.BA res) { for (double cx : x) { res.add( w!=cx); } }
    public void on(double[] w, double   x, BitArr.BA res) { for (double cw : w) { res.add(cw!= x); } }
    public void on(double[] w, double[] x, BitArr.BA res) { for (int i = 0; i < w.length; i++) { res.add(w[i]!=x[i]); } }
    
    public void on(int   w, int[] x, BitArr.BA res) { for (int cx : x) { res.add( w!=cx); } }
    public void on(int[] w, int   x, BitArr.BA res) { for (int cw : w) { res.add(cw!= x); } }
    public void on(int[] w, int[] x, BitArr.BA res) { for (int i = 0; i < w.length; i++) { res.add(w[i]!=x[i]); } }
    
    public void on(char   w, char[] x, BitArr.BA res) { for (char cx : x) { res.add( w!=cx); } }
    public void on(char[] w, char   x, BitArr.BA res) { for (char cw : w) { res.add(cw!= x); } }
    public void on(char[] w, char[] x, BitArr.BA res) { for (int i = 0; i < w.length; i++) { res.add(w[i]!=x[i]); } }
    
    public Value on(boolean w, BitArr  x) { if (w) return NotBuiltin.on(x); return x; }
    public Value on(BitArr  w, boolean x) { if (x) return NotBuiltin.on(w); return w; }
    public void  on(long[]  w, long[]  x, long[] res) { for (int i = 0; i < res.length; i++) res[i] = w[i]^x[i]; }
  };
  
  public Value call(Value w, Value x) {
    return DF.call(w, x);
  }
}