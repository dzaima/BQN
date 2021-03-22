package BQN.tools;

import BQN.errors.DomainError;
import BQN.types.*;
import BQN.types.arrs.ChrArr;

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
  
  public static Value str(String s) { // TODO split graphemes, color escapes, etc idk
    int len = s.codePointCount(0, s.length());
    if (len==s.length()) return new ChrArr(s);
    MutVal mv = new MutVal(Arr.vecsh(len), Char.SPACE);
    int i=0, o=0;
    while (i < s.length()) {
      int c = s.codePointAt(i);
      int csz = Character.charCount(c);
      mv.set(o++, chr(c, csz));
      i+= csz;
    }
    return mv.get();
  }
  
  public static Value chr(int c, int csz) {
    return csz==1? Char.of((char) c) : new ChrArr(new String(Character.toChars(c)));
  }
  public static String chr(int c) {
    return Character.charCount(c)==1? Character.toString((char) c) : new String(Character.toChars(c));
  }
}