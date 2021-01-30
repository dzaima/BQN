package APL.types.arrs;

import APL.errors.DomainError;
import APL.tools.Pervasion;
import APL.types.*;

import java.util.Arrays;

public class ChrArr extends Arr {
  public String s;
  
  public ChrArr(String s) {
    super(new int[]{s.length()}, s.length());
    this.s = s;
  }
  public ChrArr(String s, int[] sh) {
    super(sh, s.length());
    this.s = s;
  }
  
  public ChrArr(char[] arr, int[] sh) {
    this(new String(arr), sh);
  }
  public ChrArr(char[] arr) {
    this(new String(arr));
  }
  
  
  public Value get(int i) {
    return Char.of(s.charAt(i));
  }
  
  public String asString() {
    if (r() > 1) throw new DomainError("Using rank "+r()+" character array as string");
    return s;
  }
  
  public Value fItem() { return Char.SPACE; }
  public Value fItemS() { return Char.SPACE; }
  public int arrInfo() { return Pervasion.ARR_C16; }
  
  public boolean quickDepth1() { return true; }
  public Value ofShape(int[] sh) {
    return new ChrArr(s, sh);
  }
  
  public int hashCode() {
    if (hash == 0) {
      for (char c : s.toCharArray()) hash = hash*31 + c;
      hash = shapeHash(hash);
    }
    return hash;
  }
  public boolean eq(Value x) {
    if (x instanceof ChrArr) {
      if (!Arrays.equals(shape, x.shape)) return false;
      int xh = ((Arr) x).hash;
      if (hash!=0 && xh!=0 && hash!=xh) return false;
      return s.equals(((ChrArr) x).s);
    }
    return super.eq(x);
  }
}