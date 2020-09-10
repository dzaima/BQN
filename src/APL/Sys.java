package APL;

import APL.errors.*;
import APL.tokenizer.Tokenizer;
import APL.tokenizer.types.BasicLines;
import APL.tools.JComp;
import APL.types.*;
import APL.types.arrs.*;
import APL.types.functions.builtins.fns2.EvalBuiltin;
import APL.types.functions.userDefined.*;

import java.io.*;
import java.util.*;

public abstract class Sys {
  public Scope gsc; // global/top-level scope
  public Scope csc; // current scope in which things happen
  public boolean oneline;
  public APLError lastError = null;
  public Value[] defArgs = new Value[]{EmptyArr.SHAPE0S, EmptyArr.SHAPE0S};
  
  public Sys() {
    gsc = csc = new Scope(this);
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
        execFile(full, gsc);
        break;
      case "DEBUG":
        Main.debug = !Main.debug; // keeping these as static booleans to improve performance (plus getting Sys where needed might be extremely annoying)
        break;
      case "QUOTE":
        Main.quotestrings = !Main.quotestrings;
        break;
      case "ONELINE":
        oneline = !oneline;
        break;
      case "TOKENIZE"    : BasicLines tk = Tokenizer.tokenize(rest,defArgs); Comp.typeof(tk); Comp.flags(tk); println(tk.toTree("")); break;
      case "TOKENIZEREPR": println(Tokenizer.tokenize(rest,defArgs).toRepr()); break;
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
              String vs = val.toString();
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
            ArrayList<APLError.Frame> trace = lastError.trace;
            csc = trace.get(trace.size()-Integer.parseInt(rest)).sc;
          } else {
            Value v = Main.exec(rest, csc, defArgs);
            if (v instanceof Callable) {
              Scope nsc = null;
              if (v instanceof Dfn ) nsc = ((Dfn ) v).sc;
              if (v instanceof Dmop) nsc = ((Dmop) v).sc;
              if (v instanceof Ddop) nsc = ((Ddop) v).sc;
              if (v instanceof EvalBuiltin) nsc = ((EvalBuiltin) v).sc;
              if (nsc == null) throw new DomainError("argument to )cs didn't contain scope information");
              else csc = nsc;
              break;
            }
            else throw new DomainError("argument to )cs wasn't scoped");
          }
        }
        break;
      case "BC":
        println(Main.comp(rest, csc, defArgs).fmt());
        break;
      case "BCE":
        println(Main.comp(Main.exec(rest, csc, defArgs).asString(), csc, defArgs).fmt());
        break;
      case "TOKTYPE":
        println(String.valueOf(Comp.typeof(Tokenizer.tokenize(rest,defArgs).tokens.get(0))));
        break;
      case "JEVAL":
        println(new JComp(Main.comp(rest, csc, defArgs)).r.get(csc, 0));
        break;
      default:
        throw new SyntaxError("Undefined user command");
    }
  }
  
  public Value execFile(String path, Value[] args, Scope sc) {
    int sl = path.lastIndexOf("/")+1;
    Value[] rargs = Arrays.copyOf(args, args.length+2);
    rargs[args.length  ] = Main.toAPL(path.substring(sl));
    rargs[args.length+1] = Main.toAPL(path.substring(0, sl));
    return Main.exec(Main.readFile(path), sc, rargs);
  }
  public Value execFile(String path, Scope sc) {
    return execFile(path, EmptyArr.NOVALUES, sc);
  }
  
  public void line(String s) {
    if (s.startsWith(")")) {
      ucmd(s.substring(1));
    } else {
      Comp comp = Main.comp(s, csc, defArgs);
      if (comp.bc.length==0) return;
      int ci = 0;
      while (true) {
        int ni = comp.next(ci);
        if (ni == comp.bc.length) break;
        ci = ni;
      }
      byte lins = comp.bc[ci];
      Value r = comp.exec(csc);
      if (r!=null && lins!=Comp.SETN && lins!=Comp.SETU && lins!=Comp.SETM) {
        println(r);
      }
    }
  }
  
  public void lineCatch(String s) {
    try {
      line(s);
    } catch (Throwable t) {
      setLastError(t).print(this);
    }
  }
  
  public APLError setLastError(Throwable t) {
    return lastError = t instanceof APLError? (APLError) t : new ImplementationError(t);
  }
  
  
  public abstract void println(String s);
  public /*open*/ void colorprint(String s, int col) {
    println(s);
  }
  public abstract void off(int code);
  
  
  public void println(Value v) {
    println(oneline? v.oneliner() : v.toString());
  }
  
  public abstract String input();
}