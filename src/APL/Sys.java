package APL;

import APL.errors.*;
import APL.tokenizer.Tokenizer;
import APL.types.*;
import APL.types.arrs.HArr;

import java.util.ArrayList;

public abstract class Sys {
  public Scope gsc; // global/top-level scope
  public Scope csc; // current scope in which things happen
  public boolean oneline;
  
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
        Main.execFile(parts[1], csc);
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
        if (Main.lastError != null) {
          Main.lastError.printStackTrace();
        } else println("no stack to view");
        break;
      case "STACK":
        if (Main.lastError instanceof APLError) {
          ArrayList<APLError.Frame> trace = ((APLError) Main.lastError).trace;
          for (int i = 0; i < trace.size(); i++) {
            println((trace.size()-i) + ":");
            APLError.println(trace.get(i).msgs, this);
          }
        } else println("no stack to view");
        break;
      case "CS":
        if (rest.length()==0) csc = gsc;
        else {
          boolean num = true;
          for (char c : rest.toCharArray()) if (c>'9' || c<'0') { num = false; break; }
          if (num) {
            if (Main.lastError instanceof APLError) {
              ArrayList<APLError.Frame> trace = ((APLError) Main.lastError).trace;
              csc = trace.get(trace.size()-Integer.parseInt(rest)).sc;
            } else throw new DomainError("no error to )cs to");
          } else {
            Value v = Main.exec(rest, csc);
            if (v instanceof Callable) {
              Scope nsc = ((Callable) v).sc;
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
  
  public void line(String cr) {
    Main.faulty = null;
    if (cr.startsWith(")")) {
      ucmd(cr.substring(1));
    } else {
      Comp comp = Main.comp(cr);
      byte lins = comp.bc.length==0? 0 : comp.bc[comp.bc.length-1];
      Value r = comp.exec(csc);
      if (r!=null && lins!=Comp.SETN && lins!=Comp.SETU && lins!=Comp.SETM) {
        println(r);
      }
    }
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
