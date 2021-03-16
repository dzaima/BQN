package BQN.tools;

import BQN.errors.*;

import java.util.*;

public class JBC {
  HashMap<Const, Integer> constants = new HashMap<>();
  static int ctr = 0;
  
  
  
  static final Ldr l = new Ldr();
  static class Ldr extends ClassLoader {
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
  
  
  static void u2(byte[] a, int pos, int v) {
    a[pos  ] = (byte) ((v>>8)&0xff);
    a[pos+1] = (byte) ( v    &0xff);
  }
  static void u4(byte[] a, int pos, int v) {
    a[pos  ] = (byte) ((v>>24)&0xff);
    a[pos+1] = (byte) ((v>>16)&0xff);
    a[pos+2] = (byte) ((v>> 8)&0xff);
    a[pos+3] = (byte) ( v     &0xff);
  }
  
  
  static String name(Class<?> c) {
    return c.getName().replace(".", "/");
  }
  static String fname(Class<?> c) {
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
  static String met(Class<?> ret, Class<?>... args) {
    StringBuilder res = new StringBuilder("(");
    for (Class<?> c : args) res.append(fname(c));
    res.append(')').append(fname(ret));
    return res.toString();
  }
  
  
  static class Const implements Comparable<Const> {
    byte[] bs;
  
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
  
  
  
  class Met extends MutByteArr {
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
  
    void aload(int i) { // get local variable
      localc = Math.max(i+1, localc);
      if (i<4) u(42+i);
      else if (i<256) u(25, i);
      else throw new NYIError("aload>255");
    }
    void astore(int i) { // store local variable
      localc = Math.max(i+1, localc);
      if (i<4) u(75+i);
      else if (i<256) u(58, i);
      else throw new NYIError("astore>255");
    }
    void iload(int i) { // get local var int
      localc = Math.max(i+1, localc);
      if (i<4) u(26+i);
      else if (i<256) u(21, i);
      else throw new NYIError("iload>255");
    }
    
    void aaload () { u(50); } // get array item
    void aastore() { u(83); } // set array item
    
    void aconst_null() { u(1); } // push null
    void iconst(int i) { // push integer constant
      if (i>=-1 && i<=5) u(3+i);
      else if (( byte)i == i) { u(16); s (i); }
      else if ((short)i == i) { u(17); s2(i); }
      else {
        int pos = CONSTANT_Integer(i);
        if (pos<255) { u(18); u (pos); }
        else {         u(19); u2(pos); }
      }
    }
    public void ldc(String str) {
      byte[] bs = new byte[3]; bs[0] = 8;
      JBC.u2(bs, 1, CONSTANT_Utf8(str));
      int v = JBC.this.get(bs);
      if (v < 256) u(18, v);
      else { u(19); u2(v); }
    }
  
    void invvirt (String cls, String name, String type) { u(182); u2(CONSTANT_Methodref(cls, name, type)); } // invoke virtual
    void invspec (String cls, String name, String type) { u(183); u2(CONSTANT_Methodref(cls, name, type)); } // invoke special
    void invstat (String cls, String name, String type) { u(184); u2(CONSTANT_Methodref(cls, name, type)); } // invoke static
    void getfield(String cls, String name, String type) { u(180); u2(CONSTANT_Fieldref (cls, name, type)); } // get field from pop
  
    void invvirt (Class<?> cls, String name, String   type) { invvirt (name(cls), name,       type ); }
    void invspec (Class<?> cls, String name, String   type) { invspec (name(cls), name,       type ); }
    void invstat (Class<?> cls, String name, String   type) { invstat (name(cls), name,       type ); }
    void getfield(Class<?> cls, String name, Class<?> type) { getfield(name(cls), name, fname(type)); }
    
    public void new_     (String cls) { u(187); u2(CONSTANT_Class(cls)); }
    public void anewarray(String cls) { u(189); u2(CONSTANT_Class(cls)); }
    public void is       (String cls) { u(193); u2(CONSTANT_Class(cls)); }
    
    public void new_     (Class<?> cls) { new_     (name(cls)); }
    public void anewarray(Class<?> cls) { anewarray(name(cls)); }
    public void is       (Class<?> cls) { is       (name(cls)); }
    
    public void cast(String cls) {
      u(192);
      u2(CONSTANT_Class(cls));
    }
    public void cast(Class<?> c) { cast(name(c)); }
    
    void vret() { u(177); } // return void
    void aret() { u(176); } // return object
    void athrow(){u(191); } // throw ToS
    
    void swap  () { u( 95); } // ab → ba
    void dup   () { u( 89); } // a → aa
    void dup2  () { u( 92); } // ab → abab
    void dup_x1() { u( 90); } // ab → bab
    void dup_x2() { u( 91); } // abc → cabc
    void pop   () { u( 87); } // .. a → ..
    
    public void ifeq0   (Lbl l) { u(153); l.add2(); }
    public void ifne0   (Lbl l) { u(154); l.add2(); }
    public void iflt0   (Lbl l) { u(155); l.add2(); }
    public void ifge0   (Lbl l) { u(156); l.add2(); }
    public void ifgt0   (Lbl l) { u(157); l.add2(); }
    public void ifle0   (Lbl l) { u(158); l.add2(); }
    public void goto_   (Lbl l) { u(167); l.add2(); }
    public void ifnenull(Lbl l) { u(199); l.add2(); } // branch if pop!=null
    
    
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
    
    void finish() {
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
  
  class Fld {
    public final int acc, name, type;
  
    Fld(int acc, String name, String type) {
      this.acc = acc;
      this.name = CONSTANT_Utf8(name);
      this.type = CONSTANT_Utf8(type);
    }
  }
}
