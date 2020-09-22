package APL;

import APL.errors.*;
import APL.tokenizer.Token;
import APL.tokenizer.types.BlockTok;
import APL.tools.Body;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.callable.*;
import APL.types.callable.builtins.*;
import APL.types.callable.builtins.md2.DepthBuiltin;
import APL.types.callable.builtins.fns.*;
import APL.types.callable.blocks.*;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class Scope {
  public final Scope parent;
  public final Sys sys;
  public Random rnd;
  
  private HashMap<String, Integer> varMap;
  public Value[] vars;
  public String[] varNames;
  //    𝕊𝕩𝕨𝕣𝕗𝕘 | 012345
  // fi ······ | ······ | ······
  // f  012··· | 𝕊𝕩𝕨··· | 𝕊𝕩𝕨···
  // mi ···01· | 𝕣𝕗···· | ···𝕣𝕗·
  // m  01234· | 𝕊𝕩𝕨𝕣𝕗· | 𝕊𝕩𝕨𝕣𝕗·
  // di ···012 | 𝕣𝕗𝕘··· | ···𝕣𝕗𝕘
  // d  012345 | 𝕊𝕩𝕨𝕣𝕗𝕘 | 𝕊𝕩𝕨𝕣𝕗𝕘
  
  public int varAm;
  
  public Scope(Sys s) {
    varMap = null; varNames = new String[1]; vars = new Value[1]; varAm = 0;
    parent = null;
    sys = s;
    rnd = new Random();
  }
  public Scope(Scope p) {
    varMap = null; varNames = new String[1]; vars = new Value[1]; varAm = 0;
    parent = p;
    sys = p.sys;
    rnd = p.rnd;
  }
  public Scope(Scope p, String[] varNames) {
    varMap = null; this.varNames = varNames; vars = new Value[varNames.length]; varAm = varNames.length;
    parent = p;
    sys = p.sys;
    rnd = p.rnd;
  }
  
  public HashMap<String, Integer> varMap() {
    if (varMap==null) {
      varMap = new HashMap<>();
      for (int i = 0; i < varAm; i++) varMap.put(varNames[i], i);
    }
    return varMap;
  }
  public boolean hasMap() {
    return varMap!=null;
  }
  
  public Scope owner(String name) {
    if (name.startsWith("•")) return this;
    Scope c = this;
    while (!c.varMap().containsKey(name)) {
      c = c.parent;
      if (c == null) return null;
    }
    return c;
  }
  public Scope owner(int depth) {
    Scope c = this;
    while (depth--!=0) c = c.parent;
    return c;
  }
  
  public int alloc(String name) {
    if (varAm==vars.length) {
      int nlen = vars.length*2+1;
      vars = Arrays.copyOf(vars, nlen);
      varNames = Arrays.copyOf(varNames, nlen);
    }
    int idx = varAm++;
    varNames[idx] = name;
    if (varMap!=null) varMap.put(name, idx);
    return idx;
  }
  public void removeMap() {
    varMap = null;
  }
  
  
  public void update(String name, Value val) { // sets wherever var already exists
    Scope sc = owner(name);
    if (sc == null) throw new SyntaxError("No variable '"+name+"' to update", val);
    sc.set(name, val);
  }
  public void set(int index, Value val) {
    assert varNames[index] != null;
    vars[index] = val;
  }
  public void set(String key, Value val) { // sets in current scope
    if (key.charAt(0) == '•') {
      switch (key) {
        case "•vi":
          Main.vind = Main.bool(val);
          break;
        case "•compstart":
          Comp.compileStart = Math.max(-1, val.asInt());
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
            else throw new DomainError("•pp expected either a scalar number or array of 3 integers as 𝕩", val);
          }
          break;
        default:
          throw new DomainError("setting unknown quad "+key);
      }
    } else {
      Integer k = varMap().get(key);
      if (k==null) k = alloc(key);
      vars[k] = val;
    }
  }
  
  public Value get(String name) {
    if (name.startsWith("•")) {
      switch (name) {
        case "•millis": return new Num(System.currentTimeMillis() - Main.startingMillis);
        case "•time": return new Timer(this);
        case "•ctime": return new CompTimer(this);
        case "•ex": return new Ex();
        case "•lns": return new Lns();
        case "•sh": return new Shell();
        case "•nc": return new NC();
        case "•a": return Main.uAlphabet;
        case "•av": return Main.toAPL(Main.CODEPAGE);
        case "•d": return Main.digits;
        case "•args": case "•path": case "•name": throw new ImplementationError(name+": should've been handled at compile");
        case "•l":
        case "•la": return Main.lAlphabet;
        case "•erase": return new Eraser();
        case "•gc": System.gc(); return Num.ONE;
        case "•gclog": return new GCLog(this);
        case "•null": return Null.NULL;
        case "•map": case "•NS": return new MapGen();
        case "•dl": return new Delay();
        case "•dr": return new DR();
        case "•as": return new AS();
        case "•ucs": return new UCS();
        case "•hash": return new Hasher();
        case "•vi": return Main.vind? Num.ONE : Num.ZERO;
        case "•class": return new ClassGetter();
        case "•pp": return new DoubleArr(new double[] {Num.pp, Num.sEr, Num.eEr});
        case "•pfx": return new Profiler(this);
        case "•pfo": return new Profiler.ProfilerOp(this);
        case "•pfc": return new Profiler.ProfilerMd2(this);
        case "•pfr": return Profiler.results();
        case "•stdin": return new Stdin();
        case "•big": return new Big();
        case "•ia": return new FnBuiltin() {
          public String repr() { return "•IA"; }

          public Value call(Value x) {
            int[] is = new int[x.ia];
            for (int i = 0; i < is.length; i++) is[i] = x.get(i).asInt();
            return new IntArr(is, x.shape);
          }
        };
        case "•rand": return new FnBuiltin() {
          public Value call(Value x) {
            return RandBuiltin.on(x, Scope.this);
          }
          
          public String repr() {
            return "•RAND";
          }
        };
        case "•r": return new Md2Builtin() {
          public String repr() { return "•_R_"; }
  
          public Value call(Value f, Value g, Value x, Md2Derv derv) {
            return Main.toAPL(x.asString().replaceAll(f.asString(), g.asString()));
          }
        };
        case "•u": return new FnBuiltin() {
          public String repr() { return "•U"; }
  
          public Value call(Value x) {
            sys.ucmd(x.asString());
            return null;
          }
        };
        case "•comp": return new FnBuiltin() {
          
          public String repr() {
            return "•COMP";
          }
          
          public Value call(Value x) {
            return call(Num.ONE, x);
          }
          public Value call(Value w, Value x) {
            if (x.ia!=4) throw new LengthError("•COMP: 4 ≠ ≠𝕩", this, x);
            boolean allowImm = Main.bool(w);
            Value bc = x.get(0);
            Value obj = x.get(1);
            Value str = x.get(2);
            Value blk = x.get(3);
            
            byte[] bcp = new byte[bc.ia];
            for (int i = 0; i < bcp.length; i++) bcp[i] = (byte) bc.get(i).asInt();
            Token[] ref = new Token[bcp.length]; // keep as nulls for now
            
            Value[] objp = new Value[obj.ia];
            for (int i = 0; i < objp.length; i++) objp[i] = obj.get(i);
            
            String[] strp = new String[str.ia];
            for (int i = 0; i < strp.length; i++) strp[i] = str.get(i).asString();
            
            BlockTok[] blkp = new BlockTok[blk.ia];
            for (int i = 0; i < blkp.length; i++) {
              Value c = blk.get(i);
              if (c.ia!=4 && c.ia!=5) throw new DomainError("•COMP: ¬∧´(≠3⊑𝕩)∊4‿5", this);
              
              int type = c.get(0).asInt();
              boolean imm = Main.bool(c.get(1));
              
              if (type<0 || type>2) throw new DomainError("•COMP: ⊑𝕨 must be one 0, 1 or 2", this);
              char typec = type==0? (imm?'a':'f') : type==1? 'm' : 'd';
              
              if (c.ia==4) {
                int off = c.get(2).asInt();
                int lvarAm = c.get(3).ia;
                String[] lvars = new String[lvarAm];
                for (int j = 0; j < lvarAm; j++) lvars[j] = c.get(3).get(j).asString();
                
                blkp[i] = new BlockTok(typec, imm, off, lvars);
              } else {
                BlockTok r = new BlockTok(typec, imm);
                ArrayList<Body> bs = r.bodies;
                int[] offs = c.get(2).asIntVec();
                for (int j = 0; j < offs.length; j++) {
                  Value v = c.get(3).get(j);
                  String[] lvars = new String[v.ia];
                  for (int k = 0; k < lvars.length; k++) lvars[k] = v.get(k).asString();
                  char a = ((Char) c.get(4).get(j)).chr;
                  bs.add(new Body(r, typec, imm, offs[j], lvars, a));
                }
                blkp[i] = r;
              }
            }
            
            Comp c = new Comp(bcp, objp, strp, blkp, ref, Token.COMP);
            for (BlockTok block : blkp) block.comp = c;
            if (!allowImm) {
              BlockTok f = new BlockTok(((Char) blk.get(0).get(0)).chr, false, blk.get(0).get(2).asInt(), null);
              f.comp = c;
              return f.eval(Scope.this);
            }
            return blkp[0].eval(Scope.this);
          }
        };
        case "•bc": return new Fun() {
          public String repr() {
            return "•BC";
          }
          
          public Value call(Value x) {
            return call(Num.MINUS_ONE, x);
          }
          
          public Value call(Value w, Value x) {
            if (w instanceof Num) w = new IntArr(new int[]{w.asInt(), 10});
            BlockTok s = x instanceof FunBlock? ((FunBlock) x).code : x instanceof Md2Block? ((Md2Block) x).code : x instanceof Md1Block? ((Md1Block) x).code : null;
            if (s != null) return Main.toAPL(s.comp.fmt(w.get(0).asInt(), w.get(1).asInt()));
            return call(w, Scope.this.get("•comp").call(Num.ZERO, x));
          }
          
        };
        case "•opt": case "•optimize":
          return new Optimizer();
      }
    }
    Scope c = this;
    while (true) {
      Integer pos = c.varMap().get(name);
      if (pos!=null && c.vars[pos]!=null) return c.vars[pos]; // TODO remove c.vars[pos]!=null once LOC_ usage is finished
      c = c.parent;
      if (c == null) return null;
    }
  }
  public Value getC(String name) {
    Value got = get(name);
    if (got == null) throw new SyntaxError("Unknown variable \"" + name + "\"");
    return got;
  }
  
  public String toString() {
    return toString("");
  }
  private String toString(String prep) {
    StringBuilder res = new StringBuilder("{\n");
    String cp = prep+"  ";
    for (int i = 0; i < varAm; i++) {
      res.append(cp).append(varNames[i]).append(" ← ").append(vars[i]).append("\n");
    }
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
  
  public Value getL(int depth, int n) {
    return owner(depth).vars[n];
  }
  
  static class GCLog extends Fun {
    public String repr() {
      return "•GCLOG";
    }
    
    private final Scope sc;
    protected GCLog(Scope sc) {
      this.sc = sc;
    }
    
    
    public Value call(Value x) {
      return new Logger(sc, x.toString());
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
  
      public boolean eq(Value o) { return this == o; }
      public int hashCode() { return actualHashCode(); }
    }
  }
  static class Timer extends Fun {
    public String repr() { return "•TIME"; }
    
    private final Scope sc;
    Timer(Scope sc) { this.sc = sc; }
  
    public Value call(Value x) {
      return call(Num.ONE, x);
    }
    public Value call(Value w, Value x) {
      int[] options = w.asIntVec();
      int n = options[0];
      int mode = options.length>=2? options[1] : 0;
      String test = x.asString();
      Comp testCompiled = Main.comp(test, sc, null);
      
      if (mode==2) {
        double[] r = new double[n];
        for (int i = 0; i < n; i++) {
          long start = System.nanoTime();
          testCompiled.exec(sc);
          long end = System.nanoTime();
          r[i] = end-start;
        }
        return new DoubleArr(r);
      } else {
        long sns = System.nanoTime();
        for (int i = 0; i < n; i++) testCompiled.exec(sc);
        long ens = System.nanoTime();
        double ns = (ens-sns) / (double)n;
        if (mode==1) return new Num(ns);
        else return formatTime(ns);
      }
    }
  }
  
  
  static class CompTimer extends Fun {
    public String repr() { return "•CTIME"; }
    
    private final Scope sc;
    public CompTimer(Scope sc) { this.sc = sc; }
    
    public Value call(Value w, Value x) {
      int[] options = w.asIntVec();
      int n = options[0];
      int mode = options.length>=2? options[1] : 0;
      String str = x.asString();
      
      if (mode==2) {
        double[] r = new double[n];
        for (int i = 0; i < n; i++) {
          long start = System.nanoTime();
          Main.comp(str, sc, null);
          long end = System.nanoTime();
          r[i] = end-start;
        }
        return new DoubleArr(r);
      } else {
        long sns = System.nanoTime();
        for (int i = 0; i < n; i++) Main.comp(str, sc, null);
        long ens = System.nanoTime();
        double ns = (ens-sns) / (double)n;
        if (mode==1) return new Num(ns);
        else return formatTime(ns);
      }
    }
  }
  public static Value formatTime(double ns) {
    if (ns < 1000) return Main.toAPL(Num.format(ns, 3, -99, 99)+"ns");
    double ms = ns/1e6;
    if (ms > 500) return Main.toAPL(Num.format(ms/1000d, 3, -99, 99)+" seconds");
    return Main.toAPL(Num.format(ms, 3, -99, 99)+"ms");
  }
  
  
  class Eraser extends FnBuiltin { // leaves a hole in the local variable map and probably breaks many things; TODO should maybe be a ucmd?
    public String repr() { return "•ERASE"; }
    
    public Value call(Value x) {
      String k = x.asString();
      Scope o = owner(k);
      if (o==null) return Num.ZERO;
      int p = o.varMap().get(k);
      o.vars[p] = null;
      return Num.ONE;
    }
  }
  static class Delay extends FnBuiltin {
    public String repr() {
      return "•DL";
    }
    
    public Value call(Value x) {
      long nsS = System.nanoTime();
      double ms = x.asDouble() * 1000;
      int ns = (int) ((ms%1)*1000000);
      try {
        Thread.sleep((int) ms, ns);
      } catch (InterruptedException ignored) { /* idk */ }
      return new Num((System.nanoTime() - nsS) / 1000000000d);
    }
  }
  static class UCS extends FnBuiltin {
    public String repr() {
      return "•UCS";
    }
    
    public Value call(Value x) {
      return numChrM(new NumMV() {
        public boolean retNum() { return false; }
        public Value call(Num x) {
          return Char.of((char) x.asInt());
        }
        public Value call(int[] x, int[] sh) {
          char[] cs = new char[x.length];
          for (int i = 0; i < cs.length; i++) cs[i] = (char) x[i];
          return new ChrArr(cs, sh);
        }
      }, c->Num.of(c.chr), x);
    }
    
    public Value callInv(Value x) {
      return call(x);
    }
  }
  
  private static class MapGen extends FnBuiltin {
    public String repr() { return "•MAP"; }
    
    public Value call(Value x) {
      if (x instanceof StrMap) {
        StrMap wm = (StrMap) x;
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
      for (Value v : x) {
        if (v.rank != 1 || v.ia != 2) throw new RankError("•MAP: input pairs should be 2-item vectors", this, v);
        map.set(v.get(0), v.get(1));
      }
      return map;
    }
    
    public Value call(Value w, Value x) {
      if (w.rank != 1) throw new RankError("rank of 𝕨 ≠ 1", this, w);
      if (x.rank != 1) throw new RankError("rank of 𝕩 ≠ 1", this, x);
      if (w.ia != x.ia) throw new LengthError("both sides lengths should match", this, x);
      StrMap map = new StrMap();
      for (int i = 0; i < w.ia; i++) {
        map.set(w.get(i), x.get(i));
      }
      return map;
    }
  }
  
  private class Optimizer extends FnBuiltin {
    public String repr() { return "•OPT"; }
    
    public Value call(Value x) {
      String name = x.asString();
      Value v = Scope.this.get(name);
      Value optimized = v.squeeze();
      if (v == optimized) return Num.ZERO;
      update(name, optimized);
      return Num.ONE;
    }
  }
  private static class ClassGetter extends FnBuiltin {
    public String repr() { return "•CLASS"; }
    
    public Value call(Value x) {
      return new ChrArr(x.getClass().getCanonicalName());
    }
  }
  
  private class Ex extends FnBuiltin {
    public String repr() { return "•EX"; }
    
    public Value call(Value x) {
      return call(EmptyArr.SHAPE0S, x);
    }
    
    public Value call(Value w, Value x) {
      String path = x.asString();
      if (w.rank > 1) throw new DomainError("•EX: 𝕨 must be a vector or scalar (had shape "+Main.formatAPL(w.shape)+")");
      return Scope.this.sys.execFile(path, w.values(), new Scope(Scope.this));
    }
  }
  private static class Lns extends FnBuiltin {
    public String repr() { return "•LNS"; }
    
    public Value call(Value x) {
      String path = x.asString();
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
    
    public Value call(Value w, Value x) {
      if (w instanceof APLMap) {
        try {
          URL url = new URL(x.asString());
          HttpURLConnection conn = (HttpURLConnection) url.openConnection();
          APLMap m = (APLMap) w;
          String content = get(m, "content", "");
          conn.setRequestMethod(get(m, "method", "POST"));
          
          conn.setRequestProperty("Content-Type", get(m, "type", "POST"));
          conn.setRequestProperty("Content-Language", get(m, "language", "en-US"));
          conn.setRequestProperty("Content-Length", Integer.toString(content.length()));
          
          Value eo = m.getRaw("e");
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
        String p = w.asString();
        String s = x.asString();
        try (PrintWriter pw = new PrintWriter(p)) {
          pw.write(s);
        } catch (FileNotFoundException e) {
          throw new DomainError("File "+p+" not found: "+e.getMessage(), this);
        }
        return x;
      }
    }
  }
  
  
  private static class Shell extends FnBuiltin {
    public String repr() { return "•SH"; }
    
    public Value call(Value x) {
      return exec(x, null, null, false);
    }
    
    public Value call(Value w, Value x) {
      APLMap m = (APLMap) w;
      
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
            int[] ds = inpo.asIntArr();
            for (int i = 0; i < ds.length; i++) inp[i] = (byte) ds[i];
          }
        }
      }
      
      boolean raw = false;
      Value rawo = m.getRaw("raw");
      if (rawo != Null.NULL) raw = Main.bool(rawo);
      
      return exec(x, dir, inp, raw);
    }
    
    public Value exec(Value w, File f, byte[] inp, boolean raw) {
      try {
        Process p;
        if (w.get(0) instanceof Char) {
          String cmd = w.asString();
          p = Runtime.getRuntime().exec(cmd, EmptyArr.NOSTRS, f);
        } else {
          String[] parts = new String[w.ia];
          for (int i = 0; i < parts.length; i++) {
            parts[i] = w.get(i).asString();
          }
          p = Runtime.getRuntime().exec(parts, EmptyArr.NOSTRS, f);
        }
        Num ret = Num.of(p.waitFor());
        if (inp != null) p.getOutputStream().write(inp);
        byte[] out = readAllBytes(p.getInputStream());
        byte[] err = readAllBytes(p.getErrorStream());
        if (raw) return new HArr(new Value[]{ret, new IntArr(out), new IntArr(err)});
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
  
  
  private class NC extends FnBuiltin {
    public String repr() { return "•NC"; }
    
    public Value call(Value x) {
      Value o = Scope.this.get(x.asString());
      if (o == null) return Num.ZERO;
      if (o instanceof Fun) return Num.NUMS[3];
      if (o instanceof Md2) return Num.NUMS[4];
      if (o instanceof Md1) return Num.NUMS[5];
      return Num.NUMS[2];
    }
  }
  
  
  private static class Hasher extends FnBuiltin {
    public String repr() { return "•HASH"; }
    
    public Value call(Value x) {
      return Num.of(x.hashCode());
    }
  }
  private static class Stdin extends FnBuiltin {
    public String repr() { return "•STDIN"; }
    public Value call(Value x) {
      if (x instanceof Num) {
        int n = x.asInt();
        ArrayList<Value> res = new ArrayList<>(n);
        for (int i = 0; i < n; i++) res.add(Main.toAPL(Main.console.nextLine()));
        return new HArr(res);
      }
      if (x.ia == 0) {
        ArrayList<Value> res = new ArrayList<>();
        while (Main.console.hasNext()) res.add(Main.toAPL(Main.console.nextLine()));
        return new HArr(res);
      }
      throw new DomainError("•STDIN needs either ⟨⟩ or a number as 𝕩", this);
    }
  }
  
  public static class Profiler extends Fun {
    public String repr() { return "•PFX"; }
    
    private final Scope sc;
    Profiler(Scope sc) { this.sc = sc; }
    
    static final HashMap<String, Pr> pfRes = new HashMap<>();
    static Value results() {
      Value[] arr = new Value[pfRes.size()*4+4];
      arr[0] = new ChrArr("expr");
      arr[1] = new ChrArr("calls");
      arr[2] = new ChrArr("total ms");
      arr[3] = new ChrArr("avg ms");
      final int[] p = {4};
      ArrayList<String> ks = new ArrayList<>(pfRes.keySet());
      // ks.sort(Comparator.comparingDouble(a -> -pfRes.get(a).ms));
      ks.sort((a, b) -> Double.compare(pfRes.get(b).ms, pfRes.get(a).ms));
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
    
    public Value call(Value x) {
      return call(x, x);
    }
    public static Pr pr(Value ko, Value vo, Scope sc) {
      String k = ko.asString();
      Pr p = pfRes.get(k);
      if (p == null) pfRes.put(k, p = new Pr(vo==null? null : Main.comp(vo.asString(), sc, null)));
      return p;
    }
    
    public Value call(Value w, Value x) {
      Pr p = pr(w, x, sc); p.start();
      long sns = System.nanoTime();
      Value res = p.c.exec(sc);
      long ens = System.nanoTime();
      p.end(ens-sns);
      return res;
    }
    
    static class ProfilerOp extends Md1 {
      Scope sc;
      public ProfilerOp(Scope sc) {
        this.sc = sc;
      }
      
      Pr pr(Value f) {
        String s = f.asString();
        Pr p = pfRes.get(s);
        if (p == null) {
          pfRes.put(s, p = new Pr(Main.comp(s, sc, null)));
          p.fn = p.c.exec(sc);
        }
        return p;
      }
      
      public Value call(Value f, Value x, Md1Derv derv) {
        Pr p = pr(f); p.start();
        long sns = System.nanoTime();
        Value r = p.fn.call(x);
        long ens = System.nanoTime();
        p.end(ens-sns);
        return r;
      }
      
      public Value call(Value f, Value w, Value x, Md1Derv derv) {
        Pr p = pr(f); p.start();
        long sns = System.nanoTime();
        Value r = p.fn.call(w, x);
        long ens = System.nanoTime();
        p.end(ens-sns);
        return r;
      }
      
      public String repr() {
        return "•_PFO";
      }
    }
    public static class ProfilerMd2 extends Md2 {
      public String repr() { return "•_PFC_"; }
      
      private final Scope sc;
      ProfilerMd2(Scope sc) { this.sc = sc; }
      
      public Value call(Value f, Value g, Value w, Value x, Md2Derv derv) {
        Pr p = pr(g, null, sc); p.start();
        long sns = System.nanoTime();
        Value res = f.call(w, x);
        long ens = System.nanoTime();
        p.end(ens-sns);
        return res;
      }
      
      public Value call(Value f, Value g, Value x, Md2Derv derv) {
        Pr p = pr(g, null, sc); p.start();
        long sns = System.nanoTime();
        Value res = f.call(x);
        long ens = System.nanoTime();
        p.end(ens-sns);
        return res;
      }
    }
  }
  
  public static class Pr {
    private final Comp c;
    private int am;
    private double ms;
    private Value fn;
    
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
  
  private static class Big extends FnBuiltin {
    public String repr() { return "•BIG"; }
    
    public Value call(Value x) {
      return rec(x);
    }
    private Value rec(Value x) {
      if (x instanceof Num) return new BigValue(((Num) x).num);
      if (x instanceof Primitive) return x;
      Value[] pa = x.values();
      Value[] va = new Value[pa.length];
      for (int i = 0; i < pa.length; i++) {
        va[i] = rec(pa[i]);
      }
      return HArr.create(va, x.shape);
    }
    
    public Value callInv(Value x) {
      return recN(x);
    }
    private Value recN(Value x) {
      if (x instanceof BigValue) return ((BigValue) x).num();
      if (x instanceof Primitive) return x;
      if (x.quickDoubleArr()) return x;
      Value[] pa = x.values();
      Value[] va = new Value[pa.length];
      for (int i = 0; i < pa.length; i++) va[i] = recN(pa[i]);
      return HArr.create(va, x.shape);
    }
  }
  
  static class AS extends FnBuiltin {
    /*
      0 - bit booleans
      1 - 32-bit ints
      2 - 64-bit float
     */
    public Value call(Value w, Value x) {
      int t = w.asInt();
      switch (t) { default: throw new DomainError("•AS: expected 𝕨∊↕3 (𝕨="+w+")", this);
        case 0:
          BitArr.BA res = new BitArr.BA(x.shape);
          for (int c : x.asIntArr()) res.add(Main.bool(c));
          return res.finish();
        case 1:
          return new IntArr(x.asIntArr(), x.shape);
        case 2:
          return new DoubleArr(x.asDoubleArr(), x.shape);
      }
    }
    
    public String repr() {
      return "•AS";
    }
  }
  
  static class DR extends FnBuiltin {
    public String repr() { return "•DR"; }
    
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
    public Value call(Value x) {
      if (x instanceof    BitArr) return Num.of(101);
      if (x instanceof      Char) return Num.of(  2);
      if (x instanceof    ChrArr) return Num.of(102);
      if (x instanceof       Num) return Num.of(  3);
      if (x instanceof DoubleArr) return Num.of(103);
      if (x instanceof    APLMap) return Num.of(  4);
      if (x instanceof  BigValue) return Num.of(  5);
      if (x instanceof      Null) return Num.of(  9);
      if (x instanceof       Arr) return Num.of(100);
      if (x instanceof Primitive) return Num.of(  0);
      return Num.of(200); // idk ¯\_(ツ)_/¯
    }
    public Value call(Value w, Value x) {
      int[] is = w.asIntVec();
      if (is.length != 2) throw new DomainError("•DR: 𝕨 must have 2 items", this);
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
            public Value call(Value x) {
              return new Num(Double.longBitsToDouble(((BigValue) UTackBuiltin.on(BigValue.TWO, x, DR.this)).longValue()));
            }
          }, 1, x, this);
          if (f==5) return DepthBuiltin.on(new Fun() {
            public String repr() { return ""; }
            public Value call(Value x) {
              return new Num(Double.longBitsToDouble(((BigValue) x).longValue()));
            }
          }, 0, x, this);
        } else {
          if (t==1) return DepthBuiltin.on(new Fun() {
            public String repr() { return ""; }
            public Value call(Value x) {
              return new BitArr(new long[]{Long.reverse(Double.doubleToRawLongBits(x.asDouble()))}, new int[]{64});
            }
          }, 0, x, this);
          if (t==5) return DepthBuiltin.on(new Fun() {
            public String repr() { return ""; }
            public Value call(Value x) {
              return new BigValue(Double.doubleToRawLongBits(x.asDouble()));
            }
          }, 0, x, this);
        }
      }
      throw new NYIError(w+"•DR not implemented", this);
    }
    public Value callInvX(Value w, Value x) {
      return call(ReverseBuiltin.on(w), x);
    }
  }
  
}