package BQN.errors;

import BQN.*;
import BQN.tokenizer.Token;
import BQN.types.*;

import java.util.*;

public abstract class BQNError extends RuntimeException {
  public Tokenable blame;
  public Tokenable obj;
  public ArrayList<Frame> trace = new ArrayList<>();
  
  
  protected BQNError(String msg) {
    super(msg);
  }
  protected BQNError(String msg, Tokenable blame) {
    super(msg);
    if (blame instanceof Callable) this.blame = blame;
    else this.obj = blame;
  }
  protected BQNError(String msg, Tokenable blame, Tokenable obj) {
    super(msg);
    this.blame = blame;
    this.obj = obj;
  }
  
  
  public void print(Sys s) {
    String type = getClass().getSimpleName();
    String msg = getMessage();
    if (msg != null && msg.length() != 0) s.colorprint(type + ": " + msg, 246);
    else s.colorprint(type, 246);
    ArrayList<Mg> l = new ArrayList<>();
    if (blame!=null) Mg.add(l, blame, '^');
    if (obj!=null) Mg.add(l, obj, '¯');
    println(l, s);
  }
  
  public static void println(List<Mg> gs, Sys s) {
    if (gs.size() == 2 && gs.get(0).eqSrc(gs.get(1))) printgr(gs, s);
    else for (Mg g : gs) {
      ArrayList<Mg> l = new ArrayList<>();
      l.add(g);
      printgr(l, s);
    }
  }
  
  private static void printgr(List<Mg> gs, Sys s) {
    if (gs.size() == 0) return;
    
    String raw = gs.get(0).raw;
    int lns = gs.get(0).lns;
    
    int lne = raw.indexOf("\n", lns);
    if (lne == -1) lne = raw.length();
    
    String ln = gs.get(0).raw.substring(lns, lne);
    s.println(ln);
    char[] str = new char[ln.length()];
    int i = 0;
    int o = 0;
    while (i < str.length) {
      char c = ' ';
      for (Mg g : gs) if (i>=g.spos && i<g.epos) c = g.c;
      str[o] = c;
      i+= Character.isHighSurrogate(ln.charAt(i))? 2 : 1;
      o++;
    }
    s.println(new String(str, 0, o));
  }
  
  public void stack(Sys sys) {
    for (int i = 0; i < trace.size(); i++) {
      sys.println((trace.size()-i) + ":");
      Frame f = trace.get(i);
      BQNError.println(f.msgs, sys);
      boolean bc = true;
      for (Mg c : f.msgs) {
        //noinspection StringEquality
        if (c.t.raw!=Token.COMP.raw) { bc = false; break; }
      }
      if (f.msgs.size()>0 && bc) sys.println(f.comp.fmt(f.idx, 20));
    }
  }
  
  public static class Mg {
    final Token t;
    final char c;
    final String raw;
    int lns;
    int spos, epos; // in bounds of the line
    
    public Mg(Token t, char c, String raw, int lns, int spos, int epos) {
      this.t = t;
      this.c = c;
      this.raw = raw;
      this.lns = lns;
      this.spos = spos;
      this.epos = epos;
    }
    
    public static void add(ArrayList<Mg> l, Tokenable to, char c) {
      if (to == null) return;
      Token t = to.getToken();
      if (t == null) return;
      
      String raw = t.raw;
      
      int lns = raw.lastIndexOf("\n", t.spos) + 1; // not found handles itself
      
      
      int spos = t.spos - lns;
      int epos = t.epos - lns;
      
      l.add(new Mg(t, c, raw, lns, spos, epos));
    }
    
    boolean eqSrc(Mg g) {
      // noinspection StringEquality \\ we want that
      return raw==g.raw && lns==g.lns;
    }
  }
  
  
  public static class Frame {
    public final Scope sc;
    public ArrayList<Mg> msgs;
    public final Comp comp;
    public final int idx;
    
    public Frame(Scope sc, ArrayList<Mg> msgs, Comp comp, int idx) {
      
      this.sc = sc;
      this.msgs = msgs;
      this.comp = comp;
      this.idx = idx;
    }
  }
}