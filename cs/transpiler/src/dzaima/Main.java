package dzaima;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;


public class Main {
  public static Path SRC = Paths.get("../../src");
  public static Path DST = Paths.get("../BQNApp");
  public static void main(String[] args) {
    System.out.println("copying files");
    rec(Paths.get("BQN"));
    
    try {
      for (int itr = 0; itr < 3; itr++) {
        System.out.print("iteration "+itr+":");
        ProcessBuilder pb = new ProcessBuilder("./mcsBuild");
        // pb.directory(DST.getParent().toFile());
        // pb.redirectErrorStream(true);
        Process p = pb.start();
        // String[] errs = new String(p.getInputStream().readAllBytes()).split("\n");
        String[] errs = new String(p.getErrorStream().readAllBytes()).split("\n");
        System.out.println(" "+errs.length+" stderr lines");
        
        HashMap<String, Vec<Prb>> prs = new HashMap<>();
        for (int i = errs.length-1; i >= 0; i--) {
          String c = errs[i];
          String n = i+1>=errs.length? "" : errs[i+1];
          int cs = c.indexOf(" CS");
          if (cs==-1) continue;
          Prb.Tp tp = switch (c.substring(cs+3,cs+7)) { default -> null;
            case "0114" -> Prb.Tp.over;
            case "0029" -> Prb.Tp.dmd;
            case "1503" -> Prb.Tp.dmd2;
            case "0122" -> Prb.Tp.inacc;
            case "0621" -> Prb.Tp.addpb;
            case "0038" -> Prb.Tp.inner;
            case "0108" -> Prb.Tp.over;
            case "0506" -> Prb.Tp.virt;
          };
          if (tp != null) {
            if (tp.m==1 && n.contains(": (Location")) c = n;
            String[] ps = c.split("[(,)]");
            String floc = ps[0];
            
            if (tp == Prb.Tp.inacc && !c.contains(": (Location")) {
              String path = c.split("[`']")[1];
              P po = ppath(path);
              String fnd = ".* "+po.t[po.t.length-1]+"(, \\w+)*(;|\\s*=).*";
              floc = DST.toAbsolutePath().relativize(po.s.toAbsolutePath()).toString();
              try {
                List<String> lns = Files.readAllLines(po.s);
                Vec<Integer> matching = new Vec<>();
                for (int j = 0; j < lns.size(); j++) {
                  String ln = lns.get(j);
                  if (ln.matches(fnd) && ln.matches(".*(\\w|\\w>) \\w+(, \\w+)*(;|\\s*=).*")) {
                    int bi = j;
                    int bl = 0;
                    while (bl>=0) {
                      for (char ch : lns.get(bi).toCharArray()) {
                        if(ch=='{') bl--;
                        if(ch=='}') bl++;
                      }
                      bi--;
                    } bi++;
                    if (lns.get(bi).contains("class"))
                      matching.add(j);
                  }
                }
                if (matching.size()==0) System.out.println("NOT FOUND "+c);
                else {
                  Vec<Prb> prbs = prs.computeIfAbsent(floc, k -> new Vec<>());
                  for (int ln : matching) {
                    prbs.add(new Prb(Prb.Tp.addpb, ln, 0, c));
                  }
                }
              } catch (IOException e) { throw new RuntimeException(e); }
            } else {
              Vec<Prb> prbs = prs.computeIfAbsent(floc, k -> new Vec<>());
              prbs.add(new Prb(tp, Integer.parseInt(ps[1])-1, Integer.parseInt(ps[2]), c));
            }
          }
        }
        for (Map.Entry<String, Vec<Prb>> entry : prs.entrySet()) {
          String k = entry.getKey();
          Vec<Prb> v = entry.getValue();
          Path path = DST.resolve(Paths.get(k));
          String s = Files.readString(path, StandardCharsets.UTF_8);
          String[] lns = s.split("\n");
    
          for (Prb c : v) {
            String ln = lns[c.ln];
            switch (c.type) { default -> System.out.println("unhandled "+c.type);
              case over -> {
                if (ln.contains("virtual")) break;
                int i = c.pos; while (i>=0 && ln.charAt(i)!='{') i--; i++;
                ln = ln.substring(0, i) + " override " + ln.substring(i);
              }
              case virt -> {
                if (ln.contains("virtual")) break;
                int i = c.pos; while (i>=0 && ln.charAt(i)!='{') i--; i++;
                ln = ln.substring(0, i) + " virtual " + ln.substring(i);
              }
              case dmd -> {
                if (!c.msg.contains("<")) {
                  System.out.println("skipping d1 "+c.msg);
                } else {
                  String tp = c.msg.split("' to `")[1];
                  if (!tp.contains("<")) System.out.println("BAD "+c.msg);
                  tp = tp.substring(tp.indexOf("<"), tp.length()-1);
                  ln = ln.replace("<>", tp);
                }
              }
              case dmd2 -> {
                if (!c.msg.contains("<")) {
                  if (c.msg.contains("cannot convert `object'")) {
                    String tp = c.msg.split("type `|'$")[1];
                    ln = ln.substring(0,c.pos-1)+"("+tp+")"+ln.substring(c.pos-1);
                  } else System.out.println("skipping d2 "+c.msg);
                } else {
                  // String tp = c.msg.split(" to type `")[1];
                  // System.out.println(c.msg);
                  String tp = c.msg.split("convert `|' expr")[1]; // c.msg.split(" to type `")[1];
                  if (!tp.contains("<")) System.out.println("BAD "+c.msg);
                  else {
                    tp = tp.substring(tp.indexOf("<"));
                    ln = ln.replace("<>", tp);
                  }
                }
              }
              case inacc -> {
                // System.out.println(ln);
                ln = addpb(ln, c.pos);
              }
              case addpb -> {
                String ppl = ln;
                ln = addpb(ln, 0);
              }
              case inner -> {
                int ps = c.pos-1;
                ln = ln.substring(0, ps)+"outer."+ln.substring(ps);
              }
            }
            lns[c.ln] = ln;
          }
          
          StringBuilder r = new StringBuilder();
          for (String ln : lns) r.append(ln).append("\n");
          if (path.getFileName().equals("Program.cs")) throw new Error();
          Files.writeString(path, r.toString());
          
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
  static String addpb(String ln, int guide) {
    if (ln.contains("public")) return ln;
    int i = guide; while (i>=0 && ln.charAt(i)!='{') i--; i++;
    while (ln.charAt(i)==' ')i++;
    if (ln.contains("private")) ln = ln.replace("private", "public");
    else if (ln.contains("protected")) ln = ln.replace("protected", "public");
    else ln = ln.substring(0, i) + "public " + ln.substring(i);
    return ln;
  }
  
  static class Prb {
    int ln, pos;
    Tp type;
    enum Tp {
      dmd(0),
      dmd2(0),
      over(0),
      inacc(1),
      addpb(0),
      inner(0),
      virt(1),
      ;
      
      int m;
      Tp(int m) {
        this.m = m;
      }
    }
    String msg;
    public Prb(Tp type, int ln, int pos, String msg) {
      this.ln = ln;
      this.pos = pos;
      this.type = type;
      this.msg = msg;
    }
  }
  
  static class P {
    Path s;
    String[] t;
    public P(Path s, String[] t) {
      this.s = s; this.t = t;
    }
  }
  static P ppath(String path) {
    String[] ps = path.split("\\.");
    Path c = DST;
    int i = 0;
    while (i < ps.length) {
      c = c.resolve(ps[i]);
      if (!Files.isDirectory(c)) break;
      i++;
    }
    return new P(c.getParent().resolve(c.getFileName().toString()+".cs"), Arrays.copyOfRange(ps, i, ps.length));
  }
  
  private static void rec(Path p) {
    Path sf = SRC.resolve(p);
    if (Files.isDirectory(sf)) {
      try {
        Files.list(sf).forEach(c -> rec(p.resolve(c.getFileName())));
      } catch (IOException e) { e.printStackTrace(); System.exit(1); }
    } else {
      String name = p.getFileName().toString();
      if (!name.endsWith(".java")) return;
      if (name.equals("JBC.java") || name.equals("JBQNComp.java") || name.equals("StrMap.java")) return;
      proc(sf, DST.resolve(p.getParent().resolve(name.substring(0,name.length()-4)+"cs")));
    }
  }
  
  static boolean al(char c) {
    return c>='a' && c<='z'
        || c>='A' && c<='Z'
        || c=='_';
  }
  static boolean num(char c) {
    return c>='0' && c<='9';
  }
  static String s;
  static char get(int i) {
    if (i>=s.length()) return 0;
    return s.charAt(i);
  }
  
  static String ops = "+-/*%&|~!><=^";
  // static String[] map = """
  //   PUSH|0
  //   VARO|1
  //   VARM|2
  //   ARRO|3
  //   ARRM|4
  //   FN1C|5
  //   FN2C|6
  //   OP1D|7
  //   OP2D|8
  //   TR2D|9
  //   TR3D|10
  //   SETN|11
  //   SETU|12
  //   SETM|13
  //   POPS|14
  //   DFND|15
  //   FN1O|16
  //   FN2O|17
  //   CHKV|18
  //   TR3O|19
  //   OP2H|20
  //   LOCO|21
  //   LOCM|22
  //   VFYM|23
  //   SETH|24
  //   RETN|25
  //   SPEC|30
  //   EVAL|0
  //   STDIN|1
  //   STDOUT|2
  //   """.split("\n");
  
  private static void proc(Path src, Path dst) {
    try {
      s = Files.readString(src, StandardCharsets.UTF_8);
      if (s.contains("extends Builtin")) {
        for (String c : new String[]{"NumMV", "ChrMV"}) s = s.replace(c, "Fun."+c);
      }
      // for (String p : map) {
      //   String[] ps = p.split("\\|");
      //   s = s.replace("case "+ps[0], "case "+ps[1]);
      // }
      s = s.replace("->", "=>");
      s = s.replace(".length()", ".Length");
      s = s.replace(".length", ".Length");
      s = s.replace(".size()", ".Count");
      s = s.replace("getMessage()", "Message");
      s = s.replace(".getCanonicalName()", ".Name");
      s = s.replace(".getSimpleName()", ".Name");
      s = s.replace(".getName()", ".Name");
      s = s.replace(".getBytes(StandardCharsets.UTF_8)", ".getBytes()");
      s = s.replace("getClass()", "GetType()");
      s = s.replace("System.out", "CSOut");
      s = s.replace("System.arraycopy", "Array.Copy");
      s = s.replace("System.nanoTime()", "(Stopwatch.GetTimestamp()*1000000000L/Stopwatch.Frequency)");
      s = s.replace("System.currentTimeMillis()", "(Stopwatch.GetTimestamp()*1000L/Stopwatch.Frequency)");
      s = s.replace("System.console()", "null");
      s = s.replace("System.in", "\"in\"");
      s = s.replace("System.exit", "Environment.Exit");
      s = s.replace("System.getenv()", "(new HashMap<string,string>())");
      s = s.replace("System.gc()", "");
      s = s.replace("Thread.sleep", "IR.sleep");
      s = s.replace("new String(", "CS.SR.cr(");
      
      s = s.replace("Math.max", "Math.Max");
      s = s.replace("Math.min", "Math.Min");
      s = s.replace("Math.pow", "Math.Pow");
      s = s.replace("Math.exp", "Math.Exp");
      s = s.replace("Math.log", "Math.Log");
      s = s.replace("Math.sqrt", "Math.Sqrt");
      s = s.replace("Math.floorMod", "IR.fmod");
      s = s.replace("Math.floor", "Math.Floor");
      s = s.replace("Math.ceil", "Math.Ceiling");
      s = s.replace("Math.addExact", "IR.add");
      s = s.replace("Math.subtractExact", "IR.sub");
      s = s.replace("Math.multiplyExact", "IR.mul");
      s = s.replace("new BigInteger", "BR.cr");
      s = s.replace("BigInteger.", "BR.");
      s = s.replace("Integer.", "IR.");
      s = s.replace("Long.", "LR.");
      s = s.replace("Double.", "DWR.");
      s = s.replace("String.", "SR.");
      s = s.replace("Character.", "CR.");
      Vec<Tk> p1 = new Vec<>();
      int i = 0;
      while (i < s.length()) {
        int pi = i;
        char cc = s.charAt(pi);
        char nc = get(pi+1);
        if (al(cc)) { i++;
          while (al(cc=get(i)) || cc>='0'&&cc<='9') i++;
          p1.add(new NameTk(s, pi, i));
        } else if (cc==' ' || cc=='\n' || cc=='\t') {
          i++;
          p1.add(new WSTk(s, pi, i));
        } else if (cc=='/' && nc=='/') {
          while ((cc=get(i))!='\n' && cc!=0) i++;
          p1.add(new ExactTk(s, pi, i));
        } else if (cc=='/' && nc=='*') { i++;
          while (get(i)!='/' && get(i+1)!='*' && i<=s.length()) i++; i+= 3;
          p1.add(new ExactTk(s, pi, i));
        } else if (ops.indexOf(cc)!=-1) { i++;
          if (s.startsWith(">>>", pi)) i+= 2;
          p1.add(new OpTk(s, pi, i));
        } else if (cc==';') { i++;
          p1.add(new SemiTk(s, pi, i));
        } else if (cc=='.') { i++;
          p1.add(new DotTk(s, pi, i));
        } else if (cc=='?') { i++;
          p1.add(new QMTk(s, pi, i));
        } else if (cc==',') { i++;
          p1.add(new DotTk(s, pi, i));
        } else if (cc==':') { i++;
          p1.add(new ColonTk(s, pi, i));
        } else if (cc=='@') { i++;
          p1.add(new AtTk(s, pi, i));
        } else if (cc=='(' || cc=='{' || cc=='[') { i++;
          p1.add(new POTk(s, pi, i));
        } else if (cc==')' || cc=='}' || cc==']') { i++;
          p1.add(new PCTk(s, pi, i));
        } else if (num(cc)) { i++;
          while (num(cc=get(i)) || cc=='b' || cc=='x' || cc=='_') i++;
          p1.add(new ExactTk(s, pi, i));
        } else if (cc=='\'') { i++;
          while (true) { cc=get(i++); if (cc==0||cc=='\'') break; if (cc=='\\') i++; }
          p1.add(new ExactTk(s, pi, i));
        } else if (cc=='\"') { i++;
          while (true) { cc=get(i++); if (cc==0||cc=='\"') break; if (cc=='\\') i++; }
          p1.add(new ExactTk(s, pi, i));
        } else throw new Error("Unknown char "+cc+" ("+((int)cc)+")");
        if (i==pi) throw new Error("AAA");
      }
      i = -1;
      Vec<Tk> p2 = new Vec<>();
      Vec<Tk> using = new Vec<>();
      for (int j = 0; j < p1.size(); j++) {
        Tk tk = p1.get(j);
        if (tk instanceof NameTk) {
          switch (tk.src()) {
            case "instanceof" -> p2.add(new NameTk("is"));
            case "ActualTypeof" -> p2.add(new NameTk("typeof"));
            case "typeof" -> p2.add(new NameTk("typeof_"));
            case "sizeof" -> p2.add(new NameTk("sizeof_"));
            case "final" -> {
              boolean cl = p1.get(skip(p1, j)).src().equals("class");
              p2.add(new NameTk(cl? "sealed" : "readonly"));
            }
            case "Comparable" -> p2.add(new NameTk("IComparable"));
            case "toString" -> p2.add(new NameTk("ToString"));
            case "compareTo" -> p2.add(new NameTk("CompareTo"));
            case "hashCode" -> p2.add(new NameTk("GetHashCode"));
            case "Integer" -> p2.add(new NameTk("Int32"));
            case "Long" -> p2.add(new NameTk("Int64"));
            case "Character" -> p2.add(new NameTk("char"));
            case "Throwable" -> p2.add(new NameTk("Exception"));
            case "byte" -> p2.add(new NameTk("sbyte"));
            case "is" -> p2.add(new ExactTk("is_"));
            case "in" -> p2.add(new ExactTk("in_"));
            case "base" -> p2.add(new ExactTk("base_"));
            case "ref" -> p2.add(new ExactTk("ref_"));
            case "out" -> p2.add(new ExactTk("out_"));
            case "Char" -> p2.add(new ExactTk("Chr"));
            case "Vec" -> p2.add(new ExactTk("CS.Vec"));
            case "LinkedList" -> p2.add(new ExactTk("CS.LinkedList"));
            case "List" -> p2.add(new ExactTk("CS.List"));
            case "File" -> p2.add(new ExactTk("CS.File"));
            case "Path" -> p2.add(new ExactTk("CS.File"));
            case "abs" -> p2.add(new ExactTk("Abs"));
            case "sort" -> p2.add(new ExactTk("Sort"));
            case "add" -> p2.add(new ExactTk("Add"));
            case "insert" -> p2.add(new ExactTk("Insert"));
            case "addLast" -> p2.add(new ExactTk("AddLast"));
            case "addFirst" -> p2.add(new ExactTk("AddFirst"));
            case "nextDouble" -> p2.add(new ExactTk("NextDouble"));
            case "append" -> p2.add(new ExactTk("Append"));
            case "containsKey" -> p2.add(new ExactTk("ContainsKey"));
            case "clear" -> p2.add(new ExactTk("Clear"));
            case "equals" -> p2.add(new ExactTk("Equals"));
            case "contains" -> p2.add(new ExactTk("Contains"));
            // case "indexOf" -> p2.add(new ExactTk("IndexOf"));
            // case "clone" -> p2.add(new ExactTk("Clone"));
            case "toUpperCase" -> p2.add(new ExactTk("ToUpper"));
            case "toLowerCase" -> p2.add(new ExactTk("ToLower"));
            case "lastIndexOf" -> p2.add(new ExactTk("LastIndexOf"));
            case "startsWith" -> p2.add(new ExactTk("StartsWith"));
            case "endsWith" -> p2.add(new ExactTk("EndsWith"));
            case "replace" -> p2.add(new ExactTk("Replace"));
            case "split" -> p2.add(new ExactTk("Split"));
            case "super" -> p2.add(new ExactTk("base"));
            // case "assert" -> j = find(p1, j, ";"); // bad; should be after parenthesis are sane
            // case "Main" -> {
            //   if (p1.get(j+1).src().equals(".") && p1.get(j+2).src().equals("bool")) {
            //     p2.add(p1.get(j)); p2.add(p1.get(j+1)); p2.add(new NameTk("toBool")); j+= 2;
            //   } else p2.add(tk);
            // }
            case "bool" -> p2.add(new NameTk("boolean"));
            case "boolean" -> p2.add(new NameTk("bool"));
            case "String" -> p2.add(new NameTk("string"));
            // case "default" -> {
            //   if (p1.get(skip(p1, j+1)).src().equals(":")) p2.add(tk);
            //   else j = skip(p1, j+1)-1;
            // }
            case "import" -> {
              int pj = j;
              j = skip(p1, find(p1, j, ";"))-1;
              int lj = j;
              while (!p1.get(lj).src().equals(".")) lj--;
              String loc = ArrTk.join(p1.subList(skip(p1, pj), lj));
              if (loc.startsWith("java.")) {
                switch (loc.substring(5)) { default -> System.out.println("couldn't match " + loc);
                  case "util.Iterator" -> loc = "System.Collections.Generic";
                  case "util", "io", "net", "nio.charset", "nio.file" -> loc = null;
                  case "math" -> loc = "System.Numerics";
                }
              }
              if (loc != null) {
                using.add(new ExactTk("using ")); using.add(new ExactTk(loc)); using.add(new SemiTk()); using.add(WSTk.ln);
              }
            }
            default -> p2.add(tk);
          }
        } else if (tk.src().equals(">>>")) p2.add(new OpTk(">>"));
        else p2.add(tk);
        
      }
      Vec<Tk> p3 = new Vec<>();
      int e = group(0, p3, p2);
      if (e!=-1) throw new Error("unmatched parenthesis");
      Tk g0 = p3.get(0);
      if (!(g0 instanceof NameTk) || !g0.src().equals("package")) throw new Error("expected first token to be package");
      int sp = 0;
      while (!(p3.get(sp) instanceof SemiTk)) sp++;
      Vec<Tk> p4 = new Vec<>();
      p4.add(new ExactTk("""
        using CS;
        using System;
        using System.IO;
        using System.Collections.Generic;
        using System.Text;
        using System.Diagnostics;
        """));
      p4.addAll(using);
      p4.add(new NameTk("namespace"));
      p4.addAll(p2.subList(1, sp));
      p4.add(WSTk.sp);
      
      Vec<Tk> ct = new Vec<>(p3.subList(sp+1, p3.size()));
      ct.add(WSTk.ln);
  
  
      Vec<Tk> tail = new Vec<>();
      Vec<Tk> ct2 = rec1(ct, "NAMESPACE", tail);
      ct2.addAll(tail);
      ct2.add(WSTk.ln);
      p4.add(new BlkTk(ct2));
      StringBuilder r = new StringBuilder();
      for (Tk c : p4) r.append(c.src());
      String rr = r.toString();
      rr = rr.replaceAll("\\)\\s*\\{( *//[^\n]*)?\\s*base\\((.*)\\);", ") : base($2) {$1");
      rr = rr.replaceAll("\\)\\s*\\{( *//[^\n]*)?\\s*this\\((.*)\\);", ") : this($2) {$1");
      Files.createDirectories(dst.getParent());
      Files.writeString(dst, rr);
    } catch (Throwable e) {
      System.out.println("ERROR "+src);
      e.printStackTrace();
    }
  }
  
  private static Vec<Tk> upd1(List<Tk> g) { // labels & where
    Vec<Tk> r = new Vec<>();
    for (Tk tk : g) {
      r.add(tk);
      if (tk instanceof BlkTk) {
        int ci = r.size()-2;
        int lbi = -1;
        while (ci>=0 && !(r.get(ci) instanceof BlkTk || r.get(ci) instanceof SemiTk)) {
          if (r.get(ci) instanceof ColonTk) { // labels
            if (lbi >= 0) lbi = 10000000;
            else lbi = ci;
          }
          if (r.get(ci).src().equals("class")) { // where
            ci = skip(r, skip(r, ci));
            String ws = "";
            if (r.get(ci).src().equals("<")) {
              int ki = ci;
              while (!r.get(ki).src().equals(">")) {
                if (r.get(ki) instanceof ColonTk) {
                  int ei = skip(r, ki);
                
                  String k = r.get(rskip(r, ki)).src();
                  String v = r.get(ei).src();
                  r.subList(ki, ei+1).clear();
                  if (ws.length()!=0) ws+= ", ";
                  ws+= k+" : "+v;
                } else ki++;
              }
            }
            if (ws.length() != 0) r.add(r.size()-1, new ExactTk("where "+ws+" "));
            lbi = -1;
            break;
          }
          ci--;
        }
        lbl: if (lbi!=-1 && lbi!=10000000) { // labels
          ci = lbi;
          int aai = ci-2;
          while (aai>=0 && r.get(aai) instanceof WSTk) aai--;
          if (r.get(Math.max(0,aai)).src().equals("case")) break lbl;
          String name = r.get(ci-1).src();
          int bi = ci;
          while (!(r.get(bi) instanceof BlkTk)) bi++;
          ((BlkTk) r.get(bi)).tks.add(0, new ExactTk(name+"_NXT:;"));
          r.add(new ExactTk(name+"_END:;"));
        }
      }
    }
    return r;
  }
  private static Vec<Tk> rec1(List<Tk> g, String className, Vec<Tk> tail) {
    Vec<Tk> res = new Vec<>();
    String myName = className;
    for (int i = 0; i < g.size(); i++) {
      Tk c = g.get(i);
      if (c instanceof ArrTk) {
        boolean nb = !className.equals(myName);
        Vec<Tk> ntl = nb? new Vec<>() : tail;
        Vec<Tk> rr = rec1(((ArrTk) c).tks, myName, ntl);
          
        if (nb) rr.addAll(ntl);
        res.add(((ArrTk) c).mod(rr));
      } else if (c.src().equals("for")) {
        int li = skip(g, i);
        ParTk tk = ((ParTk) g.get(li));
        boolean mod = false;
  
        for (Tk k : tk.tks) {
          if (k instanceof QMTk) break;
          else if (k instanceof ColonTk) { mod = true; break; }
        }
        if (mod) {
          res.add(new NameTk("foreach"));
          res.addAll(g.subList(i+1, li));
          Vec<Tk> np = new Vec<>(tk.tks);
          for (int j = 0; j < np.size(); j++) {
            if (np.get(j) instanceof ColonTk) { np.set(j, new NameTk("in")); break; }
          }
          res.add(new ParTk(np));
          i = li;
        } else res.add(c);
      } else if (c.src().equals("extends")) {
        res.add(new ColonTk()); i++;
        while (!(g.get(i) instanceof BlkTk || g.get(i).src().equals(">"))) {
          if (g.get(i).src().equals("implements")) {
            while (res.get(res.size()-1) instanceof WSTk) res.remove(res.size()-1);
            res.add(new CommaTk());
          }
          else res.add(g.get(i));
          i++;
        }
        i--;
      } else if (c.src().equals("implements")) {
        res.add(new ColonTk());
      } else if (c.src().equals("assert")) {
        i = find(g, i, ";");
      } else if (c.src().equals("static")) {
        boolean f=false;
        int ti = i;
        while (ti<g.size() && !(g.get(ti) instanceof BlkTk || g.get(ti) instanceof SemiTk)) { if (g.get(ti).src().equals("class")) { f=true; break; } ti++; }
        if(!f) {
          if (g.get(skip(g, i)) instanceof BlkTk) {
            res.add(c); res.add(new ExactTk(" "+className+"()"));
          } else
            res.add(c);
        }
      } else if (c instanceof AtTk) {
        i = skip(g, skip(g, i));
        if (g.get(i) instanceof ParTk) i++;
        i--;
      } else if (c.src().equals("new")) {
        int pi = i;
        while (!(g.get(pi) instanceof ArrTk)) pi++;
        if (g.get(pi) instanceof ParTk) {
          int bi = skip(g, pi);
          if (g.get(bi) instanceof BlkTk) {
            String name = "ILC" + ctr++;
            res.add(new ExactTk("new "+name));
            ParTk e = (ParTk) g.get(pi);
            res.add(e);
            Vec<Tk> args = new Vec<>(e.tks);
            e.tks = args;
            String sup = ArrTk.join(rec1e(g.subList(i + 1, pi), name, tail));
            
            boolean stt = sup.contains("Pervasion") || sup.contains("MV");
            stt&= !sup.contains("/*NS*/");
            stt|= sup.contains("/*IS*/");
            String outerType = className;
            if (stt) outerType = "string";
            // System.out.println(stt);
            
            args.add(0, new ExactTk((stt? "\".\"" : "this") + (args.size()==0?"":",")));
            tail.add(new ExactTk("class "+name));
            tail.add(new ColonTk());
            tail.add(new ExactTk(sup));
            tail.add(WSTk.sp);
            int aa = 0;
            for (Tk ca : args) if(ca instanceof CommaTk) aa++;
            // for (Tk a : args) if (!(a instanceof WSTk) && !(a instanceof ExactTk)) {
            //   aa++;
            //   break;
            // }
            BlkTk brk = (BlkTk) g.get(bi);
            brk.tks = new Vec<>(brk.tks);
            String argt = outerType+" outer";
            String argb = "";
            for (int j = 0; j < aa; j++) {
              argt+= ", "+"object A"+j;
              argb+= (j==0?"":", ")+  "A"+j;
            }
            String vard = "";
            String vara = "";
            if (args.size()>1 && args.get(1).src().startsWith("/*AA ")) {
              String cm = args.get(1).src();
              cm = cm.substring(5, cm.length() - 2);
              // args.remove(1);
              args.subList(1,args.size()).clear();
              // if (aa>0 || !stt) args.add(new ExactTk(", "));
              String[] cps = cm.split(",");
              for (int j = 0; j < cps.length; j++) {
                String cp = cps[j];
                String[] tn = cp.split(" ");
                argt+= ", "+cp;
                vard+= cp+";\n";
                vara+= "this."+tn[1]+" = "+tn[1]+";";
                args.add(new ExactTk(tn[1]+(j==cps.length-1?"":",")));
              }
            }
            brk.tks.add(new ExactTk("public "+name+"("+argt+"):base("+argb+"){this.outer=outer;"+vara+"}\n"+outerType+" outer;\n"+vard));
            tail.addAll(rec1e(List.of(brk), name, tail));
            tail.add(WSTk.ln);
            i = bi;
          } else res.add(c);
        } else res.add(c);
      } else if (c.src().equals("Iterator") && g.size()>1) {
        String cn = null;
        if (g.get(i+1).src().equals("<")) {
          int ei = find(g, i, ">");
          int ai = skip(g, ei);
          if (g.get(ai).src().equals("iterator") && g.get(ai+1) instanceof ParTk) {
            int fi = skip(g, ai+1);
            if (g.get(fi) instanceof BlkTk && !className.equals("SingleItemArr") && !className.equals("EmptyArr")) {
              cn = ArrTk.join(g.subList(i+2, ei));
              res.addAll(rec1e(g.subList(i, fi+1), className, tail));
              i = fi;
            }
          }
        }
        if (cn != null) {
          res.add(WSTk.ln);
          res.add(new ExactTk("""
            public IEnumerator<T> GetEnumerator() {
              Iterator<T> i = iterator();
              while (i.hasNext()) yield return i.next();
            }
            System.Collections.IEnumerator System.Collections.IEnumerable.GetEnumerator() {
              return GetEnumerator();
            }
          """.replace("T", cn)));
        } else res.add(c);
      } else if (c.src().equals("break") || c.src().equals("continue")) {
        int np = skip(g, i);
        if (!(g.get(np) instanceof SemiTk)) {
          if (c.src().equals("continue")) {
            res.add(new ExactTk("goto "));
            res.add(new ExactTk(g.get(np).src()+"_NXT"));
            i = np;
          } else {
            res.add(new ExactTk("goto "));
            res.add(new ExactTk(g.get(np).src()+"_END"));
            i = np;
          }
        } else res.add(c);
      } else if (c.src().equals("charAt")) {
        i = skip(g, i);
        res.subList(rskip(res, res.size()-1)+1, res.size()).clear();
        res.add(new SqrTk(((ParTk) g.get(i)).tks));
      }
      
      else res.add(c);
      ellipsis: if (res.size()>=4) {
        for (int j = 0; j < 3; j++) {
          if (!res.get(res.size()-j-1).src().equals(".")) break ellipsis;
        }
        res.remove(res.size()-1); res.remove(res.size()-1); res.remove(res.size()-1);
        res.add(new SqrTk(new Vec<>()));
        int ci = res.size()-1;
        while (!(res.get(ci) instanceof NameTk)) ci--;
        res.add(ci, new ExactTk("params "));
        
      }
      if (c.src().equals("class")) {
        myName = g.get(skip(g, i)).src();
      }
    }
    return upd1(res);
  }
  
  private static Vec<Tk> rec1e(List<Tk> g, String className, Vec<Tk> tail) {
    Vec<Tk> res = new Vec<>();
    for (Tk c : g) {
      Vec<Tk> cl = new Vec<>();
      cl.add(c);
      res.addAll(rec1(cl, className, tail));
    }
    return res;
  }
  
  static int ctr = 0;
  
  static int skip(List<Tk> tks, int i) {
    i++;
    while (true) {
      if (i>=tks.size()) return i-1; 
      if (!(tks.get(i) instanceof WSTk)) break;
      i++;
    }
    return i;
  }
  static int rskip(List<Tk> tks, int i) {
    i--;
    while (true) {
      if (i<0) return 0; 
      if (!(tks.get(i) instanceof WSTk)) break;
      i--;
    }
    return i;
  }
  
  static int find(List<Tk> tks, int i, String s) {
    while (!tks.get(i).src().equals(s)) i++;
    return i;
  }
  
  private static int group(int i, Vec<Tk> res, Vec<Tk> src) {
    while (i < src.size()) {
      Tk tk = src.get(i);
      if (tk instanceof POTk) {
        Vec<Tk> tks = new Vec<>();
        i = group(i+1, tks, src);
        switch (tk.src()) { default:assert false;
          case "{": res.add(new BlkTk(tks)); if (!src.get(i-1).src().equals("}")) throw new Error("unmatched parenthesis"); break; 
          case "[": res.add(new SqrTk(tks)); if (!src.get(i-1).src().equals("]")) throw new Error("unmatched parenthesis"); break; 
          case "(": res.add(new ParTk(tks)); if (!src.get(i-1).src().equals(")")) throw new Error("unmatched parenthesis"); break; 
        }
      } else if (tk instanceof PCTk) {
        return i+1;
      } else {
        res.add(tk);
        i++;
      }
    }
    return -1;
  }
}
