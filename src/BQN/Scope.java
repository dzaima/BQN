package BQN;

import BQN.errors.*;
import BQN.tokenizer.Token;
import BQN.tokenizer.types.*;
import BQN.tools.*;
import BQN.types.*;
import BQN.types.arrs.*;
import BQN.types.callable.*;
import BQN.types.callable.blocks.*;
import BQN.types.callable.builtins.*;
import BQN.types.callable.builtins.fns.*;
import BQN.types.callable.builtins.md2.DepthBuiltin;
import BQN.types.callable.trains.*;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;


public final class Scope {
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
    if (sc == null) throw new SyntaxError("No variable '"+name+"' to update");
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
        default:
          throw new DomainError("Setting undefined system value "+key);
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
        case "•nanos": return new Num(System.nanoTime() - Main.startingNanos);
        case "•time": return new Timer(this);
        case "•ctime": return new CompTimer(this);
        case "•ex": return new Ex(this);
        case "•import": return new Import(this);
        case "•pretty": return new Pretty(this);
        case "•out": return new Out(this);
        case "•type": return new Type();
        case "•dr": return new DR();
        case "•glyph": return new Glyph();
        case "•source": return new Source();
        case "•decompose": return new Decompose();
        case "•lns": return new Lns();
        case "•fchars": return new FIO('c');
        case "•flines": return new FIO('l');
        case "•fbytes": return new FIO('b');
        case "•sh": return new Shell();
        case "•a": return Main.uAlphabet;
        case "•av": return Main.toAPL(Main.CODEPAGE);
        case "•d": return Main.digits;
        case "•args": case "•path": case "•name": throw new ImplementationError(name+": should've been handled at compile");
        case "•l":
        case "•la": return Main.lAlphabet;
        case "•erase": return new Eraser();
        case "•gc": System.gc(); return new Num(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());
        case "•gclog": return new GCLog(this);
        case "•null": return Null.NULL;
        case "•map": case "•NS": return new MapGen();
        case "•dl": return new Delay();
        case "•ucs": return new UCS();
        case "•hash": return new Hasher();
        case "•vi": return Main.vind? Num.ONE : Num.ZERO;
        case "•class": return new ClassGetter();
        case "•pfx": return new Profiler(this);
        case "•pfo": return new Profiler.ProfilerOp(this);
        case "•pfc": return new Profiler.ProfilerMd2(this);
        case "•pfr": return Profiler.results();
        case "•stdin": return new Stdin();
        case "•big": return new Big();
        case "•math": return MathNS.INSTANCE;
        case "•rand": return new FnBuiltin() {
          public String ln(FmtInfo f) { return "•RAND"; }
          
          public Value call(Value x) {
            return RandBuiltin.on(x, Scope.this);
          }
        };
        case "•r": return new Md2Builtin() {
          public String ln(FmtInfo f) { return "•_R_"; }
          
          public Value call(Value f, Value g, Value x, Md2Derv derv) {
            return Main.toAPL(x.asString().replaceAll(f.asString(), g.asString()));
          }
        };
        case "•u": return new FnBuiltin() {
          public String ln(FmtInfo f) { return "•U"; }
          
          public Value call(Value x) {
            sys.ucmd(x.asString());
            return null;
          }
        };
        case "•fillfn": return new Fun() {
          public String ln(FmtInfo f) { return "•_FillFn_"; }
          public Value call(Value x) {
            return x.fItem();
          }
          public Value call(Value w, Value x) {
            if (x instanceof Arr) return CustomFillArr.of((Arr) x, w.fMineS());
            return x;
          }
        };
        case "•fillby": return new Md2Builtin() {
          public String ln(FmtInfo f) { return "•_fillBy_"; }
          
          public Value call(Value f, Value g, Value x, Md2Derv derv) {
            Value r = f.call(x);
            Value xf = x.fItemS();
            if (r instanceof Arr && xf!=null) {
              try {
                return CustomFillArr.of((Arr) r, g.call(xf).fMineS());
              } catch (Throwable ignored) { }
            }
            return r;
          }
          
          public Value call(Value f, Value g, Value w, Value x, Md2Derv derv) {
            Value r = f.call(w, x);
            Value xf = x.fItemS();
            Value wf = w.fItemS();
            if (r instanceof Arr && xf!=null) {
              try {
                return CustomFillArr.of((Arr) r, g.call(wf, xf).fMineS());
              } catch (Throwable ignored) { }
            }
            return r;
          }
        };
        case "•comp": return new FnBuiltin() {
          public String ln(FmtInfo f) { return "•Comp"; }
          
          /* Argument structure:
               total: ⟨bytecode ⋄ constants ⋄ inner blocks ⋄ main block ⋄ bodies ⋄ [sind [⋄ eind] ⋄ src]⟩
               block: ⟨type ⋄ immediateness ⋄ monadic ⋄ dyadic⟩
               body : ⟨start ⋄ vars ⋄ [exportMask]⟩
           */
          public Value call(Value x) {
            return call(Num.ONE, x);
          }
          public Value call(Value w, Value x) {
            boolean allowImm = Main.bool(w);
            Value bc = x.get(0);
            Value obj = x.get(1);
            Value blk = x.get(2);
            Value out = x.get(3);
            Value bdy = x.get(4);
            Value inds = x.ia<6?null:x.get(5);
            Value inde = x.ia<6?null:x.get(x.ia<=7?5:6); // incl
            Value src  = x.ia<6?null:x.get(x.ia<=7?6:7);
            
            int[] bcp = bc.asIntVec();
            Token[] ref = new Token[bcp.length];
            if(inds!=null) {
              int[] is = inds.asIntArr();
              int[] ie = inde.asIntArr();
              String srcS = src.asString();
              for (int i = 0; i < is.length; i++) ref[i] = new CompToken(srcS, is[i], ie[i]+1);
            }
            
            
            Value[] objp = new Value[obj.ia];
            for (int i = 0; i < objp.length; i++) objp[i] = obj.get(i);
            
            Body[] bodies = new Body[bdy.ia];
            for (int i = 0; i < bodies.length; i++) {
              Value bd = bdy.get(i);
              int off = bd.get(0).asInt();
              Value[] vno = bd.get(1).values();
              String[] vns = new String[vno.length];
              for (int j = 0; j < vno.length; j++) vns[j] = vno[j].asString();
              int[] exp = bd.ia >= 3? SlashBuiltin.on(bd.get(2), null).asIntArr() : null;
              bodies[i] = new Body(off, vns, exp);
            }
            
            BlockTok[] blocks = new BlockTok[blk.ia];
            BlockTok outBlock = null;
            ArrayList<BlockTok> newBlocks = new ArrayList<>();
            for (int i = 0; i < blocks.length+1; i++) {
              Value bl = i>=blocks.length? out : blk.get(i);
              BlockTok r;
              if (bl instanceof BlockTok.Wrapper) {
                r = ((BlockTok.Wrapper) bl).tk;
              } else {
                int type = bl.get(0).asInt();
                boolean imm = Main.bool(bl.get(1));
                if (type<0 || type>2) throw new DomainError("•COMP: type must be one of 0, 1 or 2", this);
                char typec = type==0? (imm?'a':'f') : type==1? 'm' : 'd';
                int[] mi = bl.get(2).asIntVec();
                int[] di = bl.get(3).asIntVec();
                Body[] mb = new Body[mi.length]; for (int j = 0; j < mi.length; j++) mb[j] = bodies[mi[j]];
                Body[] db = new Body[di.length]; for (int j = 0; j < di.length; j++) db[j] = bodies[di[j]];
                r = new BlockTok(typec, imm, mb, db);
                newBlocks.add(r);
              }
              if (i>=blocks.length) outBlock = r;
              else blocks[i] = r;
            }
            Comp c = new Comp(bcp, objp, blocks, ref, Token.COMP);
            for (BlockTok block : newBlocks) block.comp = c;
            if (!allowImm) return new BlockTok.Wrapper(outBlock);
            return outBlock.eval(Scope.this);
          }
          
          public Value callInv(Value x) { //noinspection ConstantConditions
            return Scope.this.get("•decomp").call(x);
          }
        };
        case "•decomp": return new FnBuiltin() {
          public String ln(FmtInfo f) { return "•Decomp"; }
          
          public Value call(Value x) {
            BlockTok bt = BlockTok.get(x, this);
            BlockTok[] blksP = bt.comp.blocks;
            // Value[] blksN = new Value[blksP.length + 1];
            // for (int i = 0; i < blksP.length; i++) blksN[i] = new BlockTok.Wrapper(blksP[i]);
            Value[] blksN = new Value[blksP.length];
            for (int i = 0; i < blksP.length; i++) blksN[i] = new BlockTok.Wrapper(blksP[i]);
            
            ArrayList<Value> bodies = new ArrayList<>();
            HashMap<Body, Integer> bodyMap = new HashMap<>();
            int[] mbs = body(bodies, bodyMap, bt.bdM);
            int[] dbs = body(bodies, bodyMap, bt.bdD);
            Token[] ref    = bt.comp.ref;
            IntArr bcR     = new IntArr(bt.comp.bc);
            HArr   objR    = new HArr(bt.comp.objs);
            HArr   blksR   = new HArr(blksN);
            Value  blkR    = new HArr(new Value[]{Num.NUMS[bt.type=='m'? 1 : bt.type=='d'? 2 : 0], bt.immediate? Num.ONE : Num.ZERO, new IntArr(mbs), new IntArr(dbs)});
            HArr   bodiesR = new HArr(bodies);
            
            int[] inds = new int[ref.length];
            int[] inde = new int[ref.length];
            int cIs=0, cIe=-1;
            String src = null;
            for (int i = 0; i < ref.length; i++) {
              if (ref[i]!=null) {
                cIs = ref[i].spos;
                cIe = ref[i].epos;
                if(src==null) src=ref[i].raw;
                else if (!src.equals(ref[i].raw)) { cIe=-1; break; }
              }
              inds[i] = cIs;
              inde[i] = cIe-1;
            }
            if (cIe!=-1) return new HArr(new Value[]{bcR, objR, blksR, blkR, bodiesR, new IntArr(inds), new IntArr(inde), new ChrArr(src)});
            else         return new HArr(new Value[]{bcR, objR, blksR, blkR, bodiesR});
          }
          
          private int[] body(ArrayList<Value> bodies, HashMap<Body, Integer> map, Body[] bs) {
            int[] r = new int[bs.length];
            for (int i = 0; i < bs.length; i++) {
              Body c = bs[i];
              Integer p = map.get(c);
              if (p==null) {
                p = bodies.size();
                map.put(c, p);
                Value[] vars = new Value[c.vars.length];
                for (int j = 0; j < c.vars.length; j++) vars[j] = new ChrArr(c.vars[j]);
                bodies.add(new HArr(new Value[]{Num.of(c.start), new HArr(vars)})); // TODO export stuff maybe?
              }
              r[i] = p;
            }
            return r;
          }
          
          public Value callInv(Value x) { //noinspection ConstantConditions
            return Scope.this.get("•comp").call(x);
          }
        };
        case "•bc": return new Fun() {
          public String ln(FmtInfo f) { return "•BC"; }
          
          public Value call(Value x) {
            return call(Num.MINUS_ONE, x);
          }
          
          public Value call(Value w, Value x) {
            if (w instanceof Num) w = new IntArr(new int[]{w.asInt(), 10});
            BlockTok s = BlockTok.get(x, null);
            if (s != null) return Main.toAPL(s.comp.fmt(w.get(0).asInt(), w.get(1).asInt()));
            return call(w, Scope.this.get("•comp").call(Num.ZERO, x));
          }
          
        };
        case "•opt": case "•optimize":
          return new Optimizer();
        default:
          throw new DomainError("Undefined system value "+name);
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
    public String ln(FmtInfo f) { return "•GCLOG"; }
    
    private final Scope sc;
    protected GCLog(Scope sc) {
      this.sc = sc;
    }
    
    
    public Value call(Value x) {
      return new Logger(sc, x.ln(sc.sys.fi));
    }
    static class Logger extends Primitive {
      public String ln(FmtInfo f) { return "•GCLOG["+msg+"]"; }
      public Value pretty(FmtInfo f) { return new ChrArr("•GCLOG["+msg+"]"); }
      
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
      
      public boolean eq(Value o) { return this == o; }
      public int hashCode() { return actualHashCode(); }
    }
  }
  static class Timer extends Fun {
    public String ln(FmtInfo f) { return "•TIME"; }
    
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
      Comp.SingleComp testCompiled = Main.comp(test, sc, null);
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
    public String ln(FmtInfo f) { return "•CTIME"; }
    
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
    if (ns < 1000) return Main.toAPL(Num.format(ns, 3, 99, 99)+"ns");
    double ms = ns/1e6;
    if (ms > 500) return Main.toAPL(Num.format(ms/1000d, 3, 99, 99)+" seconds");
    return Main.toAPL(Num.format(ms, 3, 99, 99)+"ms");
  }
  
  private static class CustomFillArr extends Arr {
    public final Arr v;
    private final Value fill;
    public CustomFillArr(Arr r, Value fill) {
      super(r.shape, r.ia);
      this.v = r;
      this.fill = fill;
    }
    
    public static Value of(Arr base, Value fill) {
      if (base.quickDepth1()) {
        assert base.fItemS().eq(fill);
        return base;
      }
      if (base instanceof CustomFillArr) return new CustomFillArr(((CustomFillArr) base).v, fill);
      if (base.ia==0) return new EmptyArr(base.shape, fill);
      return new CustomFillArr(base, fill);
    }
    
    public Arr v(Arr v) { return new CustomFillArr(v, fill); }
    
    public Value fItemS() { return fill; }
    public Value fMineS() { return v(((Arr) super.fMineS())); }
    
    public Value            get(int i) { return v.get(i);             }
    public Arr      reverseOn(int dim) { return v(v.reverseOn(dim));  }
    public Value     ofShape(int[] sh) { return v((Arr)v.ofShape(sh));}
    public int              hashCode() { return v.hashCode();         }
    public String           asString() { return v.asString();         }
    public Value[]            values() { return v.values();           }
    public Value[]       valuesClone() { return v.valuesClone();      }
    public int[]            asIntArr() { return v.asIntArr();         }
    public int[]       asIntArrClone() { return v.asIntArrClone();    }
    public double[]      asDoubleArr() { return v.asDoubleArr();      }
    public double[] asDoubleArrClone() { return v.asDoubleArrClone(); }
    public double                sum() { return v.sum();              }
    public long[]         asBitLongs() { return v.asBitLongs();       }
    public boolean    quickDoubleArr() { return v.quickDoubleArr();   }
    public boolean       quickIntArr() { return v.quickIntArr();      }
    public boolean       quickDepth1() { return v.quickDepth1();      }
    public int               arrInfo() { return v.arrInfo();          }
    public Iterator<Value>  iterator() { return v.iterator();         }
  }
  
  
  class Eraser extends FnBuiltin { // leaves a hole in the local variable map and probably breaks many things; TODO should maybe be a ucmd?
    public String ln(FmtInfo f) { return "•ERASE"; }
    
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
    public String ln(FmtInfo f) { return "•DL"; }
    
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
    public String ln(FmtInfo f) { return "•UCS"; }
    
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
    public String ln(FmtInfo f) { return "•MAP"; }
    
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
        if (v.r() != 1 || v.ia != 2) throw new RankError("•MAP: input pairs should be 2-item vectors", this);
        map.set(v.get(0), v.get(1));
      }
      return map;
    }
    
    public Value call(Value w, Value x) {
      if (w.r() != 1) throw new RankError("rank of 𝕨 ≠ 1", this);
      if (x.r() != 1) throw new RankError("rank of 𝕩 ≠ 1", this);
      if (w.ia != x.ia) throw new LengthError("both sides lengths should match", this);
      StrMap map = new StrMap();
      for (int i = 0; i < w.ia; i++) {
        map.set(w.get(i), x.get(i));
      }
      return map;
    }
  }
  
  private static class Optimizer extends FnBuiltin {
    public String ln(FmtInfo f) { return "•OPT"; }
    
    public Value call(Value x) {
      if (x instanceof Primitive || x instanceof ChrArr || x instanceof BitArr || x instanceof SingleItemArr) return x;
      return Arr.create(x.values(), x.shape);
    }
  }
  private static class ClassGetter extends FnBuiltin {
    public String ln(FmtInfo f) { return "•CLASS"; }
    
    public Value call(Value x) {
      return new ChrArr(x.getClass().getCanonicalName());
    }
  }
  
  private static class Ex extends RelFn {
    public String ln(FmtInfo f) { return "•EX"; }
  
    private final Scope sc;
    public Ex(Scope sc) { this.sc = sc; }
    
    public Value call(String path, Value x) {
      return call(path, EmptyArr.SHAPE0S, x);
    }
    
    public Value call(String path, Value w, Value x) {
      return sc.sys.execFile(Sys.path(path, x.asString()), w, new Scope(sc));
    }
  }
  private static class Import extends RelFn {
    public String ln(FmtInfo f) { return "•Import"; }
    
    private final Scope sc;
    public Import(Scope sc) { this.sc = sc; }
    
    public Value call(String path, Value x) {
      Path p = Sys.path(path, x.asString());
      Value val = sc.sys.imported.get(p);
      if (val == null) {
        val = sc.sys.execFile(p, new Scope(sc));
        sc.sys.imported.put(p, val);
      }
      return val;
    }
    
    public Value call(String path, Value w, Value x) {
      return sc.sys.execFile(Sys.path(path, x.asString()), w, new Scope(sc));
    }
    
  }
  
  abstract static class RelFn extends Md1 {
    public Value derive(Value f) {
      String path = f==Nothing.inst? null : f.asString();
      return new FnBuiltin() {
        public String ln(FmtInfo f) { return RelFn.this.ln(f); }
        public Value call(Value w, Value x) {
          return RelFn.this.call(path, w, x);
        }
        public Value call(Value x) {
          return RelFn.this.call(path, x);
        }
      };
    }
    
    public Value call(String path, Value w, Value x) { return call(w, x); }
    public Value call(String path, Value x) { return call(x); }
  }
  public static boolean isRel(String name) {
    return name.equals("•import") || name.equals("•lns") || name.equals("•ex") || name.equals("•fchars") || name.equals("•flines") || name.equals("•fbytes");
  }
  
  private static class FIO extends RelFn {
    public String ln(FmtInfo f) {
      return type=='b'? "•FBytes" : type=='l'? "•FLines" : "•FChars";
    }
    private final char type;
    public FIO(char type) {
      this.type = type;
    }
  
    public Value call(String path, Value x) {
      Path p = Sys.path(path, x.asString());
      try {
        if (type=='l') {
          List<String> l = Files.readAllLines(p, StandardCharsets.UTF_8);
          Value[] v = new Value[l.size()];
          for (int i = 0; i < v.length; i++) v[i] = new ChrArr(l.get(i));
          return new HArr(v);
        }
        byte[] bs = Files.readAllBytes(p);
        // if (type=='b') return new IntArr(bs);
        if (type=='b') {
          char[] cs = new char[bs.length];
          for (int i = 0; i < bs.length; i++) cs[i] = (char) (bs[i]&0xff);
          return new ChrArr(cs);
        }
        // type=='c'
        return new ChrArr(new String(bs, StandardCharsets.UTF_8));
      } catch (IOException e) {
        throw new ValueError(ln(null)+": Couldn't read file \""+p+"\"");
      }
    }
  
    public Value call(String path, Value w, Value x) {
      Path p = Sys.path(path, w.asString());
      try {
        if (type=='l') {
          MutByteArr b = new MutByteArr();
          if (x.r()!=1) throw new RankError("•FLines: Expected 𝕩 to have rank 1", this);
          for (Value c : x) {
            if (c.r()!=1) throw new RankError("•FLines: Expected 𝕩 to have items of rank 1", this);
            b.add(c.asString().getBytes(StandardCharsets.UTF_8));
            b.s((byte) '\n');
          }
          Files.write(p, b.get());
        }
        if (type=='b') {
          byte[] xb = new byte[x.ia];
          if (x.ia==0 || x.get(0) instanceof Num) {
            int[] xi = x.asIntArr();
            for (int i = 0; i < xi.length; i++) xb[i] = (byte)xi[i];
          } else {
            String xs = x.asString();
            for (int i = 0; i < xs.length(); i++) {
              char c = xs.charAt(i);
              if (c>=256) throw new DomainError("•FBytes: Expected 𝕩 to consist of characters ≤@+255", this);
              xb[i] = (byte) c;
            }
          }
          Files.write(p, xb);
        }
        if (type=='c') {
          Files.write(p, x.asString().getBytes(StandardCharsets.UTF_8));
        }
        return w;
      } catch (IOException e) {
        throw new ValueError("Couldn't write to file \""+p+"\"");
      }
    }
  }
  
  private static class Lns extends RelFn {
    public String ln(FmtInfo f) { return "•LNS"; }
    
    public Value call(String path, Value x) {
      Path p = Sys.path(path, x.asString());
      String[] a = Main.readFile(p).split("\n");
      Value[] o = new Value[a.length];
      for (int i = 0; i < a.length; i++) {
        o[i] = Main.toAPL(a[i]);
      }
      return Arr.create(o);
    }
    
    String get(APLMap m, String key, String def) {
      Value got = m.get(key);
      if (got != null) return got.asString();
      return def;
    }
    
    public Value call(String path, Value w, Value x) {
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
          
          Value eo = m.get("e");
          if (eo != null) {
            APLMap e = (APLMap) eo;
            Value[][] kv = e.kvPair();
            for (int i = 0; i < kv[0].length; i++) {
              conn.setRequestProperty(kv[0][i].asString(), kv[1][i].asString());
            }
          }
          
          Value cache = m.get("cache");
          conn.setUseCaches(cache!=null && Main.bool(cache));
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
        Path p = Sys.path(path, w.asString());
        String s = x.asString();
        try (PrintWriter pw = new PrintWriter(p.toFile())) {
          pw.write(s);
        } catch (FileNotFoundException e) {
          throw new DomainError("File "+p+" not found: "+e.getMessage(), this);
        }
        return x;
      }
    }
  }
  
  
  private static class Shell extends FnBuiltin {
    public String ln(FmtInfo f) { return "•SH"; }
    
    public Value call(Value x) {
      return exec(x, null, null, false);
    }
    
    public Value call(Value w, Value x) {
      APLMap m = (APLMap) w;
      
      File dir = null;
      Value diro = m.get("dir");
      if (diro != null) dir = new File(diro.asString());
      
      byte[] inp = null;
      Value inpo = m.get("inp");
      if (inpo != null) {
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
      Value rawo = m.get("raw");
      if (rawo != null) raw = Main.bool(rawo);
      
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
  
  
  
  private static class Stdin extends FnBuiltin {
    public String ln(FmtInfo f) { return "•STDIN"; }
    
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
  private static class Pretty extends FnBuiltin {
    public String ln(FmtInfo f) { return "•Pretty"; }
    
    private final Scope sc;
    Pretty(Scope sc) { this.sc = sc; }
    
    public Value call(Value x) {
      return call(Num.ONE, x);
    }
  
    public Value call(Value w, Value x) {
      int wi = w.asInt();
      if (Math.abs(wi)==2) {
        String v = x.ln(sc.sys.fi);
        return wi==2? new ChrArr(v) : Format.str(v);
      } else {
        Value v = x.pretty(sc.sys.fi);
        return wi<0? v : new ChrArr(FmtInfo.fmt(v));
      }
    }
  }
  private static class Out extends FnBuiltin {
    public String ln(FmtInfo f) { return "•Out"; }
    
    private final Scope sc;
    public Out(Scope sc) { this.sc = sc; }
    
    public Value call(Value x) {
      sc.sys.println(Format.outputFmt(x));
      return x;
    }
  }
  
  private static class Hasher extends FnBuiltin {
    public String ln(FmtInfo f) { return "•HASH"; }
    
    public Value call(Value x) {
      return Num.of(x.hashCode());
    }
  }
  public static class Profiler extends Fun {
    public String ln(FmtInfo f) { return "•PFX"; }
    
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
      public String ln(FmtInfo f) { return "•_PFO"; }
      
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
    }
    public static class ProfilerMd2 extends Md2 {
      public String ln(FmtInfo f) { return "•_PFC_"; }
      
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
    private final BQN.Comp.SingleComp c;
    private int am;
    private double ms;
    private Value fn;
    
    public Pr(Comp.SingleComp c) {
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
    public String ln(FmtInfo f) { return "•BIG"; }
    
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
  
  
  public static class Type extends FnBuiltin {
    public String ln(FmtInfo f) { return "•Type"; }
    /*
     0 - array
     1 - number
     2 - char
     3 - function
     4 - 1-modifier
     5 - 2-modifier
     6 - bigint
     7 - namespace
     99 - unknown
     */
    public Value call(Value x) {
      return on(x);
    }
    public static Value on(Value x) {
      if (x instanceof      Arr) return Num.ZERO;
      if (x instanceof      Num) return Num.NUMS[1];
      if (x instanceof     Char) return Num.NUMS[2];
      if (x instanceof      Fun) return Num.NUMS[3];
      if (x instanceof      Md1) return Num.NUMS[4];
      if (x instanceof      Md2) return Num.NUMS[5];
      if (x instanceof BigValue) return Num.NUMS[6];
      if (x instanceof   APLMap) return Num.NUMS[7];
      return Num.NUMS[99];
    }
  }
  public static class Glyph extends FnBuiltin {
    public String ln(FmtInfo f) { return "•Glyph"; }
    
    public Value call(Value x) {
      if (x instanceof FnBuiltin || x instanceof Md1Builtin || x instanceof Md2Builtin) return new ChrArr(x.ln(null));
      throw new DomainError("•Glyph: Expected argument to be a built-in function or modifier");
    }
  }
  public static class Source extends FnBuiltin {
    public String ln(FmtInfo f) { return "•Source"; }
  
    public Value call(Value x) {
      return new ChrArr(BlockTok.get(x, this).source());
    }
  }
  
  public static class Decompose extends FnBuiltin {
    public String ln(FmtInfo f) { return "•Decompose"; }
  
    public Value call(Value x) {
      if (x instanceof FnBuiltin || x instanceof Md1Builtin || x instanceof Md2Builtin) return new HArr(new Value[]{Num.ZERO, x});
      if (x instanceof FunBlock || x instanceof Md1Block || x instanceof Md2Block) return new HArr(new Value[]{Num.ONE, x});
      if (x instanceof Atop) return new HArr(new Value[]{Num.NUMS[2],               ((Atop) x).g, ((Atop) x).h});
      if (x instanceof Fork) return new HArr(new Value[]{Num.NUMS[3], ((Fork) x).f, ((Fork) x).g, ((Fork) x).h});
      if (x instanceof Md1Derv) return new HArr(new Value[]{Num.NUMS[4], ((Md1Derv) x).f, ((Md1Derv) x).op});
      if (x instanceof Md2Derv) return new HArr(new Value[]{Num.NUMS[5], ((Md2Derv) x).f, ((Md2Derv) x).op, ((Md2Derv) x).g});
      if (x instanceof Md2HalfDerv) return new HArr(new Value[]{Num.NUMS[7], ((Md2HalfDerv) x).op, ((Md2HalfDerv) x).g});
      return new HArr(new Value[]{Num.MINUS_ONE, x});
    }
  }
  
  public static class DR extends FnBuiltin {
    public String ln(FmtInfo f) { return "•DR"; }
    
    /*
        0=100| - unknown
       10=100| - bit number
       11=100| - 64-bit float
       12=100| - 32-bit int
       20=100| - char
       30=100| - function
       40=100| - 1-modifier
       50=100| - 2-modifier
       60=100| - bigint
       70=100| - namespace object
       71=100| - native namespace
      
      0=⌊𝕩÷100 - primitive
      1=⌊𝕩÷100 - array
    */
    public Value call(Value x) {
      return on(x);
    }
    public static Value on(Value x) {
      if (x instanceof Arr) {
        if (x instanceof      HArr) return Num.NUMS[100];
        if (x instanceof    BitArr) return Num.NUMS[110];
        if (x instanceof DoubleArr) return Num.NUMS[111];
        if (x instanceof    IntArr) return Num.NUMS[112];
        if (x instanceof    ChrArr) return Num.NUMS[120];
        return Num.NUMS[100];
      } else {
        if (x instanceof       Num) return Num.NUMS[11];
        if (x instanceof      Char) return Num.NUMS[20];
        if (x instanceof       Fun) return Num.NUMS[30];
        if (x instanceof       Md1) return Num.NUMS[40];
        if (x instanceof       Md2) return Num.NUMS[50];
        if (x instanceof  BigValue) return Num.NUMS[60];
        if (x instanceof Namespace) return Num.NUMS[70];
        if (x instanceof    APLMap) return Num.NUMS[71];
        return Num.NUMS[0];
      }
    }
    /*
      1 number 𝕨 - convert to array of that type (supported - 0, 10, 11, 12)
      2 numbers in 𝕨 - reinterpret from type 0⊑𝕨 to 1⊑𝕨
     */
    public Value call(Value w, Value x) {
      int[] wi = w.asIntVec();
      if (wi.length == 2) { 
        int f = wi[0];
        int t = wi[1];
        if ((f==10 || f==11 || f==60)
         && (t==10 || t==11 || t==60)
         && (f==11 ^ t==11)) { // convert float to/from bits/long
          if (t==11) {
            if (f==10) return DepthBuiltin.on(new Fun() {
              public String ln(FmtInfo f) { return "•DR"; }
              public Value call(Value x) {
                return new Num(Double.longBitsToDouble(((BigValue) UTackBuiltin.on(BigValue.TWO, x, DR.this)).longValue()));
              }
            }, 1, x, this);
            if (f==60) return DepthBuiltin.on(new Fun() {
              public String ln(FmtInfo f) { return "•DR"; }
              public Value call(Value x) {
                return new Num(Double.longBitsToDouble(((BigValue) x).longValue()));
              }
            }, 0, x, this);
          } else {
            if (t==10) return DepthBuiltin.on(new Fun() {
              public String ln(FmtInfo f) { return "•DR"; }
              public Value call(Value x) {
                return new BitArr(new long[]{Long.reverse(Double.doubleToRawLongBits(x.asDouble()))}, new int[]{64});
              }
            }, 0, x, this);
            if (t==60) return DepthBuiltin.on(new Fun() {
              public String ln(FmtInfo f) { return "•DR"; }
              public Value call(Value x) {
                return new BigValue(Double.doubleToRawLongBits(x.asDouble()));
              }
            }, 0, x, this);
          }
        }
      } else if (wi.length == 1) {
        switch (wi[0]) {
          case 0: return new HArr(x.values(), x.shape);
          case 10: return new BitArr(x.asBitLongs(), x.shape);
          case 11: return new DoubleArr(x.asDoubleArr(), x.shape);
          case 12: return new IntArr(x.asIntArr(), x.shape);
        }
        if (wi[0]<100) throw new DomainError("•DR: 𝕨 should be ≥100");
      } else throw new DomainError("•DR: 𝕨 must have 1 or 2 items (had "+wi.length+")", this);
      throw new NYIError(w+"•DR not implemented", this);
    }
    public Value callInvX(Value w, Value x) {
      return call(ReverseBuiltin.on(w), x);
    }
  }
}