package APL.types;

import APL.tools.*;
import APL.types.arrs.ChrArr;


public class Char extends Primitive {
  public char chr;
  public static final Char[] ASCII;
  static {
    ASCII = new Char[128];
    for (int i = 0; i < 128; i++) {
      ASCII[i] = new Char((char) i);
    }
  }
  
  public static final Char SPACE = ASCII[' '];
  
  public Char(char c) {
    chr = c;
  }
  public static Char of(char c) {
    if (c < 128) return ASCII[c];
    return new Char(c);
  }
  
  
  public Char upper() { return Char.of(Character.toUpperCase(chr)); }
  public Char lower() { return Char.of(Character.toLowerCase(chr)); }
  public Char swap() {
    if (Character.isUpperCase(chr)) return lower();
    if (Character.isLowerCase(chr)) return upper();
    return this;
  }
  public int getCase() {
    return Character.isUpperCase(chr)? 1 : Character.isLowerCase(chr)? -1 : 0;
  }
  
  
  
  public Value ofShape(int[] sh) { assert Arr.prod(sh) == 1;
    return new ChrArr(String.valueOf(chr), sh);
  }
  public Value safePrototype() { return SPACE; }
  public String asString() { return String.valueOf(chr); }
  public int atomInfo() { return Pervasion.ATM_CHR; }
  
  public boolean eq(Value c) {
    return c instanceof Char && chr == ((Char) c).chr;
  }
  public int compareTo(Char v) {
    return Character.compare(chr, v.chr);
  }
  public int hashCode() {
    return chr;
  }
  
  public String ln(FmtInfo f) {
    if (spec()) return "@+" + (+chr);
    return "'"+chr+"'";
  }
  public Value pretty(FmtInfo f) {
    return new ChrArr(ln(f));
  }
  public boolean spec() {
    return chr<32 || chr==127;
  }
}