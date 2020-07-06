package APL;

import APL.errors.*;
import APL.tokenizer.Tokenizer;
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
        else off(Main.exec(rest, csc).asInt());
        break;
      case "EX":
        String full = cr.substring(cr.indexOf(" ")+1);
        execFile(full);
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
      case "TOKENIZE"    : Main.println(Tokenizer.tokenize(rest).toTree("")); break;
      case "TOKENIZEREPR": Main.println(Tokenizer.tokenize(rest).toRepr()); break;
      case "CLASS"       : var r = Main.exec(rest, csc); Main.println(r == null? "nothing" : r.getClass().getCanonicalName()); break;
      case "UOPT"        : var e = (Arr)csc.get(rest); csc.set(rest, new HArr(e.values(), e.shape)); break;
      case "ATYPE"       : Main.println(Main.exec(rest, csc).humanType(false)); break;
      case "JSTACK":
        if (lastError != null) {
          ByteArrayOutputStream os = new ByteArrayOutputStream();
          lastError.printStackTrace(new PrintStream(os));
          println(os.toString());
        } else println("no stack to view");
        break;
      case "STACK": {
        if (lastError == null) { println("no stack to view"); break; }
        ArrayList<APLError.Frame> trace = lastError.trace;
        for (int i = 0; i < trace.size(); i++) {
          println((trace.size()-i) + ":");
          APLError.println(trace.get(i).msgs, this);
        }
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
            Value v = Main.exec(rest, csc);
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
        println(Main.comp(rest).fmt());
        break;
      case "BCE":
        println(Main.comp(Main.exec(rest, csc).asString()).fmt());
        break;
      case "TYPE":
        println(String.valueOf(Comp.typeof(Tokenizer.tokenize(rest).tokens.get(0))));
        break;
      default:
        throw new SyntaxError("Undefined user command");
    }
  }
  
  private static String removeHashBang(String source) {
    if (source.charAt(0)=='#' && source.charAt(1)=='!') {
      source = source.substring(source.indexOf('\n')+1);
    }
    return source;
  }
  public Value execFile(String path, Value[] args) {
    int sl = path.indexOf("/")+1;
    Value[] rargs = Arrays.copyOf(args, args.length+2);
    rargs[args.length  ] = Main.toAPL(path.substring(sl));
    rargs[args.length+1] = Main.toAPL(path.substring(0, sl));
    Scope sc = new Scope(gsc, rargs);
    return Main.exec(removeHashBang(Main.readFile(path)), sc);
  }
  public Value execFile(String path) {
    return execFile(path, EmptyArr.NOVALUES);
  }
  
  public void line(String s) {
    Main.faulty = null;
    if (s.startsWith(")")) {
      ucmd(s.substring(1));
    } else {
      Comp comp = Main.comp(s);
      byte lins = comp.bc.length==0? 0 : comp.bc[comp.bc.length-1];
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
