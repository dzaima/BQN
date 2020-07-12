package APL.types;

import APL.errors.*;
import APL.types.arrs.*;
import APL.types.functions.builtins.fns2.NotBuiltin;

public class Pervasion {
  public static abstract class VV {
    public abstract Value on(Primitive w, Primitive x);
    
    public Value call(Value w, Value x) {
      int wr = w.rank;
      int xr = x.rank;
      if (xr==0) {
        if (wr==0) {
          if (w instanceof Primitive && x instanceof Primitive) return on((Primitive) w, (Primitive) x);
          else call(w.first(), x.first());
        }
        return scalarX(w, x.first());
      } else if (wr==0) {
        return scalarW(w.first(), x);
      }
      if (wr==xr) {
        Arr.eqShapes(w, x);
        return each(w, x);
      } else {
        throw new NYIError("mismatched rank pervasion", x);
      }
    }
    
    public Value each(Value w, Value x) {
      Value[] res = new Value[w.ia];
      for (int i = 0; i < w.ia; i++) res[i] = call(w.get(i), x.get(i));
      return Arr.create(res, x.shape);
    }
    
    public Value scalarX(Value w, Value x) {
      Value[] res = new Value[w.ia];
      for (int i = 0; i < w.ia; i++) res[i] = call(w.get(i), x);
      return Arr.create(res, w.shape);
    }
    public Value scalarW(Value w, Value x) {
      Value[] res = new Value[x.ia];
      for (int i = 0; i < x.ia; i++) res[i] = call(w, x.get(i));
      return Arr.create(res, x.shape);
    }
  }
  
  public static abstract class NN2N extends VV { // num,num gives num
    public abstract double on(double   w, double   x);
    public abstract void   on(double   w, double[] x, double[] res);
    public abstract void   on(double[] w, double   x, double[] res);
    public abstract void   on(double[] w, double[] x, double[] res);
    
    public /*open*/ int[] on(int   w, int[] x) { return null; }
    public /*open*/ int[] on(int[] w, int   x) { return null; }
    public /*open*/ int[] on(int[] w, int[] x) { return null; }
    public Value on(Primitive w, Primitive x) {
      if (w instanceof BigValue && x instanceof BigValue) return on(((BigValue) w), ((BigValue) x));
      if (!(w instanceof Num) || !(x instanceof Num)) throw new DomainError("calling a number only function on "+w.humanType(true)+" and "+x.humanType(false));
      return Num.of(on(((Num) w).num, ((Num) x).num));
    }
    
    public /*open*/ Value on(BigValue w, BigValue x) { throw new DomainError("bigintegers not allowed here", x); }
    

    public Value scalarX(Value w, Value x) {
      if (w.quickDoubleArr() && x instanceof Num) {
        if (w.quickIntArr() && Num.isInt(((Num) x).num)) {
          int[] ri = on(w.asIntArr(), (int) ((Num) x).num);
          if (ri != null) return new IntArr(ri, w.shape);
        }
        double[] rd = new double[w.ia];
        on(w.asDoubleArr(), ((Num) x).num, rd);
        return new DoubleArr(rd, w.shape);
      }
      return super.scalarX(w, x);
    }
    public Value scalarW(Value w, Value x) {
      if (w instanceof Num && x.quickDoubleArr()) {
        if (Num.isInt(((Num) w).num) && x.quickIntArr()) {
          int[] ri = on((int) ((Num) w).num, x.asIntArr());
          if (ri != null) return new IntArr(ri, x.shape);
        }
        double[] rd = new double[x.ia];
        on(((Num) w).num, x.asDoubleArr(), rd);
        return new DoubleArr(rd, x.shape);
      }
      return super.scalarW(w, x);
    }
    public Value each(Value w, Value x) {
      if (w.quickDoubleArr() && x.quickDoubleArr()) {
        if (w.quickIntArr() && x.quickIntArr()) {
          int[] ri = on(w.asIntArr(), x.asIntArr());
          if (ri != null) return new IntArr(ri, x.shape);
        }
        double[] rd = new double[x.ia];
        on(w.asDoubleArr(), x.asDoubleArr(), rd);
        return new DoubleArr(rd, x.shape);
      }
      return super.each(w, x);
    }
  }
  
  public static abstract class NN2NpB extends NN2N { // num,num gives num, plus bool,bool gives bool
    public abstract Value on(boolean w, BitArr  x);
    public abstract Value on(BitArr  w, boolean x);
    public abstract Value on(BitArr  w, BitArr  x);
    
    public Value scalarW(Value w, Value x) {
      if (w instanceof Num && Num.isBool(((Num) w).num) && x instanceof BitArr) return on(((Num) w).num==1, (BitArr) x); 
      return super.scalarW(w, x);
    }
    public Value scalarX(Value w, Value x) {
      if (w instanceof BitArr && x instanceof Num && Num.isBool(((Num) x).num)) return on((BitArr) w, ((Num) x).num==1);
      return super.scalarX(w, x);
    }
    public Value each(Value w, Value x) {
      if (w instanceof BitArr && x instanceof BitArr) return on((BitArr) w, (BitArr) x);
      return super.each(w, x);
    }
  }
  
  public static abstract class VV2B extends VV { // for comparisons
    public abstract void on(double   w, double[] x, BitArr.BA res);
    public abstract void on(double[] w, double   x, BitArr.BA res);
    public abstract void on(double[] w, double[] x, BitArr.BA res);
  
    public abstract void on(int   w, int[] x, BitArr.BA res);
    public abstract void on(int[] w, int   x, BitArr.BA res);
    public abstract void on(int[] w, int[] x, BitArr.BA res);
    
    public abstract void on(char   w, char[] x, BitArr.BA res);
    public abstract void on(char[] w, char   x, BitArr.BA res);
    public abstract void on(char[] w, char[] x, BitArr.BA res);
  
    public abstract Value on(boolean w, BitArr  x);
    public abstract Value on(BitArr  w, boolean x);
    public abstract void  on(long[]  w, long[]  x, long[] res);
    
    public Value scalarW(Value w, Value x) {
      if (x.quickDoubleArr() && w instanceof Num) {
        double wd = ((Num) w).num;
        if (Num.isBool(wd) && x instanceof BitArr) return on(wd==1, (BitArr) x);
        BitArr.BA res = new BitArr.BA(x.shape);
        if (Num.isInt(wd) && x.quickIntArr()) on((int) wd, x.asIntArr(), res);
        else on(wd, x.asDoubleArr(), res);
        return res.finish();
      }
      if (w instanceof Char && x instanceof ChrArr) {
        BitArr.BA res = new BitArr.BA(x.shape);
        on(((Char) w).chr, ((ChrArr) x).s.toCharArray(), res);
        return res.finish();
      }
      return super.scalarW(w, x);
    }
    public Value scalarX(Value w, Value x) {
      if (w.quickDoubleArr() && x instanceof Num) {
        double xd = ((Num) x).num;
        if (w instanceof BitArr && Num.isBool(xd)) return on((BitArr) w, xd==1);
        BitArr.BA res = new BitArr.BA(w.shape);
        if (w.quickIntArr() && Num.isInt(xd)) on(w.asIntArr(), (int) xd, res);
        else on(w.asDoubleArr(), xd, res);
        return res.finish();
      }
      if (w instanceof ChrArr && x instanceof Char) {
        BitArr.BA res = new BitArr.BA(w.shape);
        on(((ChrArr) w).s.toCharArray(), ((Char) x).chr, res);
        return res.finish();
      }
      return super.scalarX(w, x);
    }
    public Value each(Value w, Value x) {
      if (w.quickDoubleArr() && x.quickDoubleArr()) {
        if (w instanceof BitArr && x instanceof BitArr) {
          long[] wa = ((BitArr) w).arr;
          long[] res = new long[wa.length];
          on(wa, ((BitArr) x).arr, res);
          return new BitArr(res, x.shape);
        }
        BitArr.BA res = new BitArr.BA(x.shape);
        if (w.quickIntArr() && x.quickDoubleArr()) on(w.asIntArr(), x.asIntArr(), res);
        else on(w.asDoubleArr(), x.asDoubleArr(), res);
        return res.finish();
      }
      if (w instanceof ChrArr && x instanceof ChrArr) {
        BitArr.BA res = new BitArr.BA(x.shape);
        on(((ChrArr) w).s.toCharArray(), ((ChrArr) x).s.toCharArray(), res);
        return res.finish();
      }
      return super.each(w, x);
    }
    
    protected Value not(BitArr x) {
      return NotBuiltin.call(x);
    }
    protected Value s0(BitArr x) {
      return new SingleItemArr(Num.ZERO, x.shape);
    }
    protected Value s1(BitArr x) {
      return new SingleItemArr(Num.ONE, x.shape);
    }
  }
  
  
  private Pervasion() { }
}
