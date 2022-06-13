using System.Collections.Generic;
using System.Collections;
using System;
using System.Text;
using System.Numerics;
using System.Linq;
using System.Text.RegularExpressions;
using System.Threading;
using BQN;
using BQN.tools;
using BQN.types;
using BQN.errors;
using BQN.types.arrs;

namespace CS {
  class Program {
    static void Main(string[] args) {
      BQN.Main.main(args);
    }
  }
  
  public class SysCS : Sys {
    // Action<string> pr;
    // Func<string> rd;
    // BQN(Action<string> pr, Func<string> rd) {
    //   this.pr = pr;
    //   this.rd = rd;
    // }
    public override void println(String s) {
      Console.WriteLine(s);
    }
    public override bool hasInput() { return true; }
    public override String input() {
      return "";
    }
    
    public override void off(int code) {
      
    }
    public string eval(string s) {
      return Main.exec(s, csc, new Value[0]).ToString();
    }
    public object fn(string s, object o) {
      return unwr(((Fun)Main.exec(s, csc, new Value[0])).call(wr(o)));
    }
    public object tp(object o) {
      if (o == null) return "[null]";
      return o.GetType().ToString();
    }
    static object unwr(Value o) {
      if (o is Primitive) {
        if (o is Num n) return n.num;
        if (o is Chr c) return c.chr;
        if (o is PWr p) return p.o;
        return "[unknown primitive "+(o.GetType())+"]";
      } else if (o is Arr) {
        if (o.ia == 0) return Array.CreateInstance(typeof(object), o.shape);
        int i = 0;
        if (o.r()==1) {
          object[] x = new object[o.ia];
          foreach (Value c in o) x[i++] = unwr(c);
          return x;
        } else {
          Array a = Array.CreateInstance(typeof(object), o.shape);
          foreach (int[] cp in new Indexer(o.shape)) a.SetValue(unwr(o.get(i++)), cp);
          return a;
        }
      }
      return o;
    }
    static Value wr(object o) {
      if (o is Value) return o as Value;
      if (o is Array arr) {
        int[] sh = new int[arr.Rank];
        for (int i = 0; i < arr.Rank; i++) sh[i] = arr.GetLength(i);
        Value[] ps = new Value[arr.Length];
        int j = 0;
        foreach (object c in arr) {
          ps[j++] = wr(c);
        }
        return new HArr(ps, sh);
      }
      if (o is string c0) return new ChrArr(c0);
      if (o is Int32  c1) return new Num(c1);
      if (o is Int16  c2) return new Num(c2);
      if (o is sbyte  c3) return new Num(c3);
      if (o is float  c4) return new Num(c4);
      if (o is char   c5) return new Chr(c5);
      return new PWr(o);
    }
  }
  public class PWr : Primitive {
    override public string ln(FmtInfo f) { return "(PWr)"; }
    override public Value pretty(FmtInfo f) { return new ChrArr("(PWr)"); }
    public object o;
    public PWr(object o) {
      this.o = o;
    }
    public override bool eq(Value v) {
      if (v is PWr th) return o.Equals(th.o);
      return false;
    }
    public override int GetHashCode() {
      return o.GetHashCode();
    }
  }
  
  public interface Iterable<T>: IEnumerable<T> {
    Iterator<T> iterator();
  }
  public class List<T> : System.Collections.Generic.List<T> {
    public List() { }
    public List(System.Collections.Generic.List<T> p) : base(p) { }
    public T remove(int i) {
      T r = this[i];
      RemoveAt(i);
      return r;
    }
    public T get(int i) {
      return this[i];
    }
    public T[] toArray(T[] blank) {
      return ToArray();
    }
    public List<T> subList(int s, int e) {
      return new List<T>(GetRange(s, e-s));
    }
    public bool isEmpty() {
      return Count == 0;
    }
  }
  public class ArrayList<T> : CS.List<T> {
    public ArrayList() { }
    public ArrayList(int init) { }
    // public ArrayList(System.Collections.Generic.List<T> p) : base(p) { }
    public ArrayList(System.Collections.Generic.ICollection<T> p) : base(p.ToList()) { }
  }
  public class LinkedList<T> : System.Collections.Generic.LinkedList<T> {
    public T removeLast() {
      T r = Last.Value;
      RemoveLast();
      return r;
    }
    public T removeFirst() {
      T r = First.Value;
      RemoveFirst();
      return r;
    }
    public void Add(T t) {
      AddLast(t);
    }
    public void Add(int i, T t) {
      if (i==Count) AddLast(t);
      else if (i==0) AddFirst(t);
      else {
        AddBefore(getN(i), t);
      }
    }
    public T get(int x) {
      return getN(x).Value;
    }
    public LinkedListNode<T> getN(int x) {
      LinkedListNode<T> c = First;
      for (int i = 0; i < x; i++) c = c.Next;
      return c;
    }
    public T remove(int i) {
      T r = get(i);
      Remove(r);
      return r;
    }
    public new string ToString() {
      StringBuilder b = new StringBuilder("[");
      foreach (T t in this) {
        b.Append(t.ToString());
        b.Append(", ");
      }
      b.Remove(b.Length-2, 2);
      b.Append("]");
      return b.ToString();
    }
  }
  public interface Iterator<T> {
    bool hasNext();
    T next();
  }
  public class RuntimeException : Exception {
    public RuntimeException() {}
    public RuntimeException(string s) : base(s) {}
    public void initCause(Exception e) { }
  }
  public class OutOfMemoryError : Exception { }
  public class StackOverflowError : Exception { }
  public class NumberFormatException : Exception { }
  public static class BR {
    public static BigInteger ONE = BigInteger.One;
    public static BigInteger ZERO = BigInteger.Zero;
    public static BigInteger cr(string s) {
      return BigInteger.Parse(s);
    }
    public static BigInteger cr(int i) {
      return new BigInteger(i);
    }
    public static int intValue(this BigInteger i) {
      return (int) i;
    }
    public static double doubleValue(this BigInteger i) {
      return (double) i;
    }
    public static long longValue(this BigInteger i) {
      return (long) i;
    }
    public static int signum(this BigInteger i) {
      return i.Sign;
    }
    public static BigInteger negate(this BigInteger i) { return -i; }
    public static BigInteger Abs(this BigInteger i) { return i<0?-i:i; }
    public static BigInteger multiply (this BigInteger a, BigInteger b) { return a*b; }
    public static BigInteger Add      (this BigInteger a, BigInteger b) { return a+b; }
    public static BigInteger divide   (this BigInteger a, BigInteger b) { return a/b; }
    public static BigInteger remainder(this BigInteger a, BigInteger b) { return a%b; }
    public static BigInteger subtract (this BigInteger a, BigInteger b) { return a-b; }
    public static BigInteger gcd      (this BigInteger a, BigInteger b) { return BigInteger.GreatestCommonDivisor(a, b); }
    
    public static BigInteger shiftLeft (this BigInteger a, int b) { return a<<b; }
    public static BigInteger shiftRight(this BigInteger a, int b) { return a>>b; }
    public static bool Equals (this BigInteger a, BigInteger b) { return a==b; }
    
    public static BigInteger pow(this BigInteger i, int pw) {
      return BigInteger.Pow(i, pw);
    }
    public static BigInteger[] divideAndRemainder(this BigInteger a, BigInteger b) {
      BigInteger rem;
      BigInteger div = BigInteger.DivRem(a,b,out rem);
      return new BigInteger[]{div, rem};
    }
    public static int bitLength(this BigInteger i) {
      return 0;
    }
    public static bool testBit(this BigInteger i, int x) {
      return (i>>x) != 0;
    }
    public static BigInteger valueOf(string s) {
      return cr(s);
    }
    public static BigInteger valueOf(long l) {
      return new BigInteger(l);
    }
    
    /*
    public static BigInteger (this BigInteger i) {
      
    }
    */
  }
  public class InternalError : BQNError {
    public InternalError(String s) : base(s) { }
  }
  // public class AssertionError : RuntimeException {
  //   public AssertionError() { }
  //   public AssertionError(String s) : base(s) { }
  // }
  public class IllegalStateException : RuntimeException {
    public IllegalStateException(String s) : base(s) { }
    public IllegalStateException() { }
  }
  public class InterruptedException : RuntimeException {}
  public class Scanner {
    public Scanner(string s) {}
    public string nextLine() {
      return Console.ReadLine();
    }
    public bool hasNext() { return true; }
    public bool hasNextLine() { return true; }
  }
  public class FileOutputStream {
    public FileOutputStream(string s) { }
    public void write(sbyte[] bs) { }
    public void close() { }
  }
  public class InputStream {
    
    public int read() {
      return -1;
    }
    public sbyte[] readAllBytes() {
      return null;
    }
  }
  public class Error : Exception {
    public Error(string s) : base(s) {
      
    }
  }
  public class HashMap<K, V> : Dictionary<K, V> {
    public void put(K k, V v) {
      if (k==null) return;
      if (ContainsKey(k)) Remove(k);
      if (v==null) return;
      Add(k, v);
    }
    public void remove(K k) {
      Remove(k);
    }
    public V get(K k) {
      return ContainsKey(k)? this[k] : default(V);
    }
    public void putIfAbsent(K k, V v) {
      if (ContainsKey(k)) return;
      this[k] = v;
    }
    public ArrayList<K> keySet() {
      return new ArrayList<K>(Keys);
    }
  }
  public class File {
    public string s;
    public File(string s) { this.s = s; }
    public File toPath() { return this; }
    
    // extremely incomplete:
    public bool isAbsolute() { return true; }
    public File normalize() { return this; }
    public File toAbsolutePath() { return this; }
    public File getParent() {
      string c = s.EndsWith("/")? s.Substring(0, s.Length-1) : s;
      return new File(c.Substring(0, c.LastIndexOf('/')));
    }
    public string getFileName() { return s; }
    public File resolve(File c) { return new File(c.s[0]=='/'? c.s : s+"/"+c.s); }
    public File resolve(string c) { return new File(c[0]=='/'? c : s+"/"+c); }
 override     public string ToString() { return s; }
  }
  public static class Files {
    public static sbyte[] readAllBytes(File f) {
      return (sbyte[])(object) System.IO.File.ReadAllBytes(f.s);
    }
    public static void write(File f, sbyte[] bs) {
      throw new NYIError("nyi file write");
    }
    public static List<string> readAllLines(File f, string charset) {
      return new ArrayList<string>(SR.cr(readAllBytes(f), charset).Split("\n"));
    }
  }
  public static class Paths {
    public static File get(string s) {
      return new File(s);
    }
  }
  
  public static class LR {
    public static long reverse(long l_) { ulong l = (ulong)l_;
      l = (l>> 1)&0x5555555555555555ul  |  (l<< 1)&0xaaaaaaaaaaaaaaaaUL;
      l = (l>> 2)&0x3333333333333333ul  |  (l<< 2)&0xccccccccccccccccUL;
      l = (l>> 4)&0x0f0f0f0f0f0f0f0ful  |  (l<< 4)&0xf0f0f0f0f0f0f0f0UL;
      l = (l>> 8)&0x00ff00ff00ff00fful  |  (l<< 8)&0xff00ff00ff00ff00UL;
      l = (l>>16)&0x0000ffff0000fffful  |  (l<<16)&0xffff0000ffff0000UL;
      l = (l>>32)&0x00000000fffffffful  |  (l<<32)&0xffffffff00000000UL;
      return (long)l;
    }
    public static int bitCount(long l_) { ulong l = (ulong)l_;
      ulong r = l - ((l >> 1) & 0x5555555555555555UL);
      r = (r&0x3333333333333333UL) + ((r>>2) & 0x3333333333333333UL);
      return (byte)(unchecked(((r+(r>>4)) & 0xF0F0F0F0F0F0F0FUL) * 0x101010101010101UL) >> 56);
    }
    public static string toBinaryString(long l) {
      return Convert.ToString(l, 2);
    }
    public static int numberOfTrailingZeros(long l) {
        uint i = (uint)l;
        return i == 0 ? 32 + itz((uint)(l>>32)) : itz(i);
    }
    public static int itz(uint i) {
      // HD, Count trailing 0's
      i = ~i & (i - 1);
      if (((int)i) <= 0) return (int)(i & 32u);
      uint n = 1;
      if (i > 1u<<16) { n+= 16; i>>= 16; }
      if (i > 1u<< 8) { n+=  8; i>>=  8; }
      if (i > 1u<< 4) { n+=  4; i>>=  4; }
      if (i > 1u<< 2) { n+=  2; i>>=  2; }
      return (int)(n + (i>>1));
    }
  }
  public static class IR {
    public static int MAX_VALUE = int.MaxValue;
    public static int MIN_VALUE = int.MinValue;
    public static string toHexString(int i) {
      return i.ToString("X");
    }
    public static int parseInt(string s) {
      return Int32.Parse(s);
    }
    public static long nextLong(this Random r) {
      return (long)((((uint)r.Next()|0UL)<<32) | (uint)r.Next());
    }
    public static int nextInt(this Random r) {
      return r.Next();
    }
    public static int nextInt(this Random r, int e) {
      return r.Next(e);
    }
    public static double nextDouble(this Random r) {
      return r.NextDouble();
    }
    public static int Add(int a, int b) {
      return a+b;
    }
    public static int sub(int a, int b) {
      return a-b;
    }
    public static int mul(int a, int b) {
      return a*b;
    }
    public static int fmod(int a, int b) {
      int r = a%b;
      if ((r^b) < 0 && r!=0) return r+b;
      return r;
    }
    public static int compare(int a, int b) {
      return a>b? 1 : a<b? -1 : 0;
    }
    
    public static void sleep(int a, int b) {
      Thread.Sleep(a);
    }
    public static string ToString(int i) {
      return i.ToString();
    }
    // public static string toString(int i, int b) {
    //   return Convert.ToString(i, b);
    // }
  }
  public static class DWR {
    public static double POSITIVE_INFINITY = Double.PositiveInfinity;
    public static double NEGATIVE_INFINITY = Double.NegativeInfinity;
    public static long doubleToLongBits(double d) {
      return BitConverter.DoubleToInt64Bits(d);
    }
    public static bool isInfinite(double n) { return n==POSITIVE_INFINITY || n==NEGATIVE_INFINITY; }
    public static bool isNaN(double n) { return n!=n; }
    public static long doubleToRawLongBits(double d) {
      return BitConverter.DoubleToInt64Bits(d);
    }
    public static double longBitsToDouble(long l) {
      return BitConverter.Int64BitsToDouble(l);
    }
    public static double parseDouble(string s) {
      return Double.Parse(s);
    }
    public static int GetHashCode(double d) {
      return d.GetHashCode();
    }
    public static int compare(double a, double b) {
      return a>b? 1 : a<b? -1 : 0;
    }
  }
  public static class StandardCharsets {
    public static readonly string UTF_8 = "utf8";
  }
  public static class SR {
    public static string cr(sbyte[] bs, string cs) {
      return System.Text.Encoding.UTF8.GetString((byte[]) (Array)bs);
    }
    public static string cr(char[] cs) {
      return new string(cs);
    }
    public static string cr(char[] cs, int s, int e) {
      return new string(cs, s, e-s);
    }
    public static string valueOf(object o) {
      return o.ToString();
    }
    public static void deleteCharAt(this StringBuilder b, int i) {
      b.Remove(i, 1);
    }
    public static void delete(this StringBuilder b, int s, int e) {
      b.Remove(s, e-s);
    }
    public static T[] clone<T>(this T[] a) {
      return (T[]) a.Clone();
    }
    public static int codePointAt(this string s, int i) {
      return Char.ConvertToUtf32(s, i);
    }
    public static int codePointCount(this string i, int s, int e) {
      int r = 0;
      while (s<e) {
        s+= CR.charCount(i[s]);
        r++;
      }
      return r;
    }
    public static string substring(this string i, int s, int e) {
      return i.Substring(s, e-s);
    }
    public static string substring(this string i, int s) {
      return i.Substring(s);
    }
    public static int indexOf(this string i, string s) {
      return i.IndexOf(s, StringComparison.OrdinalIgnoreCase);
    }
    public static int indexOf(this string i, string s, int p) {
      return i.IndexOf(s, p, StringComparison.OrdinalIgnoreCase);
    }
    public static int indexOf(this string s, char c) {
      for (int i = 0; i < s.Length; i++) if (s[i]==c) return i;
      return -1;
    }
    public static void getChars(this string s, int ss, int se, char[] da, int ds) {
      for (int i = 0; i < se-ss; i++) da[i+ds] = s[i+ss];
    }
    public static string format(string s, params object[] o) {
      return string.Format(s, o);
    }
    public static sbyte[] getBytes(this string s) {
      return (sbyte[]) (Array)Encoding.UTF8.GetBytes(s);
    }
    public static string replaceAll(this string s, string o, string r) {
      return Regex.Replace(s, o, r);
    }
  }
  public static class CR {
    public static bool isHighSurrogate(char c) {
      return c>=55296 && c < 56320;
    }
    public static bool isLowSurrogate(char ch) {
        return ch>='\uDC00' && ch<'\uE000';
    }
    public static char ToUpper(char c) { return Char.ToUpper(c); }
    public static char ToLower(char c) { return Char.ToLower(c); }
    public static bool isUpperCase(char c) { return Char.IsUpper(c); }
    public static bool isLowerCase(char c) { return Char.IsLower(c); }
    public static int charCount(int i) { return i>=0x010000? 2 : 1; }
    public static int compare(char a, char b) {
      return a.CompareTo(b);
    }
    public static char[] toChars(int i) {
      char[] a = new char[charCount(i)];
      if (a.Length==1) a[0] = (char)i;
      else {
        a[0] = (char) ((i>>10) + ('\uD800'-(0x010000>>10)));
        a[1] = (char) ((i&0x3ff) + '\uDC00');
      }
      return a;
    }
    public static char[] toCharArray(this string s) {
      char[] res = new char[s.Length];
      for (int i = 0; i < res.Length; i++) res[i] = s[i];
      return res;
    }
  }
  
  public class CSOut {
    public static void println(object o) {
      Console.WriteLine(o);
      Console.Out.Flush();
    }
    public static void println() {
      Console.WriteLine();
      Console.Out.Flush();
    }
    public static void print(object o) {
      Console.Write(o);
      Console.Out.Flush();
    }
  }
  /*
  class Test {
    void f(int a, int b, int c) {
      aaa: if(a==b){return;}
      lbl: for (int i = 0; i < a; i++) {
        lblS:;
        if (i==b) goto lblS;
        if (i==c) goto lblE;
      }
      lblE:;
    }
    static void Main(string[] args) {
    }
  }
  interface Itf<T> {
    T f(T x);
  }
  class X<T> : Itf<T> where T : Test {
    public T f(T t) {
      return null;⎉
    }
  }
  class A : Itf<Test> {
    public Test f(Test t) {
      return null;
    }
  }*/
  public static class Arrays {
    public static T[] copyOf<T>(T[] a, int len) {
      //Console.WriteLine(a.Length+" "+len);
      T[] res = new T[len];
      if (len > a.Length) {
        Array.Copy(a, 0, res, 0, a.Length);
      } else Array.Copy(a, 0, res, 0, len);
      return res;
    }
    public static T[] copyOfRange<T>(T[] a, int s, int e) {
      T[] res = new T[e-s];
      Array.Copy(a, s, res, 0, e-s);
      return res;
    }
    public static T[] fill<T>(T[] a, T i) {
      Array.Fill(a, i);
      return a;
    }
    public static T[] fill<T>(T[] a, int s, int e, T i) {
      Array.Fill(a, i, s, e-s);
      return a;
    }
    public static void Sort<T>(T[] a) {
      Array.Sort(a);
    }
    public static List<T> asList<T>(T[] a) {
      ArrayList<T> l = new ArrayList<T>();
      foreach (T t in a) l.Add(t);
      return l;
    }
    
    public static void Sort<T> (T[] a, System.Collections.Generic.IComparer<T> c) { // https://stackoverflow.com/a/148123 https://creativecommons.org/licenses/by-sa/3.0/
        var keys = new KeyValuePair<int, T>[a.Length];
        for (var i = 0; i < a.Length; i++) keys[i] = new KeyValuePair<int, T>(i, a[i]);
        Array.Sort(keys, a, new StabilizingComparer<T>(c));
    }
    private sealed class StabilizingComparer<T> : IComparer<KeyValuePair<int, T>>  {
        private readonly System.Collections.Generic.IComparer<T> c;
        public StabilizingComparer(System.Collections.Generic.IComparer<T> c) {
            this.c = c;
        }
        public int Compare(KeyValuePair<int, T> x, KeyValuePair<int, T> y) {
            var result = c.Compare(x.Value, y.Value);
            return result != 0 ? result : x.Key.CompareTo(y.Key);
        }
    }

    public static bool Equals<T>(T[] a, T[] b) {
      if (a.Length!=b.Length) return false;
      for (int i = 0; i < a.Length; i++) if (!a[i].Equals(b[i])) return false;
      return true;
    }
    
    public static string ToString<T>(T[] a) {
      return "["+string.Join(", ", a.Select(c => c.ToString()).ToArray())+"]";
    }
    
    
    public static void printStackTrace(this Exception e) {
      Console.WriteLine(e.ToString());
    }
  }
  public static class Collections {
    public static void addAll<T>(this ICollection<T> l, List<T> a) {
      foreach (T t in a) l.Add(t);
    }
    public static void addAll<T>(List<T> l, T[] a) {
      foreach (T t in a) l.Add(t);
    }
    public static void shuffle<T>(List<T> l, Random r) {
      // do nothing.. noone will notice, shh
    }
    public static void reverse<T>(List<T> l) {
      l.Reverse();
    }
  }
  public class JBQNComp {
    public BQN.tools.JFn r;
    public JBQNComp(BQN.Comp c, int start) {
      
    }
  }
  public class Cmp<T> : IComparer<T> {
    private Func<T, T, int> cmp;
    public Cmp(Func<T, T, int> cmp) {
        this.cmp = cmp;
    }
    public static IComparer<T> cr(Func<T, T, int> cmp) {
        return new Cmp<T>(cmp);
    }
    public int Compare(T a, T b) {
        return cmp(a, b);
    }
}
}
/*namespace System.Math {
  int abs(int x) {
    
  }
}*/
