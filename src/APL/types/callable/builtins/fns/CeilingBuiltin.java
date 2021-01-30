package APL.types.callable.builtins.fns;

import APL.errors.DomainError;
import APL.tools.*;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.callable.builtins.FnBuiltin;

public class CeilingBuiltin extends FnBuiltin {
  public String ln(FmtInfo f) { return "⌈"; }
  public Value identity() { return Num.NEGINF; }
  
  public Value call(Value x) {
    if (x instanceof Arr) {
      if (x.quickDoubleArr()) {
        if (x.quickIntArr()) return x;
        double[] xd = x.asDoubleArr();
        ia: {
          int[] res = new int[x.ia];
          for (int i = 0; i < res.length; i++) {
            double c = xd[i];
            if (c>Integer.MIN_VALUE && c<=Integer.MAX_VALUE) {
              int iv = (int) c;
              res[i] = iv + (c>0 && c!=iv? 1 : 0);
            } else break ia;
          }
          return new IntArr(res, x.shape);
        }
        double[] res = new double[x.ia];
        for (int i = 0; i < res.length; i++) res[i] = Math.ceil(xd[i]);
        return new DoubleArr(res, x.shape);
      }
      if (x instanceof ChrArr) {
        return new ChrArr(((ChrArr) x).s.toUpperCase(), x.shape);
      }
      Value[] vs = new Value[x.ia];
      for (int i = 0; i < vs.length; i++) vs[i] = call(x.get(i));
      return new HArr(vs, x.shape);
    } else if (x instanceof Num) return new Num(Math.ceil(((Num) x).num));
    else if (x instanceof Char) return ((Char) x).upper();
    else throw new DomainError("⌈: argument contained "+x.humanType(true), this);
  }
  
  public Pervasion.NN2N dyNum() { return DF; }
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
    public Value on(boolean w, BitArr x) { return w? BitArr.s1(x) : x; }
    public Value on(BitArr w, boolean x) { return x? BitArr.s1(w) : w; }
    public Value on(BitArr w, BitArr x) {
      BitArr.BC res = new BitArr.BC(w.shape);
      for (int i = 0; i < w.arr.length; i++) res.arr[i] = w.arr[i] | x.arr[i];
      return res.finish();
    }
  };
  public Value call(Value w, Value x) {
    return DF.call(w, x);
  }
}