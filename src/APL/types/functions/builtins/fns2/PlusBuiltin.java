package APL.types.functions.builtins.fns2;

import APL.tools.Pervasion;
import APL.types.*;
import APL.types.functions.Builtin;


public class PlusBuiltin extends Builtin {
  public String repr() {
    return "+";
  }
  
  
  
  public Value identity() {
    return Num.ZERO;
  }
  
  public Value call(Value x) {
    return x; // TODO
  }
  
  public Pervasion.NN2N dyNum() { return DF; };
  public static final Pervasion.NN2N DF = new Pervasion.NN2N() {
    public Value on(BigValue w, BigValue x) { return new BigValue(w.i.add(x.i)); }
    public double on(double w, double x) { return w + x; }
    public void on(double   w, double[] x, double[] res) { for (int i = 0; i < x.length; i++) res[i] = w    + x[i]; }
    public void on(double[] w, double   x, double[] res) { for (int i = 0; i < w.length; i++) res[i] = w[i] + x   ; }
    public void on(double[] w, double[] x, double[] res) { for (int i = 0; i < w.length; i++) res[i] = w[i] + x[i]; }
    
    public int[] on(int   w, int[] x) {try{int[]res=new int[x.length];for(int i=0;i<x.length;i++) {res[i]=Math.addExact(w   ,x[i]);}return res;}catch(ArithmeticException e){return null;}}
    public int[] on(int[] w, int   x) {try{int[]res=new int[w.length];for(int i=0;i<w.length;i++) {res[i]=Math.addExact(w[i],x   );}return res;}catch(ArithmeticException e){return null;}}
    public int[] on(int[] w, int[] x) {try{int[]res=new int[x.length];for(int i=0;i<x.length;i++) {res[i]=Math.addExact(w[i],x[i]);}return res;}catch(ArithmeticException e){return null;}}
    // public int[] on(int   w, int[] x) {int[]res=new int[x.length];for(int i=0;i<x.length;i++) {int cw=w   ,cx=x[i],r=res[i]= cw+cx;if(u(cw,cx,r))return null;}return res;}
    // public int[] on(int[] w, int   x) {int[]res=new int[w.length];for(int i=0;i<w.length;i++) {int cw=w[i],cx=x   ,r=res[i]= cw+cx;if(u(cw,cx,r))return null;}return res;}
    // public int[] on(int[] w, int[] x) {int[]res=new int[x.length];for(int i=0;i<w.length;i++) {int cw=w[i],cx=x[i],r=res[i]= cw+cx;if(u(cw,cx,r))return null;}return res;}
    // private boolean u(int w, int x, int r) { return ((w^r) & (x^r)) < 0; }
  };
  public Value call(Value w, Value x) {
    return DF.call(w, x);
  }
  public Value callInv(Value x) { return call(x); }
  public Value callInvW(Value w, Value x) {
    return MinusBuiltin.DF.call(x, w);
  }
  
  @Override public Value callInvA(Value w, Value x) {
    return callInvW(x, w);
  }
}