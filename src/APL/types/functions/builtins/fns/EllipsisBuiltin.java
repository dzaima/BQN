package APL.types.functions.builtins.fns;

import APL.errors.DomainError;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.functions.builtins.FnBuiltin;

import java.math.BigInteger;

public class EllipsisBuiltin extends FnBuiltin {
  @Override public String repr() {
    return "…";
  }
  
  
  
  public Value call(Value w, Value x) {
    if (w instanceof BigValue || x instanceof BigValue) {
      BigInteger al = BigValue.bigint(w);
      BigInteger wl = BigValue.bigint(x);
      BigInteger size = al.subtract(wl).abs().add(BigInteger.ONE);
      int isize = BigValue.safeInt(size);
      if (isize==Integer.MAX_VALUE) throw new DomainError("…: expected range too large ("+w+"…"+x+")", this, x);
      
      Value[] arr = new Value[isize];
      BigInteger c = al;
      BigInteger dir = al.compareTo(wl) < 0? BigInteger.ONE : BigValue.MINUS_ONE.i;
      for (int i = 0; i < isize; i++) {
        arr[i] = new BigValue(c);
        c = c.add(dir);
      }
      return new HArr(arr);
    }
    int wi = w.asInt();
    int xi = x.asInt();
    int[] arr = new int[Math.abs(wi-xi)+1];
    if (wi>xi) {
      for (int i = 0; i < arr.length; i++) arr[i] = wi - i;
    } else {
      for (int i = 0; i < arr.length; i++) arr[i] = wi + i;
    }
    return new IntArr(arr);
  }
}