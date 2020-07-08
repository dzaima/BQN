package APL;

import APL.errors.*;
import APL.tokenizer.*;
import APL.tokenizer.types.DfnTok;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.functions.*;
import APL.types.functions.builtins.dops.DepthBuiltin;
import APL.types.functions.builtins.fns2.*;
import APL.types.functions.userDefined.*;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class Scope {
  private final Scope parent;
  public final HashMap<String, Value> vars;
  public final Sys sys;
  public Random rnd;
  public final Value[] args;
  public Scope(Sys s) {
    vars = new HashMap<>();
    sys = s;
    parent = null;
    args = new Value[]{EmptyArr.SHAPE0S, EmptyArr.SHAPE0S};
    rnd = new Random();
  }
  public Scope(Scope p) {
    vars = new HashMap<>();
    sys = p.sys;
    parent = p;
    args = p.args;
    rnd = p.rnd;
  }
  public Scope(Scope inherit, Value[] args) {
    vars = inherit.vars;
    sys = inherit.sys;
    parent = inherit.parent;
    this.args = args;
    rnd = inherit.rnd;
  }
  public Scope owner(String name) {
    if (vars.containsKey(name)) return this;
    else if (parent == null) return null;
    else return parent.owner(name);
  }
  
  public void update(String name, Value val) { // sets wherever var already exists
    Scope sc = owner(name);
    if (sc == null) throw new SyntaxError("No variable '"+name+"' to update", val);
    sc.set(name, val);
  }
  public void set(String name, Value val) { // sets in current scope
    if (name.charAt(0) == '•') {
      switch (name) {
        case "•io":
          if (!val.equals(Num.ZERO)) throw new DomainError("Cannot set •io to "+val);
          break;
        case "•boxsimple":
          Main.enclosePrimitives = val.asInt() == 1;
          break;
        case "•vi":
          Main.vind = Main.bool(val);
          break;
        case "•rl":
          rnd = new Random(val.asInt());
          break;
        case "•pp":
          if (val instanceof Primitive) {
            Num.setPrecision(val.asInt());
          } else {
            int[] args = val.asIntVec();
            if (args.length == 3) Num.setPrecision(args[0], args[1], args[2]);
            else throw new DomainError("•pp expected either a scalar number or array of 3 integers as ⍵", val);
          }
          break;
        default:
          throw new DomainError("setting unknown quad "+name);
      }
    } else {
      if (val == null) vars.remove(name);
      else vars.put(name, val); // +TODO a separate "remove" call
    }
  }
  public Value get(String name) {
    if (name.startsWith("•")) {
      switch (name) {
        case "•millis": return new Num(System.currentTimeMillis() - Main.startingMillis);
        case "•time": return new Timer(true);
        case "•htime": return new Timer(false);
        case "•ex": return new Ex();
        case "•lns": return new Lns();
        case "•sh": return new Shell();
        case "•nc": return new NC();
        case "•a": return Main.uAlphabet;
        case "•av": return Main.toAPL(Main.CODEPAGE);
        case "•d": return Main.digits;
        case "•args": return new HArr(Arrays.copyOf(args, args.length-2));
        case "•path": return args[args.length-1];
        case "•name": return args[args.length-2];
        case "•l":
        case "•la": return Main.lAlphabet;
        case "•erase": return new Eraser();
        case "•gc": System.gc(); return Num.ONE;
        case "•gclog": return new GCLog(this);
        case "•null": return Null.NULL;
        case "•map": case "•NS": return new MapGen();
        case "•dl": return new Delay();
        case "•dr": return new DR();
        case "•ucs": return new UCS();
        case "•hash": return new Hasher();
        case "•io": return Num.ZERO;
        case "•vi": return Main.vind? Num.ONE : Num.ZERO;
        case "•boxsimple": return Main.enclosePrimitives? Num.ONE : Num.ZERO;
        case "•class": return new ClassGetter();
        case "•pp": return new DoubleArr(new double[] {Num.pp, Num.sEr, Num.eEr});
        case "•pfx": return new Profiler(this);
        case "•pfo": return new Profiler.ProfilerOp(this);
        case "•pfc": return new Profiler.ProfilerDop();
        case "•pfr": return Profiler.results();
        case "•stdin": return new Stdin();
        case "•big": return new Big();
        case "•r": return new Dop() {
          public String repr() { return "•_R_"; }
  
          public Value call(Value aa, Value ww, Value w, DerivedDop derv) {
            return Main.toAPL(w.asString().replaceAll(aa.asString(), ww.asString()));
          }
        };
        case "•u": return new Builtin() {
          @Override public String repr() { return "•U"; }
  
          @Override public Value call(Value w) {
            sys.ucmd(w.asString());
            return null;
          }
        };
        case "•comp": return new Builtin() {
          
          public String repr() {
            return "•COMP";
          }
          
          public Value call(Value a, Value w) {
            if (w.ia!=4) throw new LengthError("•COMP: 4 ≠ ≠𝕩", this, w);
            char type = a.ia==0?0: ((Char) a.get(0)).chr;
            if (a.ia!=2 && type!='a') throw new DomainError("𝕨 of •COMP must be an array of type and immediateness, or 'a'");
            boolean imm = type=='a' || Main.bool(a.get(1));
            Value bc = w.get(0);
            Value obj = w.get(1);
            Value str = w.get(2);
            Value dfn = w.get(3);
            
            byte[] bcp = new byte[bc.ia];
            for (int i = 0; i < bcp.length; i++) bcp[i] = (byte) bc.get(i).asInt();
            Token[] ref = new Token[bcp.length]; // keep as nulls for now
            
            Value[] objp = new Value[obj.ia];
            for (int i = 0; i < objp.length; i++) objp[i] = obj.get(i);
            
            String[] strp = new String[str.ia];
            for (int i = 0; i < strp.length; i++) strp[i] = str.get(i).asString();
            
            DfnTok[] dfnp = new DfnTok[dfn.ia];
            for (int i = 0; i < dfnp.length; i++) {
              Value c = dfn.get(i);
              dfnp[i] = c instanceof Dfn? ((Dfn) c).code : c instanceof Ddop? ((Ddop) c).code : ((Dmop) c).code;
            }
            
            Comp c = new Comp(bcp, objp, strp, dfnp, ref, null);
            if (type == 'f') return new Dfn (new DfnTok(c, 'f', imm), Scope.this);
            if (type == 'd') return new Ddop(new DfnTok(c, 'd', imm), Scope.this);
            if (type == 'm') return new Dmop(new DfnTok(c, 'm', imm), Scope.this);
            if (type == 'a') {
              DfnTok t = new DfnTok(c, 'a', imm);
              Scope nsc = new Scope(Scope.this);
              return t.comp.exec(nsc, t.start(nsc, null, null, null, null, Nothing.inst));
            }
            throw new DomainError("•COMP: ⊑𝕨 must be one of \"fdma\"");
          }
        };
        case "•bc": return new Fun() {
          public String repr() {
            return "•BC";
          }
          
          public Value call(Value w) {
            DfnTok s = w instanceof Dfn? ((Dfn) w).code : w instanceof Ddop? ((Ddop) w).code : ((Dmop) w).code;
            return Main.toAPL(s.comp.fmt());
          }
          
        };
        case "•opt": case "•optimize":
          return new Optimizer();
      }
    }
    Value f = vars.get(name);
    if (f == null) {
      if (parent == null) return null;
      else return parent.get(name);
    } else return f;
  }
  Variable getVar(String name) {
    return new Variable(this, name);
  }
  public String toString() {
    return toString("");
  }
  private String toString(String prep) {
    StringBuilder res = new StringBuilder("{\n");
    String cp = prep+"  ";
    for (String n : vars.keySet()) res.append(cp).append(n).append(" ← ").append(get(n)).append("\n");
    if (parent != null) res.append(cp).append("parent: ").append(parent.toString(cp));
    res.append(prep).append("}\n");
    return res.toString();
  }
  
  public double rand(double d) {
    return rnd.nextDouble()*d;
  }
  public long randLong() {
    return rnd.nextLong();
  }
  public int rand(int n) {
    return rnd.nextInt(n);
  }
  
  static class GCLog extends Builtin {
    private final Scope sc;
    
    protected GCLog(Scope sc) {
      this.sc = sc;
    }
    
    @Override public String repr() {
      return "•GCLOG";
    }
    
    @Override
    public Value call(Value w) {
      return new Logger(sc, w.toString());
    }
    static class Logger extends Primitive {
      private final Scope sc;
      final String msg;
      Logger(Scope sc, String s) {
        this.sc = sc;
        this.msg = s;
      }
      
      @SuppressWarnings("deprecation") // this is this things purpose
      protected void finalize() {
        sc.sys.println(msg+" was GCed");
      }
      public String toString() {
        return "•GCLOG["+msg+"]";
      }
      
      @Override
      public Value ofShape(int[] sh) {
        if (sh.length == 0 && !Main.enclosePrimitives) return this;
        assert Arr.prod(sh) == 1;
        return new SingleItemArr(this, sh);
      }
    }
  }
  class Timer extends Builtin {
    public final boolean raw;
    @Override public String repr() {
      return "•TIME";
    }
    Timer(boolean raw) {
      this.raw = raw;
    }
    public Value call(Value w) {
      return call(Num.ONE, w);
    }
    public Value call(Value a, Value w) {
      int[] options = a.asIntVec();
      int n = options[0];
      
      boolean separate = false;
      if (options.length >= 2) separate = options[1]==1;
      
      String test = w.asString();
      
      Comp testCompiled = Comp.comp(Tokenizer.tokenize(test));
      
      if (separate) {
        double[] r = new double[n];
        for (int i = 0; i < n; i++) {
          long start = System.nanoTime();
          testCompiled.exec(Scope.this);  
          long end = System.nanoTime();
          r[i] = end-start;
        }
        return new DoubleArr(r);
      } else {
        long start = System.nanoTime();
        for (int i = 0; i < n; i++) testCompiled.exec(Scope.this);
        long end = System.nanoTime();
        if (raw) {
          return new Num((end-start)/n);
        } else {
          double t = end-start;
          t/= n;
          if (t < 1000) return Main.toAPL(new Num(t)+" nanos");
          t/= 1e6;
          if (t > 500) return Main.toAPL(new Num(t/1000d)+" seconds");
          return Main.toAPL(new Num(t)+" millis");
        }
      }
    }
  }
  class Eraser extends Builtin {
    public String repr() {
      return "•ERASE";
    }
    
    public Value call(Value w) {
      Scope.this.set(w.asString(), null);
      return w;
    }
  }
  static class Delay extends Builtin {
    public String repr() {
      return "•DL";
    }
    
    public Value call(Value w) {
      long nsS = System.nanoTime();
      double ms = w.asDouble() * 1000;
      int ns = (int) ((ms%1)*1000000);
      try {
        Thread.sleep((int) ms, ns);
      } catch (InterruptedException ignored) { /* idk */ }
      return new Num((System.nanoTime() - nsS) / 1000000000d);
    }
  }
  static class UCS extends Builtin {
    public String repr() {
      return "•UCS";
    }
    
    public Value call(Value w) {
      return numChrM(new NumMV() {
        public Value call(Num c) {
          return Char.of((char) c.asInt());
        }
  
        public boolean retNum() {
          return false;
        }
      }, c->Num.of(c.chr), w);
    }
    
    public Value callInv(Value w) {
      return call(w);
    }
  }
  
  private static class MapGen extends Builtin {
    public String repr() {
      return "•MAP";
    }
    
    public Value call(Value w) {
      if (w instanceof StrMap) {
        StrMap wm = (StrMap) w;
        // Scope sc;
        // HashMap<String, Obj> vals;
        // if (wm.sc == null) {
        //   sc = null;
        //   vals = new HashMap<>(wm.vals);
        // } else {
        //   sc = new Scope(wm.sc.parent);
        //   sc.vars.putAll(wm.vals);
        //   vals = sc.vars;
        // }
        // return new StrMap(sc, vals);
        return new StrMap(new HashMap<>(wm.vals));
      }
      StrMap map = new StrMap();
      for (Value v : w) {
        if (v.rank != 1 || v.ia != 2) throw new RankError("•MAP: input pairs should be 2-item vectors", this, v);
        map.set(v.get(0), v.get(1));
      }
      return map;
    }
    
    public Value call(Value a, Value w) {
      if (a.rank != 1) throw new RankError("rank of ⍺ ≠ 1", this, a);
      if (w.rank != 1) throw new RankError("rank of ⍵ ≠ 1", this, w);
      if (a.ia != w.ia) throw new LengthError("both sides lengths should match", this, w);
      StrMap map = new StrMap();
      for (int i = 0; i < a.ia; i++) {
        map.set(a.get(i), w.get(i));
      }
      return map;
    }
  }
  
  private class Optimizer extends Builtin {
    public String repr() {
      return "•OPT";
    }
    
    public Value call(Value w) {
      String name = w.asString();
      Value v = Scope.this.get(name);
      Value optimized = v.squeeze();
      if (v == optimized) return Num.ZERO;
      update(name, optimized);
      return Num.ONE;
    }
  }
  private static class ClassGetter extends Builtin {
    public String repr() {
      return "•CLASS";
    }
    public Value call(Value w) {
      return new ChrArr(w.getClass().getCanonicalName());
    }
  }
  
  private class Ex extends Builtin {
    public String repr() {
      return "•EX";
    }
    
    public Value call(Value w) {
      return call(EmptyArr.SHAPE0S, w);
    }
  
    public Value call(Value a, Value w) {
      String path = w.asString();
      if (a.rank > 1) throw new DomainError("•EX: 𝕨 must be a vector or scalar (had shape "+Main.formatAPL(a.shape)+")");
      return Scope.this.sys.execFile(path, a.values());
    }
  }
  private static class Lns extends Builtin {
    public String repr() {
      return "•LNS";
    }
    
    public Value call(Value w) {
      String path = w.asString();
      String[] a = Main.readFile(path).split("\n");
      Value[] o = new Value[a.length];
      for (int i = 0; i < a.length; i++) {
        o[i] = Main.toAPL(a[i]);
      }
      return Arr.create(o);
    }
    
    String get(APLMap m, String key, String def) {
      Value got = m.getRaw(key);
      if (got != Null.NULL) return got.asString();
      return def;
    }
    
    public Value call(Value a, Value w) {
      if (a instanceof APLMap) {
        try {
          URL url = new URL(w.asString());
          HttpURLConnection conn = (HttpURLConnection) url.openConnection();
          APLMap m = (APLMap) a;
          String content = get(m, "content", "");
          conn.setRequestMethod(get(m, "method", "POST"));
          
          conn.setRequestProperty("Content-Type", get(m, "type", "POST"));
          conn.setRequestProperty("Content-Language", get(m, "language", "en-US"));
          conn.setRequestProperty("Content-Length", Integer.toString(content.length()));
          
          Obj eo = m.getRaw("e");
          if (eo != Null.NULL) {
            APLMap e = (APLMap) eo;
            for (Value k : e.allKeys()) {
              Value v = e.getRaw(k);
              conn.setRequestProperty(k.asString(), v.asString());
            }
          }
          
          Value cache = m.getRaw("cache");
          conn.setUseCaches(cache!=Null.NULL && Main.bool(cache));
          conn.setDoOutput(true);
          
          if (content.length() != 0) {
            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            os.writeBytes(content);
            os.close();
          }
          
          
          InputStream is = conn.getInputStream();
          ArrayList<Value> vs = new ArrayList<>();
          try (BufferedReader rd = new BufferedReader(new InputStreamReader(is))) {
            String ln;
            while ((ln = rd.readLine()) != null) vs.add(Main.toAPL(ln));
          }
          return new HArr(vs);
        } catch (MalformedURLException e) {
          throw new DomainError("bad URL: "+e.getMessage(), this);
        } catch (ProtocolException e) {
          throw new DomainError("ProtocolException: "+e.getMessage(), this);
        } catch (IOException e) {
          throw new DomainError("IOException: "+e.getMessage(), this);
        }
      } else {
        String p = a.asString();
        String s = w.asString();
        try (PrintWriter pw = new PrintWriter(p)) {
          pw.write(s);
        } catch (FileNotFoundException e) {
          throw new DomainError("File "+p+" not found: "+e.getMessage(), this);
        }
        return w;
      }
    }
  }
  
  
  private static class Shell extends Builtin {
    public String repr() {
      return "•SH";
    }
    
    public Value call(Value w) {
      return exec(w, null, null, false);
    }
    
    public Value call(Value a, Value w) {
      APLMap m = (APLMap) a;
      
      File dir = null;
      Value diro = m.getRaw("dir");
      if (diro != Null.NULL) dir = new File(diro.asString());
      
      byte[] inp = null;
      Value inpo = m.getRaw("inp");
      if (inpo != Null.NULL) {
        if (inpo.ia > 0) {
          if (inpo.first() instanceof Char) inp = inpo.asString().getBytes(StandardCharsets.UTF_8);
          else {
            inp = new byte[inpo.ia];
            double[] ds = inpo.asDoubleArr();
            for (int i = 0; i < ds.length; i++) inp[i] = (byte) ds[i];
          }
        }
      }
      
      boolean raw = false;
      Value rawo = m.getRaw("raw");
      if (rawo != Null.NULL) raw = Main.bool(rawo);
      
      return exec(w, dir, inp, raw);
    }
    
    public Value exec(Value w, File f, byte[] inp, boolean raw) {
      try {
        Process p;
        if (w.get(0) instanceof Char) {
          String cmd = w.asString();
          p = Runtime.getRuntime().exec(cmd, new String[0], f);
        } else {
          String[] parts = new String[w.ia];
          for (int i = 0; i < parts.length; i++) {
            parts[i] = w.get(i).asString();
          }
          p = Runtime.getRuntime().exec(parts, new String[0], f);
        }
        Num ret = Num.of(p.waitFor());
        if (inp != null) p.getOutputStream().write(inp);
        byte[] out = readAllBytes(p.getInputStream());
        byte[] err = readAllBytes(p.getErrorStream());
        if (raw) return new HArr(new Value[]{ret, new DoubleArr(out), new DoubleArr(err)});
        else return new HArr(new Value[]{ret, Main.toAPL(new String(out, StandardCharsets.UTF_8)),
                                              Main.toAPL(new String(err, StandardCharsets.UTF_8))});
      } catch (Throwable e) {
        e.printStackTrace();
        return Null.NULL;
      }
    }
    private byte[] readAllBytes(InputStream is) {
      try {
        byte[] res = new byte[512];
        int used = 0;
        read: while (true) {
          while (used < res.length) {
            int n = is.read(res, used, res.length-used);
            if (n==-1) break read;
            used+= n;
          }
          if (used==res.length) res = Arrays.copyOf(res, res.length*2);
        }
        return Arrays.copyOf(res, used);
      } catch (IOException e) {
        throw new DomainError("failed to read I/O", this);
      }
    }
  }
  
  
  private class NC extends Builtin {
    public String repr() {
      return "•NC";
    }
    
    public Value call(Value w) {
      Obj obj = Scope.this.get(w.asString());
      if (obj == null) return Num.ZERO;
      if (obj instanceof Fun  ) return Num.NUMS[3];
      if (obj instanceof Dop  ) return Num.NUMS[4];
      if (obj instanceof Mop  ) return Num.NUMS[5];
      if (obj instanceof Value) return Num.NUMS[2];
      return Num.NUMS[9];
    }
  }
  
  
  private static class Hasher extends Builtin {
    public String repr() {
      return "•HASH";
    }
    public Value call(Value w) {
      return Num.of(w.hashCode());
    }
  }
  private static class Stdin extends Builtin {
    public String repr() {
      return "•STDIN";
    }
    public Value call(Value w) {
      if (w instanceof Num) {
        int n = w.asInt();
        ArrayList<Value> res = new ArrayList<>(n);
        for (int i = 0; i < n; i++) res.add(Main.toAPL(Main.console.nextLine()));
        return new HArr(res);
      }
      if (w.ia == 0) {
        ArrayList<Value> res = new ArrayList<>();
        while (Main.console.hasNext()) res.add(Main.toAPL(Main.console.nextLine()));
        return new HArr(res);
      }
      throw new DomainError("•STDIN needs either ⍬ or a number as ⍵", this);
    }
  }
  
  public static class Profiler extends Builtin {
    private final Scope sc;
    Profiler(Scope sc) {
      this.sc = sc;
    }
    
    static final HashMap<String, Pr> pfRes = new HashMap<>();
    static Value results() {
      Value[] arr = new Value[pfRes.size()*4+4];
      arr[0] = new ChrArr("expr");
      arr[1] = new ChrArr("calls");
      arr[2] = new ChrArr("total ms");
      arr[3] = new ChrArr("avg ms");
      final int[] p = {4};
      ArrayList<String> ks = new ArrayList<>(pfRes.keySet());
      Collections.sort(ks);
      for (String k : ks) {
        Pr pr = pfRes.get(k);
        arr[p[0]++] = Main.toAPL(k);
        arr[p[0]++] = new Num(pr.am);
        arr[p[0]++] = new Num(Math.floor(pr.ms*1e6      )/1e6);
        arr[p[0]++] = new Num(Math.floor(pr.ms*1e6/pr.am)/1e6);
      }
      pfRes.clear();
      return new HArr(arr, new int[]{arr.length>>2, 4});
    }
    
    public String repr() {
      return "•PFX";
    }
    public Value call(Value w) {
      return call(w, w);
    }
    private static Pr pr(Value ko, Value vo) {
      String k = ko.asString();
      Pr p = pfRes.get(k);
      if (p == null) pfRes.put(k, p = new Pr(vo==null? null : Main.comp(vo.asString())));
      return p;
    }
  
    public Value call(Value a, Value w) {
      Pr p = pr(a, w); p.start();
      long sns = System.nanoTime();
      Value res = p.c.exec(sc);
      long ens = System.nanoTime();
      p.end(ens-sns);
      return res;
    }
    
    static class ProfilerOp extends Mop {
      Scope sc;
      public ProfilerOp(Scope sc) {
        this.sc = sc;
      }
      
      Pr pr(Obj f) {
        String s = ((Value) f).asString();
        Pr p = pfRes.get(s);
        if (p == null) {
          pfRes.put(s, p = new Pr(Main.comp(s)));
          p.fn = (Fun) p.c.exec(sc);
        }
        return p;
      }
      
      public Value call(Value f, Value w, DerivedMop derv) {
        Pr p = pr(f); p.start();
        long sns = System.nanoTime();
        Value r = p.fn.call(w);
        long ens = System.nanoTime();
        p.end(ens-sns);
        return r;
      }
      
      public Value call(Value f, Value a, Value w, DerivedMop derv) {
        Pr p = pr(f); p.start();
        long sns = System.nanoTime();
        Value r = p.fn.call(a, w);
        long ens = System.nanoTime();
        p.end(ens-sns);
        return r;
      }
      
      public String repr() {
        return "•_PFO";
      }
    }
    public static class ProfilerDop extends Dop {
      
      public Value call(Value aa, Value ww, Value a, Value w, DerivedDop derv) {
        Pr p = pr(ww, null); Fun f = aa.asFun(); p.start();
        long sns = System.nanoTime();
        Value res = f.call(a, w);
        long ens = System.nanoTime();
        p.end(ens-sns);
        return res;
      }
  
      public Value call(Value aa, Value ww, Value w, DerivedDop derv) {
        Pr p = pr(ww, null); Fun f = aa.asFun(); p.start();
        long sns = System.nanoTime();
        Value res = f.call(w);
        long ens = System.nanoTime();
        p.end(ens-sns);
        return res;
      }
      
      
      public String repr() {
        return "•_PFC_";
      }
    }
  }
  
  private static class Pr {
    private final Comp c;
    private int am;
    private double ms;
    private Fun fn;
    
    public Pr(Comp c) {
      this.c = c;
    }
    
    int lvl;
    public void start() {
      lvl++;
    }
    public void end(long ns) {
      lvl--;
      if (lvl==0) {
        this.ms+= ns/1e6d;
        am++;
      }
    }
  }
  
  private static class Big extends Builtin {
    public Value call(Value w) {
      return rec(w);
    }
    private Value rec(Value w) {
      if (w instanceof Num) return new BigValue(((Num) w).num);
      if (w instanceof Primitive) return w;
      Value[] pa = w.values();
      Value[] va = new Value[pa.length];
      for (int i = 0; i < pa.length; i++) {
        va[i] = rec(pa[i]);
      }
      return HArr.create(va, w.shape);
    }
    
    public Value callInv(Value w) {
      return recN(w);
    }
    private Value recN(Value w) {
      if (w instanceof BigValue) return ((BigValue) w).num();
      if (w instanceof Primitive) return w;
      if (w instanceof DoubleArr) return w;
      Value[] pa = w.values();
      Value[] va = new Value[pa.length];
      for (int i = 0; i < pa.length; i++) {
        va[i] = recN(pa[i]);
      }
      return HArr.create(va, w.shape);
    }
    public String repr() {
      return "•BIG";
    }
  }
  
  private static class DR extends Builtin {
    /*
       0=100| - unknown
       1=100| - bit
       2=100| - char
       3=100| - 64-bit float
       4=100| - map
       5=100| - bigint
       9=100| - null
      
      0=÷∘100 - primitive
      1=÷∘100 - array
    */
    public Value call(Value w) {
      if (w instanceof    BitArr) return Num.of(101);
      if (w instanceof      Char) return Num.of(  2);
      if (w instanceof    ChrArr) return Num.of(102);
      if (w instanceof       Num) return Num.of(  3);
      if (w instanceof DoubleArr) return Num.of(103);
      if (w instanceof    APLMap) return Num.of(  4);
      if (w instanceof  BigValue) return Num.of(  5);
      if (w instanceof      Null) return Num.of(  9);
      if (w instanceof       Arr) return Num.of(100);
      if (w instanceof Primitive) return Num.of(  0);
      return Num.of(200); // idk ¯\_(ツ)_/¯
    }
    public Value call(Value a, Value w) {
      int[] is = a.asIntVec();
      if (is.length != 2) throw new DomainError("•DR expected ⍺ to have 2 items", this);
      int f = is[0];
      int t = is[1];
      if ((f==1 || f==3 || f==5)
       && (t==1 || t==3 || t==5)
       && (f==3 ^ t==3)) { // convert float to/from bits/long
        // if (w instanceof Num) return new BigValue(Double.doubleToLongBits(w.asDouble()), false);
        // return new Num(Double.longBitsToDouble(((BigValue) w).i.longValue()));
        if (t==3) {
          if (f==1) return DepthBuiltin.on(new Fun() {
            public String repr() { return ""; }
            public Value call(Value w) {
              return new Num(Double.longBitsToDouble(((BigValue) UTackBuiltin.on(BigValue.TWO, w, DR.this)).longValue()));
            }
          }, 1, w, this);
          if (f==5) return DepthBuiltin.on(new Fun() {
            public String repr() { return ""; }
            public Value call(Value w) {
              return new Num(Double.longBitsToDouble(((BigValue) w).longValue()));
            }
          }, 0, w, this);
        } else {
          if (t==1) return DepthBuiltin.on(new Fun() {
            public String repr() { return ""; }
            public Value call(Value w) {
              return new BitArr(new long[]{Long.reverse(Double.doubleToRawLongBits(w.asDouble()))}, new int[]{64});
            }
          }, 0, w, this);
          if (t==5) return DepthBuiltin.on(new Fun() {
            public String repr() { return ""; }
            public Value call(Value w) {
              return new BigValue(Double.doubleToRawLongBits(w.asDouble()));
            }
          }, 0, w, this);
        }
      }
      throw new NYIError(a+"•DR not implemented", this);
    }
    public Value callInvW(Value a, Value w) {
      return call(ReverseBuiltin.on(a), w);
    }
    public String repr() {
      return "•DR";
    }
  }
  
}