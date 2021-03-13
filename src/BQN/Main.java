package BQN;

import BQN.errors.*;
import BQN.tokenizer.*;
import BQN.tokenizer.types.BasicLines;
import BQN.tools.Format;
import BQN.types.*;
import BQN.types.arrs.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Scanner;

@SuppressWarnings("WeakerAccess") // for use as a library
public class Main {
  public static final String CODEPAGE = "\0\0\0\0\0\0\0\0\0\t\n\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0\0 !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~÷×↑↓⌈⌊≠∊⍺⍴⍵⍳∍⋾⎕⍞⌸⌺⍇⍁⍂⌻⌼⍃⍄⍈⍌⍍⍐⍓⍔⍗⍯⍰⍠⌹⊆⊇⍶⍸⍹⍘⍚⍛⍜⍊≤≥⍮ϼ⍷⍉⌽⊖⊙⌾○∘⍟⊗¨⍨⍡⍥⍩⍣⌾⍤⊂⊃∩∪⊥⊤∆∇⍒⍋⍫⍱⍲∨∧⍬⊣⊢⌷⍕⍎←→⍅⍆⍏⍖⌿⍀⍪≡≢⍦⍧⍭‽⍑∞…√ᑈᐵ¯⍝⋄⌶⍙";
  public static boolean debug = false;
  public static boolean vind = false; // vector indexing
  public static boolean quotestrings = false; // whether to quote strings & chars in non-oneline mode
  public static boolean colorful = true;
  static final ChrArr uAlphabet = toAPL("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
  static final ChrArr lAlphabet = toAPL("abcdefghijklmnopqrstuvwxyz");
  public static final ChrArr digits = toAPL("0123456789");
  static int printlvl = 0;
  static final long startingMillis = System.currentTimeMillis();
  static final long startingNanos = System.nanoTime();
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
                  println("Usage: BQN [options]");
                  println("Options:");
                  println("-f file: execute the contents of the file with all further arguments as •args");
                  println("-e code: execute the argument as BQN");
                  println("-p code: execute the argument as BQN and print its result");
                  println("-i     : execute STDIN as BQN");
                  println("-r     : start the REPL after everything else");
                  println("-s     : start the REPL without indenting input after everything else");
                  println("-d     : enable debug mode");
                  println("-q     : quote strings in output");
                  println("-b     : disable boxing");
                  println("-c     : disable colorful printing");
                  println("-q     : enable quoting strings");
                  println("-D file: run the file as SBCS");
                  println("-E a b : encode the file A in the SBCS, save as B");
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
                    for (int j = 0; j < gargs.length; j++) gargs[j] = Main.toAPL(args[i+j]);
                    sys.execFile(Sys.path(sys.cd, name), new HArr(gargs), sys.gsc);
                    i = args.length;
                    break;
                  case 'e':
                    String code = args[++i];
                    exec(code, sys.gsc, sys.defArgs);
                    break;
                  case 'p':
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
                  case 'q':
                    quotestrings = true;
                    break;
                  case 'b':
                    sys.ln = true;
                    break;
                  case 'c':
                    colorful = false;
                    break;
                  case 'E': {
                    String origS = readFile(args[++i]);
                    byte[] res = new byte[origS.length()];
                    for (int j = 0; j < origS.length(); j++) {
                      char chr = origS.charAt(j);
                      int index = CODEPAGE.indexOf(chr);
                      if (index == -1) throw new DomainError("error encoding character "+chr+" (dec "+(+chr)+")");
                      res[j] = (byte) index;
                    }
                    String conv = args[++i];
                    try (FileOutputStream stream = new FileOutputStream(conv)) {
                      stream.write(res);
                    } catch (IOException e) {
                      e.printStackTrace();
                      throw new DomainError("couldn't write file");
                    }
                    break;
                  }
                  case 'D':
                    try {
                      byte[] bytes = Files.readAllBytes(new File(args[++i]).toPath());
                      StringBuilder res = new StringBuilder();
                      for (byte b : bytes) {
                        res.append(CODEPAGE.charAt(b & 0xff));
                      }
                      exec(res.toString(), sys.gsc, sys.defArgs);
                    } catch (IOException e) {
                      e.printStackTrace();
                      throw new DomainError("couldn't read file");
                    }
                    break;
                  default:
                    throw new DomainError("Unknown command-line argument -"+c);
                }
              }
            }
          } else if (p.charAt(0)=='•') {
            int si = p.indexOf('←');
            if (si == -1) throw new DomainError("argument `"+p+"` didn't contain a `←`");
            String qk = p.substring(0, si);
            String qv = p.substring(si+1);
            sys.gsc.set(qk, exec(qv, sys.gsc, sys.defArgs));
          } else {
            throw new DomainError("Unknown command-line argument "+p);
          }
        }
      } catch (APLError e) {
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
      System.exit(0);
    }
    
    public String input() {
      return Main.console.nextLine();
    }
    
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
  public static String formatAPL(int[] ia) {
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
      if (path.startsWith("'") && path.endsWith("'")  ||  path.startsWith("\"") && path.endsWith("\"")) {
        msg+= " (argument shouldn't be surrounded in quotes)";
      }
      DomainError ne = new DomainError(msg);
      ne.initCause(e);
      throw ne;
    }
  }
  
  public static Value exec(String s, Scope sc, Value[] args) {
    return Comp.comp(tokenize(s, args), sc).exec(sc);
  }
  public static Comp.SingleComp comp(String s, Scope sc, Value[] args) {
    return Comp.comp(tokenize(s, args), sc);
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
  
  
  public static ChrArr toAPL(String s) { // TODO remove
    return new ChrArr(s);
  }
  
  
  public static String repeat(String s, int l) {
    StringBuilder r = new StringBuilder();
    for (int i = 0; i < l; i++) r.append(s);
    return r.toString();
  }
}