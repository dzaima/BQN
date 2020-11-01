package APL.types.callable.builtins.fns;

import APL.errors.DomainError;
import APL.tools.*;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.callable.builtins.FnBuiltin;

public class MinusBuiltin extends FnBuiltin {
  public String ln(FmtInfo f) { return "-"; }
  
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
  
  public Pervasion.NN2N dyNum() { return DF; }
  public static final Pervasion.NN2N DF = new Pervasion.NN2N() {
    public Value on(BigValue w, BigValue x) { return new BigValue(w.i.subtract(x.i)); }
    public double on(double w, double x) { return w - x; }
    public void on(double   w, double[] x, double[] res) { for (int i = 0; i < x.length; i++) res[i] = w    - x[i]; }
    public void on(double[] w, double   x, double[] res) { for (int i = 0; i < w.length; i++) res[i] = w[i] - x   ; }
    public void on(double[] w, double[] x, double[] res) { for (int i = 0; i < w.length; i++) res[i] = w[i] - x[i]; }
    
    public int[] on(int   w, int[] x) {try{int[]res=new int[x.length];for(int i=0;i<x.length;i++) {res[i]=Math.subtractExact(w   ,x[i]);}return res;}catch(ArithmeticException e){return null;}}
    public int[] on(int[] w, int   x) {try{int[]res=new int[w.length];for(int i=0;i<w.length;i++) {res[i]=Math.subtractExact(w[i],x   );}return res;}catch(ArithmeticException e){return null;}}
    public int[] on(int[] w, int[] x) {try{int[]res=new int[x.length];for(int i=0;i<x.length;i++) {res[i]=Math.subtractExact(w[i],x[i]);}return res;}catch(ArithmeticException e){return null;}}
    
    public Value each(Value w, Value x) {
      if (w instanceof ChrArr && x.quickIntArr()) {
        int[] xi = x.asIntArr();
        String ws = ((ChrArr) w).s;
        char[] r = new char[xi.length];
        for (int i = 0; i < xi.length; i++) r[i] = (char) (ws.charAt(i)-xi[i]);
        return new ChrArr(r, x.shape);
      }
      if (w instanceof ChrArr && x instanceof ChrArr) {
        String ws = ((ChrArr) w).s;
        String xs = ((ChrArr) x).s;
        int[] d = new int[ws.length()];
        for (int i = 0; i < d.length; i++) d[i] = ws.charAt(i)-xs.charAt(i);
        return new IntArr(d, x.shape);
      }
      return super.each(w, x);
    }
    public Value scalarX(Value w, double x) {
      if (w instanceof ChrArr) {
        String ws = ((ChrArr) w).s;
        int xi = Num.toInt(x);
        char[] r = new char[ws.length()];
        for (int i = 0; i < ws.length(); i++) r[i] = (char) (ws.charAt(i)-xi);
        return new ChrArr(r, w.shape);
      }
      return super.scalarX(w, x);
    }
    public Value on(Primitive w, Primitive x) {
      if (w instanceof Char || x instanceof Char) {
        if (w instanceof Char && x instanceof Char) return new Num(((Char) w).chr-((Char) x).chr);
        if (w instanceof Char && x instanceof Num) return Char.of((char) (((Char) w).chr-x.asInt()));
        if (x instanceof Char) throw new DomainError("-: cannot operate on "+w.humanType(true)+" and "+x.humanType(false));
      }
      return super.on(w, x);
    }
  };
  
  public Value call(Value w, Value x) {
    return DF.call(w, x);
  }
  public Value callInv(Value x) { return call(x); }
  public Value callInvX(Value w, Value x) { return call(w, x); }
  public Value callInvW(Value w, Value x) {
    return PlusBuiltin.DF.call(w, x);
  }
}