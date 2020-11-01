package APL.tools;

import APL.errors.DomainError;
import APL.types.*;
import APL.types.arrs.ChrArr;

public class Format {
  public static String outputFmt(Value v) {
    if (v instanceof Primitive) {
      if (v instanceof Num) {
        String s = String.valueOf(((Num) v).num);
        if (s.startsWith("-")) s = "¯"+s.substring(1);
        return s.endsWith(".0")? s.substring(0, s.length()-2) : s;
      }
      if (v instanceof Char) return String.valueOf(((Char) v).chr);
      if (v instanceof BigValue) return ((BigValue) v).i.toString();
      throw new DomainError("Cannot format "+v.humanType(true), v);
    }
    if (v.r() > 2) throw new DomainError("Cannot format rank "+v.r()+" array", v);
    if (v.r()<=1) {
      if (v instanceof ChrArr) {
        return v.asString();
      } else {
        StringBuilder b = new StringBuilder(v.ia);
        for (Value c : v) {
          if (!(c instanceof Char)) throw new DomainError("Cannot format array of non-chars", v);
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
            if (!(c instanceof Char)) throw new DomainError("Cannot format array of non-chars", v);
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
