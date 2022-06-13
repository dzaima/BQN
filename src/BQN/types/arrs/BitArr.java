package BQN.types.arrs;

import BQN.Main;
import BQN.tools.Pervasion;
import BQN.types.*;
import BQN.types.callable.builtins.fns.NotBuiltin;

import java.util.Arrays;

public final class BitArr extends Arr {
  
  public final long[] arr;
  // data[0]&1 - 1st item, (data[0]&0b10)
  // filler can be anything
  
  public BitArr(long[] arr, int[] shape) {
    super(shape);
    assert sizeof(shape) == arr.length : arr.length+" not expected for shape "+Main.fArr(shape);
    this.arr = arr;
  }
  
  public BitArr(long[] arr, int[] shape, int ia) {
    super(shape, ia);
    this.arr = arr;
  }
  
  
  public static int sizeof(Value x) {
    return x.ia+63 >> 6;
  }
  public static int sizeof(int am) {
    return am+63 >> 6;
  }
  public static int sizeof(int[] sh) {
    int m = 1;
    for (int i : sh) m*= i;
    return sizeof(m);
  }
  
  public static Value not(BitArr x) { return NotBuiltin.on(x); }
  public static Value s0(Value v) { return new BitArr(new long[sizeof(v.ia)], v.shape, v.ia); }
  public static Value s1(Value v) { long[]ls=new long[sizeof(v.ia)];Arrays.fill(ls,-1L); return new BitArr(ls, v.shape, v.ia); }
  
  
  public void setEnd(boolean on) {
    if ((ia&63) != 0) {
      int extra = ia&63;
      long tail = -(1L<<extra); // bits outside of the array
      long last = arr[arr.length-1]; // last item of the array
      long at = tail & (on? ~last : last); // masking tail bits of the last item
      arr[arr.length-1] = last ^ at;
    }
  }
  public static void setEnd(long[] arr, int ia, boolean on) {
    if ((ia&63) != 0) {
      int extra = ia&63;
      long tail = -(1L<<extra); // bits outside of the array
      long last = arr[arr.length-1]; // last item of the array
      long at = tail & (on? ~last : last); // masking tail bits of the last item
      arr[arr.length-1] = last ^ at;
    }
  }
  
  
  
  public Value get(int i) {
    return Num.NUMS[(int) ((arr[i>>6] >> (i&63)) & 1)]; // no branching!
  }
  
  
  
  public int[] asIntArrClone() {
    int[] res = new int[ia];
    int rp = 0;
    for (int i = 0; i < ia>>6; i++) {
      long cl = arr[i];
      for (int j = 0; j < 64; j++) {
        res[rp++] = (int) (cl&1);
        cl>>= 1;
      }
    }
    int over = ia & 63;
    for (int i = 0; i < over; i++) {
      res[rp++] = (int) ((arr[rp / 64]>>i) & 1);
    }
    return res;
  }
  
  public double[] asDoubleArrClone() {
    double[] res = new double[ia];
    int ctr = 0;
    for (int i = 0; i < ia>>6; i++) {
      long cl = arr[i];
      for (int j = 0; j < 64; j++) {
        res[ctr++] = cl&1;
        cl>>= 1;
      }
    }
    int over = ia & 63;
    for (int i = 0; i < over; i++) {
      res[ctr++] = (int) ((arr[ctr / 64]>>i) & 1);
    }
    return res;
  }
  public long[] asBitLongs() {
    return arr;
  }
  
  
  public boolean quickDoubleArr() { return true; }
  public boolean quickIntArr() { return true; }
  public boolean quickDepth1() { return true; }
  public Value ofShape(int[] sh) { return new BitArr(arr, sh); }
  public Value fItem() { return Num.ZERO; }
  public Value fItemS() { return Num.ZERO; }
  public int arrInfo() { return Pervasion.ARR_BIT; }
  
  public double sum() {
    return isum();
  }
  public int isum() {
    int r = 0;
    setEnd(false);
    for (long l : arr) {
      r+= Long.bitCount(l);
    }
    return r;
  }
  
  public static class BA { // bit adder
    private final long[] a; // no trailing garbage allowed!
    private final int[] sh;
    private int i = 0, o = 0; // index, offset
    private final boolean mode;
    public BA(int[] sh, boolean mode) {
      this.a = new long[sizeof(sh)];
      this.sh = sh;
      this.mode = mode;
      reg = 0;
    }
    
    private static long reg;
    public void add(boolean b) {
      // reg|= (b? 1L : 0L)<<o;
      // if (++o == 64) {
      //   o = 0;
      //   a[i] = reg;
      //   reg = 0;
      //   i++;
      // }
      // a[i]|= (b? 1L : 0L)<<o;
      // if (++o == 64) {
      //   o = 0;
      //   i++;
      // }
      
      reg = reg*2 + (b?1:0);
      if (++o == 64) {
        o = 0;
        a[i] = Long.reverse(reg);
        i++;
      }
    }
    
    public void add(BitArr a) {
      add(a, 0, a.ia);
    }
    
    public void add(BitArr g, int s, int e) {
      copy(g.arr, s, a, i*64+o, e-s);
      skip(e-s);
    }
    
    public void skip(int n) {
      int off = o+n;
      o = off&63;
      i+= off>>6;
    }
    
    public BitArr finish() {
      // System.out.println(i+" "+o+" "+reg);
      // if (mode & o!=0 & i<a.length) a[i] = reg;
      if (mode & o!=0 & i<a.length) a[i] = Long.reverse(reg<<64-o);
      return new BitArr(a, sh);
    }
  }
  
  
  public static void fill(long[] a, int s, int e) {
    int i = s>>6;
    int o = s&63;
    int n = e-s;
    int off = o+n;
    if (off < 64) { // start & end being in the same cell is annoying
      if (n==0) return;
      a[i]|= ((1L<<n)-1) << o;
    } else {
      a[i]|= (~0L) << o;
      int li = i + ((off-1) >> 6);
      for (int j = i+1; j <= li; j++) {
        a[j] = ~0L;
      }
      i+= off>>6;
      o = off&63;
      
      if (o != 0) a[i] = (1L<<o)-1;
    }
  }
  
  public static void copy(long[] src, int srcS, long[] dst, int dstS, int len) {
    //   for (int i = 0; i < len; i++) {
    //     int si = i+srcS;
    //     int di = i+dstS;
    //     dst[di>>6]|= (src[si>>6]>>(si&63)&1) << (di&63);
    //   }
    // }
    // public static void copyn(long[] src, int srcS, long[] dst, int dstS, int len) {
    int s = srcS;
    int e = srcS+len;
    if (len==0) return;
    int o = dstS&63;
    
    // System.out.println();
    // System.out.println("copy "+s+"-"+e+"  to  "+dstS+"-"+(dstS+len));
    // System.out.println("srclen = "+src.length+"; dstlen = "+dst.length);
    // System.out.println("decision on "+o+" "+s);
    
    int sI = dstS>>6;                // first index of where to batch insert; included
    long sV = dst[sI];               // first long
    long sM = (1L<<o) - 1;           // mask of what's already written
    
    int eI = (dstS+len-1) >> 6;      // last index of where to batch insert; included
    long eV = dst[eI];               // last long
    long eM = -(1L << (o+len & 63)); // mask of what's already written
    if (eM==~0L) eM=0;
    // System.out.println("  masks:");
    // System.out.println(str64(sM) + " of "+str64(sV)+" @ "+sI);
    // System.out.println(str64(eM) + " of "+str64(eV)+" @ "+eI);
    // System.out.println("  before:");
    // for (long l : src) System.out.print(str64(l)+"  "); System.out.println();
    // for (long l : dst) System.out.print(str64(l)+"  "); System.out.println();
    int shl = o-(s&63);
    // System.out.println("oshl="+shl);
    if (shl==0) {
      int sp =  s   >>6; // incl
      int ep = (e-1)>>6; // incl
      System.arraycopy(src, sp, dst, sI, ep-sp+1);
    } else {
      int pG = s >> 6;
      if (shl < 0) {
        shl+= 64;
        pG++;
      }
      int shr = 64-shl;
      // System.out.println("shl="+shl+"; shr="+shr);
      // System.out.println(i+"…"+Li+": s="+s+" o="+o+" e="+e+" pG="+pG+" shl="+shl);
      
      /* some unrolling of
           for (int pT = i; pT <= Li; pT++) {
             if (pG<garr.length) a[pT]|= garr[pG]<<shl;
             if (pG-1>=0) a[pT]|= garr[pG-1]>>>shr;
             pG++;
           }
      */
      {
        int pT = sI;
        if (pG< src.length) dst[pT]|= src[pG]<<shl;
        if (pG-1>=0) dst[pT]|= src[pG-1]>>>shr;
        // System.out.println(pT+": "+(pG< src.length)+" "+(pG-1>=0));
        pG++;
      }
      for (int pT = sI+1; pT < eI; pT++) {
        dst[pT]|= src[pG]<<shl;
        dst[pT]|= src[pG-1]>>>shr;
        // System.out.println(pT+": dbl");
        pG++;
      }
      if (sI+1<=eI) {
        int pT = eI;
        if (pG< src.length) dst[pT]|= src[pG]<<shl;
        dst[pT]|= src[pG-1]>>>shr;
        // System.out.println(pT+": "+(pG< src.length));
        pG++;
      }
    }
    
    if (sI == eI) {
      long written = sM|eM;
      dst[sI] = (sV&written) | (dst[sI] & ~written);
    } else {
      dst[sI] = (sV&sM) | (dst[sI]&~sM);
      dst[eI] = (eV&eM) | (dst[eI]&~eM);
    }
    
    // System.out.println("  after:");
    // for (long l : dst) System.out.print(str64(l)+"  ");
    // System.out.println();
  }
  
  public static String str64(long l) {
    StringBuilder t = new StringBuilder(Long.toBinaryString(l));
    while(t.length() < 64) t.insert(0, "0");
    for (int i = 56; i > 0; i-= 8)t.insert(i, '_');
    return t.toString();
  }
  
  public long longFrom(int s) {
    int i1 = s >> 6;
    int i2 = (s+63) >> 6;
    int o1 = s & 63;
    // System.out.printf("%d %d %d %d\n", s, i1, i2, o1);
    if (arr.length == i2) return arr[i1]>>>o1;
    return arr[i1]>>>o1 | arr[i2]<<(64-o1);
  }
  
  public static class BC { // boolean creator
    public final long[] arr;
    final int[] sz;
    public BC(int[] sz) {
      this.sz = sz;
      arr = new long[sizeof(sz)];
    }
    public BitArr finish() {
      // assert (i<<6) + o == Arr.prod(sz); \\ idk man
      return new BitArr(arr, sz);
    }
    
    public void set(int pos) {
      arr[pos>>6]|= 1L<<(pos&63);
    }
    public void clear(int pos) {
      arr[pos>>6]&= ~(1L<<(pos&63));
    }
  }
  public static BC create(int[] sh) {
    return new BC(sh);
  }
  public final class BR { // boolean read
    BitArr outer;
    BR(BitArr outer) {
      this.outer = outer;
    }
    private int i, o = 0;
    public boolean read() {
      boolean r = (arr[i] & 1L<<o) != 0;
      o++;
      // i+= o==64? 1 : 0;
      // o&= 63;
      if (o == 64) {
        o = 0;
        i++;
      }
      return r;
    }
    
    public void skip(int n) {
      int fp = (i<<6) + o + n;
      i = fp>>>6;
      o = fp&63;
    }
  }
  
  public BR read() {
    return new BR(this);
  }
  
  public Value[] valuesClone() {
    Value[] vs = new Value[ia];
    int o = 0;
    for (int i = 0; i < ia/64; i++) {
      long l = arr[i];
      for (int j = 0; j < 64; j++) {
        vs[o++] = Num.NUMS[(int) (l&1)];
        l>>>= 1;
      }
    }
    if (o!=ia) {
      long l = arr[arr.length-1];
      for (int i = 0; i < ia%64; i++) {
        vs[o++] = Num.NUMS[(int) (l&1)];
        l>>>= 1;
      }
    }
    return vs;
  }
  
  public Arr reverseOn(int dim) {
    if (dim!=0 || r()!=1) return super.reverseOn(dim);
    long[] nl = new long[arr.length];
    int off = (64-ia) & 63;
    copy(arr, 0, nl, off, ia);
    for (int i = 0; i < nl.length>>1; i++) { int r = nl.length-i-1;
      long iv = nl[i]; long rv = nl[r];
      nl[i] = Long.reverse(rv);
      nl[r] = Long.reverse(iv);
    }
    if ((nl.length&1) != 0) nl[nl.length>>1] = Long.reverse(nl[nl.length>>1]);
    return new BitArr(nl, shape);
  }
  
  public boolean eq(Value x) {
    if (x instanceof BitArr) {
      if (!Arrays.equals(shape, x.shape)) return false;
      int xh = ((Arr) x).hash;
      if (hash!=0 && xh!=0 && hash!=xh) return false;
      
      BitArr xb = (BitArr) x;
      setEnd(false); xb.setEnd(false);
      for (int i = 0; i < arr.length; i++) if (arr[i] != xb.arr[i]) return false;
      return true;
    }
    return super.eq(x);
  }
}