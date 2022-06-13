package BQN.types;

import BQN.errors.DomainError;
import BQN.tools.*;
import BQN.types.arrs.*;

public class Num extends Primitive {
  public static final long MAX_SAFE_DOUBLE = 9007199254740992L;
  
  public static final Num NEGINF = new Num(Double.NEGATIVE_INFINITY);
  public static final Num POSINF = new Num(Double.POSITIVE_INFINITY);
  public static final Num MINUS_ONE = new Num(-1d);
  public static final Num ZERO  = new Num(0d);
  public static final Num ONE   = new Num(1d);
  public static final Num[] NUMS = new Num[256];
  private static final Num NEG_ZERO = new Num(-0.0);
  
  public static int pp;
  public static int sEr, eEr;
  
  static {
    for (int i = 0; i < NUMS.length; i++) {
      NUMS[i] = new Num(i);
    }
    // setPrecision(14, -10, 10);
  }
  
  
  public static final Num E = new Num("2.71828182845904523536028747135266249775724709369995");
  public static final Num PI = new Num("3.1415926535897932384626433832795028841971693993751");
  public final double num;
  public Num(String val) {
    if (val.startsWith("¯")) {
      num = -Double.parseDouble(val.substring(1));
    } else num = Double.parseDouble(val);
  }
  
  public Num(int    n) { num = n; }
  public Num(long   n) { num = n; }
  public Num(double n) { num = n; }
  
  public static Num of(int n) {
    if (n>=0 && n<256) return NUMS[n];
    return new Num(n);
  }
  
  public static Num of(double n) {
    if (n == 0) return Double.doubleToRawLongBits(n)==0? NUMS[0] : NEG_ZERO;
    if (n>0 & n<256 && n==(int)n) return NUMS[(int) n];
    return new Num(n);
  }
  public static boolean isInt(double d) {
    return (int)d == d;
  }
  public static boolean isBool(double n) {
    return n==0 || n==1;
  }
  public static int toInt(double d) {
    int i = (int)d;
    if (i != d) throw new DomainError("Expected integer, got "+d);
    return i;
  }
  @Deprecated public static int toInt(int i) { throw new AssertionError(); }
  
  
  
  public Value ofShape(int[] sh) { assert Arr.prod(sh) == 1;
    return isInt(num)? (Value)new IntArr(new int[]{(int) num}, sh) : new DoubleArr(new double[]{num}, sh);
  }
  
  
  public int   asInt        () { return           toInt(num) ; }
  public int[] asIntArrClone() { return new int[]{toInt(num)}; }
  public int[] asIntArr     () { return new int[]{toInt(num)}; }
  
  public double   asDouble        () { return              num ; }
  public double[] asDoubleArr     () { return new double[]{num}; }
  public double[] asDoubleArrClone() { return new double[]{num}; }
  public double sum() { return num; }
  
  public Value[] valuesClone() { return new Value[]{this}; }
  public Value[] values     () { return new Value[]{this}; }
  
  
  public boolean quickIntArr() { return isInt(num); }
  public boolean quickDoubleArr() { return true; }
  public Value fItemS() { return ZERO; }
  public int atomInfo() {
    int iv = (int)num;
    return iv==num? ((iv&1)==iv? Pervasion.ATM_BIT : Pervasion.ATM_I32) : Pervasion.ATM_F64;
  }
  
  
  
  
  
  
  
  
  
  
  public boolean eq(Value n) {
    return n instanceof Num && ((Num) n).num == num;
  }
  public int hashCode() {
    if (num == 0d) return 0; // ¯0 == 0
    return Double.hashCode(num);
  }
  
  
  
  
  
  
  
  
  
  
  public Value pretty(FmtInfo f) { return new ChrArr(format(num, f.pp, f.pns, f.pne)); }
  public String    ln(FmtInfo f) { return            format(num, f.pp, f.pns, f.pne);  }
  
  
  // ============================== NUMBER FORMATTING ============================== \\
  
  public static String fmt(double d) { // for simple cases when 
    return format(d, 20, 10, 10);
  }
  public static String formatInt(int i) {
    return i<0? "¯"+(-i) : Integer.toString(i);
  }
  private static final char[] buf = new char[400];
  public static String format(double d, int pp, int pns, int pne) {
    return d+"";
    pp--; // too lazy to change all uses of this
    double a = Math.abs(d);
    if (d == 0) {
      if (Double.doubleToRawLongBits(d) == 0) return "0";
      else return "¯0";
    }
    
    String f = a.toString("E"+pp);
    // Console.WriteLine(f);
    char[] fa = f.toCharArray();
    if (fa[0] > '9') {
      if (fa[0] == 'N') return "NaN";
      return d<0? "¯∞" : "∞";
    }
    int exp = (fa[pp+4]-'0')*10  +  fa[pp+5]-'0'; // exponent
    if (pp+6<fa.length) exp = exp*10  +  fa[pp+6]-'0';
    boolean negExp = fa[pp+3] == '-';
    
    /* pp=4:
       4.0000e+03 (optionally another digit)
       0123456789
     ∆ 43210123456
       ¯¯¯¯
    */
    if (negExp? exp>pns : exp>pne) { // scientific notation
      int len = 0;
      if (d < 0) buf[len++] = '¯';
      
      int ls = pp+1; // last significant digit position
      while (fa[ls] == '0') ls--;
      if (ls == 1) ls = 0;
      ls++;
      
      System.arraycopy(fa, 0, buf, len, ls);
      len+= ls;
      buf[len++] = 'e';
      
      if (negExp) buf[len++] = '¯';
      
      int es = pp+4;
      int ee = fa.length;
      if (fa[pp+4] == '0') es++;
      System.arraycopy(fa, es, buf, len, ee-es);
      len+= ee-es;
      
      
      return new String(buf, 0, len);
    } else {
      
      
      // generic positional notation
      /* )nf 5 (pp==4):
         _31416e+00
         01234567890
       ∆ 432101234567
         ¯¯¯¯
      */
      int len = 0;
      if (d < 0) buf[len++] = '¯';
      fa[1] = fa[0]; // put all the significant digits in order
      fa[0] = '0'; // 0 so ls calculation doesn't have to special-case
      
      
      int ls = pp+1; // length of significant digits
      while (fa[ls] == '0') ls--;
      
      if (negExp) {
        buf[len] = '0';
        buf[len+1] = '.';
        len+= 2;
        for (int i = 0; i < exp-1; i++) buf[len+i] = '0'; // zero padding
        len+= exp-1;
        System.arraycopy(fa, 1, buf, len, ls); // actual digits; ≡  for (int i = 0; i < ls; i++) buf[len+i] = fa[i+1];
        len+= ls;
      } else {
        
        
        // generic standard notation
        /* •pp←4:
           _31416e+00
           01234567890
         ∆ 432101234567
           ¯¯¯¯
        */
        if (d < 0) {
          buf[len++] = '¯';
        }
        fa[1] = fa[0]; // put all the significant digits in order
        fa[0] = '0'; // 0 so ls calculation doesn't have to special-case
        
        
        ls = pp+1; // length of significant digits
        while (fa[ls] == '0') ls--;
        
        if (fa[pp+3] == '-') {
          buf[len] = '0';
          buf[len+1] = '.';
          len+= 2;
          for (int i = 0; i < exp-1; i++) buf[len+i] = '0'; // zero padding
          len+= exp-1;
          System.arraycopy(fa, 1, buf, len, ls); // actual digits; ≡  for (int i = 0; i < ls; i++) buf[len+i] = fa[i+1];
          len+= ls;
        } else {
          if (exp+1 < ls) {
            System.arraycopy(fa, 1, buf, len, exp+1); // digits before '.'; ≡  for (int i = 0; i < exp+1; i++) buf[len+i] = fa[i+1];
            len+= exp+1;
            buf[len++] = '.';
            System.arraycopy(fa, 2+exp, buf, len, ls-exp-1); // ≡  for (int i = 0; i < ls-exp-1; i++) buf[len+i] = fa[i+(2+exp)];
            len+= ls-exp-1;
          } else {
            System.arraycopy(fa, 1, buf, len, ls); // all given digits; ≡  for (int i = 0; i < ls; i++) buf[len+i] = fa[i+1];
            len+= ls;
            for (int i = 0; i < exp-ls+1; i++) buf[len+i] = '0'; // pad with zeroes
            len+= exp-ls+1;
          }
        }
        // System.out.println(f+": sig="+ls+"; exp="+exp+"; len="+len);
      }
      return new String(buf, 0, len);
    }
  }
}