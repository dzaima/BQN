package APL.types.functions.builtins.fns2;

import APL.Main;
import APL.errors.DomainError;
import APL.types.*;
import APL.types.functions.Builtin;

import java.math.BigInteger;

public class StileBuiltin extends Builtin {
  @Override public String repr() {
    return "|";
  }
  
  
  
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
  };
  
  public Value call(Value x) {
    return numChrMapM(NF, c->{ throw new DomainError("|char", this, x); }, c -> Num.of(c.size()), x);
  }
  
  private static final D_NNeN DNF = new D_NNeN() {
    public double on(double w, double x) {
      double c = x % w;
      if (c!=0  &  (w>=0 ^ x>=0)) c+= w;
      return c;
    }
    public void on(double[] res, double w, double[] x) {
      for (int i = 0; i < x.length; i++) {
        double xc = x[i];
        double c = xc%w;
        if (c!=0  &  (w>=0 ^ xc>=0)) c+= w;
        res[i] = c;
      }
    }
    public void on(double[] res, double[] w, double x) {
      for (int i = 0; i < w.length; i++) {
        double wc = w[i];
        double c = x%wc;
        if (c!=0  &  (wc>=0 ^ x>=0)) c+= wc;
        res[i] = c;
      }
    }
    public void on(double[] res, double[] w, double[] x) {
      for (int i = 0; i < w.length; i++) {
        double wc = w[i];
        double xc = x[i];
        double c = xc%wc;
        if (c!=0  &  (wc>=0 ^ xc>=0)) c+= wc;
        res[i] = c;
      }
    }
    public Value call(BigValue w, BigValue x) {
      BigInteger r = x.i.remainder(w.i);
      if (r.signum()<0) r = r.add(w.i);
      return new BigValue(r);
    }
  };
  public Value call(Value w, Value x) {
    return numD(DNF, w, x);
  }
  
  
  
  private static final D_NNeN CPY_SGN = new D_NNeN() {
    public double on(double o, double n) {
      if (o==0 && n!=0) throw new DomainError("⌾|: cannot add sign to "+n+" as original was 0");
      return o<0? -n : n;
    }
    public void on(double[] res, double o, double[] n) {
      for (int i = 0; i < res.length; i++) {
        double nc = n[i];
        if (o==0 && nc!=0) throw new DomainError("⌾|: cannot add sign to "+nc+" as original was 0");
        res[i] = o<0? -nc : nc;
      }
    }
    public void on(double[] res, double[] o, double n) {
      for (int i = 0; i < res.length; i++) {
        double oc = o[i];
        if (oc==0 && n!=0) throw new DomainError("⌾|: cannot add sign to "+n+" as original was 0");
        res[i] = oc<0? -n : n;
      }
    }
    public void on(double[] res, double[] o, double[] n) {
      for (int i = 0; i < res.length; i++) {
        double oc = o[i];
        double nc = n[i];
        if (oc==0 && nc!=0) throw new DomainError("⌾|: cannot add sign to "+nc+" as original was 0");
        res[i] = oc<0? -nc : nc;
      }
    }
    public Value call(BigValue o, BigValue n) {
      BigInteger oi = o.i;
      BigInteger ni = n.i;
      if (oi.signum()==0 && ni.signum()!=0) throw new DomainError("⌾|: cannot add sign to "+ni+" as original was 0");
      return oi.signum()<0? new BigValue(ni.negate()) : n;
    }
  };
  public Value under(Value o, Value x) {
    Main.faulty = this;
    Value v = o instanceof Fun? ((Fun) o).call(call(x)) : o;
    return numD(CPY_SGN, x, v);
  }
}