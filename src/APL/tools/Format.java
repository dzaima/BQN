package APL.tools;

import APL.errors.DomainError;
import APL.types.*;
import APL.types.arrs.ChrArr;

public class Format {
  public static String outputFmt(Value v) {
    if (v instanceof Primitive) {
      if (v instanceof Num) {
        double n = ((Num) v).num;
        long b = Double.doubleToRawLongBits(n);
        if (Double.isInfinite(n)) return n==Double.POSITIVE_INFINITY? "∞" : "¯∞";
        if (Double.isNaN(n)) return "NaN";
        StringBuilder s = new StringBuilder(b<0? "¯" : "");
        s.append(Math.abs(n));
        int l = s.length();
        if (s.charAt(l-2)=='.' && s.charAt(l-1)=='0') s.delete(l-2, l);
        return s.toString();
      }
      if (v instanceof Char) return String.valueOf(((Char) v).chr);
      if (v instanceof BigValue) return ((BigValue) v).i.toString();
      throw new DomainError("Cannot format "+v.humanType(true));
    }
    if (v.r() > 2) throw new DomainError("Cannot format rank "+v.r()+" array");
    if (v.r()<=1) {
      if (v instanceof ChrArr) {
        return v.asString();
      } else {
        StringBuilder b = new StringBuilder(v.ia);
        for (Value c : v) {
          if (!(c instanceof Char)) throw new DomainError("Cannot format array of non-chars");
          b.append(((Char) c).chr);
        }
        return b.toString();
      }
    } else {
      StringBuilder b = new StringBuilder(v.ia);
      int h = v.shape[0];
      int w = v.shape[1];
      if (v instanceof ChrArr) {
        String str = ((ChrArr) v).s;
        for (int y = 0; y < h; y++) {
          b.append(str, y*w, (y+1)*w);
          if (y!=h-1) b.append('\n');
        }
      } else {
        for (int y = 0; y < h; y++) {
          // b.append(str, y*w, (y+1)*w);
          for (int x = 0; x < w; x++) {
            Value c = v.get(x+y*w);
            if (!(c instanceof Char)) throw new DomainError("Cannot format array of non-chars");
            b.append(((Char) c).chr);
          }
          if (y!=h-1) b.append('\n');
        }
      }
      return b.toString();
    }
  }
  
  public static Value str(String s) {
    return new ChrArr(s); // TODO split graphemes, color escapes, etc idk
  }
}
