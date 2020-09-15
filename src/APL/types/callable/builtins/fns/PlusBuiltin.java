package APL.types.callable.builtins.fns;

import APL.errors.DomainError;
import APL.tools.Pervasion;
import APL.types.*;
import APL.types.arrs.ChrArr;
import APL.types.callable.builtins.FnBuiltin;


public class PlusBuiltin extends FnBuiltin {
  public String repr() {
    return "+";
  }
  
  
  
  public Value identity() {
    return Num.ZERO;
  }
  
  public Value call(Value x) {
    return x; // TODO
  }
  
  public Pervasion.NN2N dyNum() { return DF; }
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
    
    public Value each(Value w, Value x) {
      if (x instanceof ChrArr) { Value t=x; x=w; w=t; }
      if (w instanceof ChrArr && x.quickIntArr()) {
        int[] xi = x.asIntArr();
        String ws = ((ChrArr) w).s;
        char[] r = new char[xi.length];
        for (int i = 0; i < r.length; i++) r[i] = (char) (xi[i]+ws.charAt(i));
        return new ChrArr(r, x.shape);
      }
      return super.each(w, x);
    }
    
    public Value scalarX(Value w, double x) {
      if (w instanceof ChrArr) return scalarW(x, w);
      return super.scalarX(w, x);
    }
    public Value scalarW(double w, Value x) {
      if (x instanceof ChrArr) {
        int k = Num.toInt(w);
        char[] res = new char[x.ia];
        String xs = ((ChrArr) x).s;
        for (int i = 0; i < res.length; i++) res[i] = (char) (xs.charAt(i)+k);
        return new ChrArr(res, x.shape);
      }
      return super.scalarW(w, x);
    }
    
    public Value on(Primitive w, Primitive x) {
      if (w instanceof Char || x instanceof Char) {
        if (w instanceof Char && x instanceof Char) throw new DomainError("+: cannot add char to char");
        if (w instanceof Char) return Char.of((char) (((Char) w).chr+x.asInt()));
        else                   return Char.of((char) (((Char) x).chr+w.asInt()));
      }
      return super.on(w, x);
    }
  };
  public Value call(Value w, Value x) {
    return DF.call(w, x);
  }
  public Value callInv(Value x) { return call(x); }
  public Value callInvX(Value w, Value x) {
    return MinusBuiltin.DF.call(x, w);
  }
  
  @Override public Value callInvW(Value w, Value x) {
    return callInvX(x, w);
  }
}