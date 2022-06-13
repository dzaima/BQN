package BQN;

import BQN.errors.AssertionError;
import BQN.errors.*;
import BQN.tokenizer.Tk2;
import BQN.tokenizer.types.BasicLines;
import BQN.tools.Format;
import BQN.types.*;
import BQN.types.arrs.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Scanner;

@SuppressWarnings("WeakerAccess") // for use as a library
public class Main {
  public static boolean debug = false;
  public static boolean vind = false; // vector indexing
  public static boolean quotestrings = true; // whether to quote strings & chars in non-oneline mode
  public static boolean colorful = true;
  public static final boolean SAFE = false;
  static int printlvl = 0;
  public static final long startingMillis = System.currentTimeMillis();
  public static final long startingNanos = System.nanoTime();
  public static Scanner console;
  
  
  public static void main(String[] args) {
    colorful = System.console() != null && System.getenv().get("TERM") != null;
    console = new Scanner(System.in);
    CliSys sys = new CliSys();
    boolean REPL = false;
    boolean silentREPL = false;
    if (args.length > 0) {
      try {
        for (int i = 0; i < args.length; i++) {
          String p = args[i];
          if (p.length() >= 2 && p.charAt(0) == '-') {
            if (p.charAt(1) == '-') {
              switch (p) {
                case "--help":
                  println("Usage: BQN [options] [file.bqn arguments]");
                  println("Options:");
                  println("-f file: execute the contents of the file with all further arguments as •args");
                  println("-e code: execute the argument as BQN");
                  println("-p code: execute the argument as BQN and print its result pretty-printed");
                  println("-o code: execute the argument as BQN and print its raw result");
                  println("-i     : execute STDIN as BQN");
                  println("-r     : start the REPL after everything else");
                  println("-s     : start the REPL without indenting input after everything else");
                  println("-d     : enable debug mode");
                  println("-b     : disable pretty-printing");
                  println("-c     : disable colorful printing");
                  println("If given no arguments, an implicit -r will be added");
                  System.exit(0);
                  break;
                default:
                  throw new DomainError("Unknown command-line argument " + p);
              }
            } else {
              for (char c : p.substring(1).toCharArray()) {
                switch (c) {
                  case 'f':
                    String name = args[++i]; i++;
                    Value[] gargs = new Value[args.length-i];
                    for (int j = 0; j < gargs.length; j++) gargs[j] = new ChrArr(args[i+j]);
                    sys.execFile(Sys.path(sys.cd, name), new HArr(gargs), sys.gsc);
                    i = args.length;
                    break;
                  case 'e':
                    String code = args[++i];
                    exec(code, sys.gsc, sys.defArgs);
                    break;
                  case 'p':
                    code = args[++i];
                    println(Format.outputFmt(exec(code, sys.gsc, sys.defArgs).pretty(sys.fi)));
                    break;
                  case 'o':
                    code = args[++i];
                    println(Format.outputFmt(exec(code, sys.gsc, sys.defArgs)));
                    break;
                  case 'i':
                    StringBuilder s = new StringBuilder();
                    while (console.hasNext()) {
                      s.append(console.nextLine()).append('\n');
                    }
                    exec(s.toString(), sys.gsc, sys.defArgs);
                    break;
                  case 'r':
                    REPL = true;
                    break;
                  case 's':
                    REPL = true;
                    silentREPL^= true;
                    break;
                  case 'd':
                    debug = true;
                    break;
                  case 'b':
                    sys.ln = true;
                    break;
                  case 'c':
                    colorful = false;
                    break;
                  default:
                    throw new DomainError("Unknown command-line argument -"+c);
                }
              }
            }
          } else {
            String name = p; i++;
            Value[] gargs = new Value[args.length-i];
            for (int j = 0; j < gargs.length; j++) gargs[j] = new ChrArr(args[i+j]);
            sys.execFile(Sys.path(sys.cd, name), new HArr(gargs), sys.gsc);
            i = args.length;
            break;
          }
        }
      } catch (AssertionError e) {
        e.print(sys);
        if (!REPL) System.exit(1);
      } catch (BQNError e) {
        e.print(sys);
        System.out.println("Stack:");
        e.stack(sys);
        throw e;
      } catch (Throwable e) {
        e.printStackTrace();
        sys.colorprint(e + ": " + e.getMessage(), 246);
        throw e;
      }
    }
    if (args.length==0 || REPL) {
      sys.gsc.markREPL();
      while (true) {
        if (debug) printlvl = 0;
        if (!silentREPL) print("   ");
        if (!console.hasNext()) break;
        
        String cr = console.nextLine();
        sys.lineCatch(cr);
      }
    }
  }
  
  
  static class CliSys extends Sys {
    public void off(int code) {
      System.exit(code);
    }
    
    public String input() { return Main.console.nextLine(); }
    public boolean hasInput() { return Main.console.hasNextLine(); }
    
    public void println(String s) {
      System.out.println(s);
    }
    
    public void colorprint(String s, int col) {
      if (Main.colorful) println("\u001b[38;5;" + col + "m" + s + "\u001b[0m");
      else println(s);
    }
  }
  
  
  public static void print(String s) {
    System.out.print(s);
  }
  
  public static void println(String s) {
    System.out.println(s);
  }
  public static String fArr(int[] ia) {
    if (ia.length == 0) return "⟨⟩";
    StringBuilder r = new StringBuilder(Num.formatInt(ia[0]));
    for (int i = 1; i < ia.length; i++) {
      r.append("‿");
      r.append(Num.formatInt(ia[i]));
    }
    return r.toString();
  }
  public static String readFile(String path) {
    return readFile(Paths.get(path));
  }
  public static String readFile(Path path) {
    try {
      byte[] encoded = Files.readAllBytes(path);
      return new String(encoded, StandardCharsets.UTF_8);
    } catch (IOException e) {
      String msg = "File " + path + " not found";
      // if (path.startsWith("'") && path.endsWith("'")  ||  path.startsWith("\"") && path.endsWith("\"")) {
      //   msg+= " (argument shouldn't be surrounded in quotes)";
      // }
      DomainError ne = new DomainError(msg);
      ne.initCause(e);
      throw ne;
    }
  }
  
  public static Value exec(String s, Scope sc, Value[] args) {
    return Comp.comp(tokenize(s, args), sc, false).exec(sc);
  }
  public static Comp.SingleComp comp(String s, Scope sc, Value[] args) {
    return Comp.comp(tokenize(s, args), sc, false);
  }
  // public static boolean newTk = true;
  public static BasicLines tokenize(String s, Value[] args) {
    // if (!newTk) return Tokenizer.tokenize(s, args);
    return new Tk2(s, args).tkBlock();
  }
  
  public static void printdbg(Object... args) {
    if (!debug) return;
    if (args.length > 0) print(args[0] == null? "null" : args[0].toString());
    for (int i = 1; i < args.length; i++) {
      print(" ");
      print(args[i] == null? "null" : args[i].toString());
    }
    print("\n");
  }
  
  
  public static boolean isBool(Value x) {
    if (!(x instanceof Num)) return false;
    Num n = (Num) x;
    return n.num==0 || n.num==1;
  }
  public static boolean bool(double d) {
    if (d == 1) return true;
    if (d == 0) return false;
    throw new DomainError("Expected boolean, got "+d);
  }
  public static boolean bool(Value v) {
    if (v instanceof Num) {
      double num = ((Num) v).num;
      if (num == 1) return true;
      if (num == 0) return false;
    }
    throw new DomainError("Expected boolean, got "+v);
  }
  
  
  public static String repeat(String s, int l) {
    StringBuilder r = new StringBuilder();
    for (int i = 0; i < l; i++) r.append(s);
    return r.toString();
  }
  
  public static void unsafeTest(Callable v) { if (SAFE) throw new DomainError("Cannot use "+v+" in safe mode"); }
  public static void unsafeTest(String   v) { if (SAFE) throw new DomainError("Cannot use "+v+" in safe mode"); }
  
}