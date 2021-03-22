package BQN.tools;

import BQN.errors.*;

import java.util.*;

public class JBC {
  HashMap<Const, Integer> constants = new HashMap<>();
  static int ctr = 0;
  public ArrayList<Met> methods = new ArrayList<>();
  public ArrayList<Fld> fields = new ArrayList<>();
  public MutIntArr interfaces = new MutIntArr(1);
  public int access_flags = 0x0021; // access flags; default: ACC_PUBLIC ACC_SUPER
  
  
  public static final Ldr l = new Ldr();
  public static class Ldr extends ClassLoader {
    public Class<?> def(String name, byte[] b, int off, int len) throws ClassFormatError {
      return defineClass(name, b, off, len);
    }
  }
  
  
  
  public int CONSTANT_Utf8(String s) {
    MutByteArr bs = new MutByteArr();
    bs.u(1);
    bs.u2(0); // placeholder
    for (int i = 0; i < s.length(); i++) {
      int c = s.charAt(i)&0xffff;
      if (c<=0x7f && c!=0) {
        bs.u(c);
      } else if (c<=0x07ff) {
        bs.u(0b1100_0000 | c>> 6);
        bs.u(0b1000_0000 | c     & 0b0011_1111);
      } else {
        bs.u(0b1110_0000 | c>>12);
        bs.u(0b1000_0000 | (c>>6)& 0b0011_1111);
        bs.u(0b1000_0000 | c     & 0b0011_1111);
      }
    }
    u2(bs.bs, 1, bs.len-3);
    return get(bs.get());
    // System.out.println(Arrays.toString(sb));
    // byte[] bs = new byte[sb.length+3]; bs[0] = 1; // tag
    // u2(bs, 1, sb.length);
    // ins(bs, 3, sb);
    // return get(bs);
  }
  public int CONSTANT_Class(Class<?> c) {
    return CONSTANT_Class(name(c));
  }
  public int CONSTANT_Class(String s) {
    byte[] bs = new byte[3]; bs[0] = 7; // tag
    u2(bs, 1, CONSTANT_Utf8(s));
    return get(bs);
  }
  public int CONSTANT_NameAndType(String name, String type) {
    byte[] bs = new byte[5]; bs[0] = 12; // tag
    u2(bs, 1, CONSTANT_Utf8(name));
    u2(bs, 3, CONSTANT_Utf8(type));
    return get(bs);
  }
  public int CONSTANT_Methodref(String cls, String name, String type) {
    byte[] bs = new byte[5]; bs[0] = 10; // tag
    u2(bs, 1, CONSTANT_Class(cls));
    u2(bs, 3, CONSTANT_NameAndType(name, type));
    return get(bs);
  }
  public int CONSTANT_Fieldref(String cls, String name, String type) {
    byte[] bs = new byte[5]; bs[0] = 9; // tag
    u2(bs, 1, CONSTANT_Class(cls));
    u2(bs, 3, CONSTANT_NameAndType(name, type));
    return get(bs);
  }
  public int CONSTANT_Integer(int i) {
    byte[] bs = new byte[5]; bs[0] = 3; // tag
    u4(bs, 1, i);
    return get(bs);
  }
  
  public int get(byte[] bs) {
    Const o = new Const(bs);
    Integer r = constants.get(o);
    if (r!=null) return r;
    int n = constants.size()+1;
    if (n==32767) throw new DomainError("java constant pool limit exceeded");
    constants.put(o, n);
    // System.out.println(Arrays.toString(bs)+": "+n);
    return n;
  }
  
  
  public static void u2(byte[] a, int pos, int v) {
    a[pos  ] = (byte) ((v>>8)&0xff);
    a[pos+1] = (byte) ( v    &0xff);
  }
  public static void u4(byte[] a, int pos, int v) {
    a[pos  ] = (byte) ((v>>24)&0xff);
    a[pos+1] = (byte) ((v>>16)&0xff);
    a[pos+2] = (byte) ((v>> 8)&0xff);
    a[pos+3] = (byte) ( v     &0xff);
  }
  
  
  public static String name(Class<?> c) {
    return c.getName().replace(".", "/");
  }
  public static String fname(Class<?> c) {
    if (c.isPrimitive()) {
        if (c==boolean.class) return "Z";
        if (c==byte   .class) return "B";
        if (c==char   .class) return "C";
        if (c==double .class) return "D";
        if (c==float  .class) return "F";
        if (c==int    .class) return "I";
        if (c==long   .class) return "J";
        if (c==short  .class) return "S";
        if (c==void   .class) return "V";
    }
    String n = c.getName().replace('.', '/');
    if (c.isArray()) return n;
    return "L"+n+";";
  }
  public static String met(Class<?> ret, Class<?>... args) {
    StringBuilder res = new StringBuilder("(");
    for (Class<?> c : args) res.append(fname(c));
    res.append(')').append(fname(ret));
    return res.toString();
  }
  
  
  public static class Const implements Comparable<Const> {
    public byte[] bs;
  
    public Const(byte[] bs) { this.bs = bs; }
  
    public int compareTo(JBC.Const o) {
      int c;
      c = Integer.compare(bs.length, o.bs.length);
      if(c!=0) return c;
      for (int i = 0; i < bs.length; i++) {
        c = Byte.compare(bs[i], o.bs[i]);
        if (c!=0) return c;
      }
      return 0;
    }
    
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof Const)) return false;
      Const that = (Const) o;
      
      return Arrays.equals(this.bs, that.bs);
    }
    
    public int hashCode() { return Arrays.hashCode(bs); }
  
    public String toString() {
      return Arrays.toString(bs);
    }
  }
  
  
  
  public class Met extends MutByteArr {
    public final int acc, name, type;
    public int mstack = 0; // max stack size
    public int localc; // local variable count
    
    // public BA smt = new BA(); // StackMapTable bytes
    // public int smtc; // StackMapTable entry count 
  
    public Met(int acc, String name, String type, int argc) { // argc should include `this` if applicable
      this.acc = acc;
      this.name = CONSTANT_Utf8(name);
      this.type = CONSTANT_Utf8(type);
      this.localc = argc; // in case the arguments aren't used
    }
  
    public void aload(int i) { // get local variable
      localc = Math.max(i+1, localc);
      if (i<4) u(42+i);
      else if (i<256) u(25, i);
      else throw new NYIError("aload>255");
    }
    public void astore(int i) { // store local variable
      localc = Math.max(i+1, localc);
      if (i<4) u(75+i);
      else if (i<256) u(58, i);
      else throw new NYIError("astore>255");
    }
    public void iload(int i) { // get local var int
      localc = Math.max(i+1, localc);
      if (i<4) u(26+i);
      else if (i<256) u(21, i);
      else throw new NYIError("iload>255");
    }
    
    public void aaload () { u(50); } // get array item; arr,index → val
    public void aastore() { u(83); } // set array item; arr,index,val → ⟨⟩
    
    public void aconst_null() { u(1); } // push null
    public void iconst(int i) { // push integer constant
      if (i>=-1 && i<=5) u(3+i);
      else if (( byte)i == i) { u(16); s (i); }
      else if ((short)i == i) { u(17); s2(i); }
      else {
        int pos = CONSTANT_Integer(i);
        if (pos<255) { u(18); u (pos); }
        else {         u(19); u2(pos); }
      }
    }
    public void ldc(String str) { // push string constant
      byte[] bs = new byte[3]; bs[0] = 8;
      JBC.u2(bs, 1, CONSTANT_Utf8(str));
      int v = JBC.this.get(bs);
      if (v < 256) u(18, v);
      else { u(19); u2(v); }
    }
  
    public void invvirt  (String cls, String name, String type) { u(182); u2(CONSTANT_Methodref(cls, name, type)); } // invoke virtual
    public void invspec  (String cls, String name, String type) { u(183); u2(CONSTANT_Methodref(cls, name, type)); } // invoke special
    public void invstat  (String cls, String name, String type) { u(184); u2(CONSTANT_Methodref(cls, name, type)); } // invoke static
    public void getfield (String cls, String name, String type) { u(180); u2(CONSTANT_Fieldref (cls, name, type)); } // get field from pop
    public void getstatic(String cls, String name, String type) { u(178); u2(CONSTANT_Fieldref (cls, name, type)); } // get static field from pop
    public void putstatic(String cls, String name, String type) { u(179); u2(CONSTANT_Fieldref (cls, name, type)); } // push static field
  
    public void invvirt  (Class<?> cls, String name, String   type) { invvirt  (name(cls), name,       type ); }
    public void invspec  (Class<?> cls, String name, String   type) { invspec  (name(cls), name,       type ); }
    public void invstat  (Class<?> cls, String name, String   type) { invstat  (name(cls), name,       type ); }
    public void getfield (Class<?> cls, String name, Class<?> type) { getfield (name(cls), name, fname(type)); }
    public void getstatic(Class<?> cls, String name, Class<?> type) { getstatic(name(cls), name, fname(type)); }
    public void putstatic(Class<?> cls, String name, Class<?> type) { putstatic(name(cls), name, fname(type)); }
    
    public void new_     (String cls) { u(187); u2(CONSTANT_Class(cls)); }
    public void anewarray(String cls) { u(189); u2(CONSTANT_Class(cls)); }
    public void is       (String cls) { u(193); u2(CONSTANT_Class(cls)); }
    
    public void new_     (Class<?> cls) { new_     (name(cls)); }
    public void anewarray(Class<?> cls) { anewarray(name(cls)); }
    public void is       (Class<?> cls) { is       (name(cls)); }
    
    public void newarrayb() { u(188); u(4); } // boolean[]
    public void newarrayC() { u(188); u(5); }
    public void newarrayF() { u(188); u(6); }
    public void newarrayD() { u(188); u(7); }
    public void newarrayB() { u(188); u(8); } // byte[]
    public void newarrayS() { u(188); u(9); }
    public void newarrayI() { u(188); u(10); }
    public void newarrayL() { u(188); u(11); }
    
    public void cast(String cls) {
      u(192);
      u2(CONSTANT_Class(cls));
    }
    public void cast(Class<?> c) { cast(name(c)); }
    
    public void vret() { u(177); } // return void
    public void aret() { u(176); } // return object
    public void athrow(){u(191); } // throw ToS
    
    public void swap  () { u( 95); } // ab → ba
    public void dup   () { u( 89); } // a → aa
    public void dup2  () { u( 92); } // ab → abab
    public void dup_x1() { u( 90); } // ab → bab
    public void dup_x2() { u( 91); } // abc → cabc
    public void pop   () { u( 87); } // .. a → ..
    
    public void ifeq0   (Lbl l) { u(153); l.add2(); }
    public void ifne0   (Lbl l) { u(154); l.add2(); }
    public void iflt0   (Lbl l) { u(155); l.add2(); }
    public void ifge0   (Lbl l) { u(156); l.add2(); }
    public void ifgt0   (Lbl l) { u(157); l.add2(); }
    public void ifle0   (Lbl l) { u(158); l.add2(); }
    public void goto_   (Lbl l) { u(167); l.add2(); }
    public void ifnenull(Lbl l) { u(199); l.add2(); } // branch if pop!=null
  
    public void i2f() { u(134); }
    public void i2d() { u(135); }
    public void i2c() { u(146); }
    public void i2b() { u(145); }
    public void i2s() { u(147); }
    public void i2l() { u(133); }
    
    
    int lookupStart;
    public void lookupswitch(Lbl def, int count) {
      lookupStart = len;
      u(171);
      while (len%4!=0) u(0);
      def.add4(len, lookupStart);
      u4(count);
    }
    public void lookup(int match, Lbl to) {
      u4(match);
      to.add4(len, lookupStart);
    }
    
    ArrayList<Lbl> ls = new ArrayList<>();
    public Lbl lbl() {
      Lbl c = new Lbl();
      ls.add(c);
      return c;
    }
  
    public class Lbl {
      MutIntArr fr2 = new MutIntArr(1);
      MutIntArr fr4p= new MutIntArr(1);
      MutIntArr fr4r= new MutIntArr(1);
      int pos = -1;
      public void add2() {
        fr2.add(len);
        u2(65535);
      }
      public void add4(int pos, int rel) {
        fr4p.add(pos);
        fr4r.add(rel);
        u4(-1);
      }
  
      public void here() {
        assert pos==-1;
        pos = len;
      }
    }
  
    public void finish() {
      // ArrayList<Integer> lend = new ArrayList<>();
      for (Lbl l : ls) {
        int to = l.pos;
        if (to==-1 && (l.fr2.sz!=0 || l.fr4p.sz!=0)) throw new ImplementationError("unset label");
        for (int i = 0; i < l.fr2.sz; i++) {
          int from = l.fr2.is[i];
          JBC.u2(bs, from, to-from+1); // TODO split fr2 into p/r
        }
        for (int i = 0; i < l.fr4p.sz; i++) {
          JBC.u4(bs, l.fr4p.is[i], to-l.fr4r.is[i]);
        }
      }
      // Collections.sort(lend);
      // int prev = 0;
      // for (Integer c : lend) {
      //   if (c==prev) continue;
      //   int off = c-prev-1;
      //   if(off>63) throw new NYIError("StackMapTable with delta>63");
      //   smt.b(off);
      //   smtc++;
      //   prev = c;
      // }
    }
  }
  
  public byte[] finish(int this_class, int super_class) {
    int code_att = CONSTANT_Utf8("Code");
    // int smt_att = CONSTANT_Utf8("StackMapTable");
    
    MutByteArr res = new MutByteArr();
    res.u(0xCA,0xFE,0xBA,0xBE); // magic
    res.u2(0x00); res.u2(49); // version
    res.u2(constants.size()+1); // constant count
    
    byte[][] map = new byte[constants.size()][];
    constants.forEach((k, v) -> map[v-1] = k.bs);
    for (byte[] c : map) res.u(c); // constants
    
    res.u2(access_flags);
    res.u2(this_class);
    res.u2(super_class);
    res.u2(interfaces.sz); // interface count
    for (int i = 0; i < interfaces.sz; i++) res.u2(interfaces.is[i]);
    
    res.u2(fields.size()); // field count
    for (Fld f : fields) {
      res.u2(f.acc);
      res.u2(f.name);
      res.u2(f.type);
      res.u2(0); // attribute count
    }
    
    res.u2(methods.size()); // method count
    for (Met c : methods) {
      c.finish();
      if (c.len > 65500) return null;
      res.u2(c.acc);
      res.u2(c.name);
      res.u2(c.type);
      if ((c.acc&0x0500)!=0) {
        res.u2(0); // attribute count; no code
        if (c.len>0) throw new ImplementationError("abstract/native Met had code!");
      } else {
        res.u2(1); // attribute count
        res.u2(code_att);
        res.u4(12+c.len); // 20+c.len+c.smt.len with stackmapable attribute
        res.u2(c.mstack); // max_stack
        res.u2(c.localc); // max locals
        res.u4(c.len);
        res.u(c.get());
        res.u2(0); // exception table length
        // no exceptions
        res.u2(0); // attributes for code
        // no attributes
        // res.u2(1); // attribute count
        // res.u2(smt_att);
        // res.u4(c.smt.len+2);
        // res.u2(c.smtc);
        // res.b(c.smt.get());
      }
    }
    res.u2(0); // attribute count
    return res.get();
  }
  
  public class Fld {
    public final int acc, name, type;
  
    public Fld(int acc, String name, Class<?> cls) {
      this(acc, name, fname(cls));
    }
    public Fld(int acc, String name, String type) {
      this.acc = acc;
      this.name = CONSTANT_Utf8(name);
      this.type = CONSTANT_Utf8(type);
    }
  }
}
