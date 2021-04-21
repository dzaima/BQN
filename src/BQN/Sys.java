package BQN;

import BQN.errors.*;
import BQN.tokenizer.*;
import BQN.tokenizer.types.*;
import BQN.tools.*;
import BQN.types.*;
import BQN.types.arrs.*;
import BQN.types.callable.blocks.*;
import BQN.types.callable.builtins.fns.EvalBuiltin;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public abstract class Sys {
  public Scope gsc; // global/top-level scope
  public Scope csc; // current scope in which things happen
  public boolean ln;
  public BQNError lastError = null;
  public Value[] defArgs;
  public String cd;
  public FmtInfo fi = new FmtInfo(14, 10, 10);
  
  public Sys() {
    gsc = csc = new Scope(this);
    Path p = Paths.get(".").toAbsolutePath().normalize();
    cd = p.toString();
    defArgs = new Value[]{new ChrArr(p.toString()), EmptyArr.SHAPE0S, EmptyArr.SHAPE0SV};
  }
  
  public void ucmd(String cr) {
    String[] parts = cr.split(" ");
    String t = parts[0].toUpperCase();
    String rest = parts.length==1? "" : cr.substring(t.length()+1);
    switch (t) {
      case "OFF": case "EXIT": case "STOP":
        if (rest.length()==0) off(0);
        else off(Main.exec(rest, csc, defArgs).asInt());
        break;
      case "EX":
        String full = cr.substring(cr.indexOf(" ")+1);
        execFile(path(cd, full), gsc);
        break;
      case "DEBUG":
        Main.debug = !Main.debug; // keeping these as static booleans to improve performance (plus getting Sys where needed might be extremely annoying)
        break;
      case "QUOTE":
        Main.quotestrings = !Main.quotestrings;
        break;
      case "LN":
        if (rest.length()==0) ln = !ln;
        else println(Main.exec(rest, csc, defArgs).ln(fi));
        break;
      case "TOKENIZE"    : BasicLines tk = Main.tokenize(rest,defArgs); Comp.typeof(tk); Comp.flags(tk); println(tk.toTree("")); break;
      case "TOKENIZEREPR": println(Main.tokenize(rest,defArgs).toRepr()); break;
      case "CLASS"       : Value r = Main.exec(rest, csc, defArgs); println(r == null? "nothing" : r.getClass().getCanonicalName()); break;
      case "UOPT"        : Arr e = (Arr)csc.get(rest); csc.set(rest, new HArr(e.values(), e.shape)); break;
      case "ATYPE"       : println(Main.exec(rest, csc, defArgs).humanType(false)); break;
      case "JSTACK":
        if (lastError != null) {
          ByteArrayOutputStream os = new ByteArrayOutputStream();
          lastError.printStackTrace(new PrintStream(os));
          println(os.toString());
        } else println("no stack to view");
        break;
      case "STACK": {
        if (lastError != null) lastError.stack(this);
        else println("no stack to view");
        break;
      }
      case "SCI": {
        println("Variables:");
        String[] names = csc.varNames;
        for (int i = 0; i < names.length; i++) {
          String s = names[i];
          if (s!=null) {
            Value val = csc.vars[i];
            if (val == null) println("  "+s+": unset");
            else {
              String vs = val.ln(FmtInfo.def);
              println("  "+s+": "+(vs.length()<100 && !vs.contains("\n")? vs : val.humanType(false)));
            }
          } else println("  ("+i+") unused");
        }
        if (csc.hasMap()) println("hashmap initialized");
        int d=0;Scope c = csc;
        while (c.parent!=null) { d++; c=c.parent; }
        println(d==0? "At global scope" : "At depth "+d);
        break;
      }
      case "CS":
        if (rest.length()==0) csc = gsc;
        else {
          boolean num = true;
          for (char c : rest.toCharArray()) if (c>'9' || c<'0') { num = false; break; }
          if (num) {
            if (lastError == null) { println("no stack to )cs to"); break; }
            ArrayList<BQNError.Frame> trace = lastError.trace;
            csc = trace.get(trace.size()-Integer.parseInt(rest)).sc;
          } else {
            Value v = Main.exec(rest, csc, defArgs);
            if (v instanceof Callable) {
              Scope nsc = null;
              if (v instanceof FunBlock) nsc = ((FunBlock) v).sc;
              if (v instanceof Md1Block) nsc = ((Md1Block) v).sc;
              if (v instanceof Md2Block) nsc = ((Md2Block) v).sc;
              if (v instanceof EvalBuiltin) nsc = ((EvalBuiltin) v).sc;
              if (nsc == null) throw new DomainError("argument to )cs didn't contain scope information");
              else csc = nsc;
              break;
            }
            else throw new DomainError("argument to )cs wasn't scoped");
          }
        }
        break;
      case "NF":
        if (rest.length() == 0) {
          println("digits: "+fi.pp+"; range: "+Num.fmt(-fi.pns)+"…"+fi.pne + (ln? "; )ln on" : ""));
        } else {
          fi = fi.with(Main.exec(rest, csc, defArgs).asIntVec());
        }
        break;
      case "BC":
        println(Main.comp(rest, csc, defArgs).fmt());
        break;
      case "BCE":
        println(Main.comp(Main.exec(rest, csc, defArgs).asString(), csc, defArgs).fmt());
        break;
      case "TOKTYPE":
        println(String.valueOf(Comp.typeof(Main.tokenize(rest,defArgs).tokens.get(0))));
        break;
      case "NTOK":
        LineTok nt = new LineTok("…", 0, 1, new Tk2(rest, defArgs).tkLine());
        System.out.println(nt.toTree(""));
        System.out.println(nt.toRepr());
        break;
      case "NEV":
          System.out.println(Comp.comp(new Tk2(rest, defArgs).tkBlock(), csc, false).exec(csc));
        break;
      default:
        throw new SyntaxError("Undefined user command");
    }
  }
  
  public static Path path(String path, String s) {
    if (path == null) {
      Path p = Paths.get(s);
      if (!p.isAbsolute()) throw new DomainError("Expected code outside files to only use absolute paths");
      return p.normalize();
    }
    return Paths.get(path).resolve(s).toAbsolutePath().normalize();
  }
  
  public String[] split(Path p) {
    p = p.toAbsolutePath().normalize();
    return new String[]{p.getParent().toString()+"/", p.getFileName().toString()};
  }
  public Value execFile(Path path, Value args, Scope sc) {
    String[] spl = split(path);
    Value[] rargs = new Value[]{
      new ChrArr(spl[0]), // path
      new ChrArr(spl[1]), // name
      args,               // args
    };
    String code = Main.readFile(path);
    return Comp.compN(Main.tokenize(code, rargs), sc).exec(sc);
  }
  public Value execFile(Path path, Scope sc) {
    return execFile(path, EmptyArr.SHAPE0S, sc);
  }
  public HashMap<Path, Value> imported = new HashMap<>();
  
  public void line(String s) {
    if (s.startsWith(")")) {
      ucmd(s.substring(1));
    } else {
      Comp.SingleComp comp = Comp.comp(Main.tokenize(s, defArgs), csc, true);
      if (comp.c.bc.length==0) return;
      Value r = comp.exec(csc);
      if (r!=null && !shy(comp.c)) println(r);
    }
  }
  
  public static boolean shy(Comp c) {
    if (c.bc.length==0) return true;
    int ci = 0;
    while (true) {
      int ni = c.next(ci);
      if (ni == c.bc.length) break;
      ci = ni;
    }
    int lins = c.bc[ci];
    return lins==Comp.SETN || lins==Comp.SETU || lins==Comp.SETM || (ci>2 && lins==Comp.FN1C && c.bc[ci-2]==Comp.SYSV && (c.bc[ci-1]==SysVals.getID("•out") || c.bc[ci-1]==SysVals.getID("•show")));
  }
  
  public void lineCatch(String s) {
    try {
      line(s);
    } catch (Throwable t) { report(t); }
  }
  public void report(Throwable t) {
    BQNError err = lastError = t instanceof BQNError? (BQNError) t : new ImplementationError(t);
    err.print(this);
  }
  
  
  public abstract void println(String s);
  public /*open*/ void colorprint(String s, int col) {
    println(s);
  }
  public abstract void off(int code);
  
  
  public /*open*/ void println(Value v) {
    println(ln? v.ln(fi) : FmtInfo.fmt(v.pretty(fi)));
  }
  
  public abstract String input();
  public abstract boolean hasInput();
  
}