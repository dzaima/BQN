package BQN;

import BQN.errors.*;
import BQN.tools.SysVals;
import BQN.types.*;

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
  public Scope(Scope p, String IWillHandleVariables) {
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
  public static final Value REPL_MARK = new Nothing();
  public void markREPL() {
    if (vars.length<1 || vars[0]==null) vars[0] = REPL_MARK;
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
        case "•args": case "•path": case "•name": case "•state": throw new ImplementationError(name+": should've been handled at compile");
        default:
          Value v = SysVals.getDyn(name, this);
          if (v==null) throw new DomainError("Undefined system value "+name);
          return v;
      }
    }
    Scope c = this;
    while (true) {
      Integer pos = c.varMap().get(name);
      if (pos!=null && c.vars[pos]!=null) return c.vars[pos]; // TODO remove c.vars[pos]!=null once DYN_ usage is finished
      c = c.parent;
      if (c == null) return null;
    }
  }
  public Value getC(String name) {
    Value got = get(name);
    if (got == null) throw new SyntaxError("Unknown variable \"" + name + "\"");
    return got;
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
  public Value getDel(int depth, int n) {
    Value[] v = owner(depth).vars;
    Value r = v[n];
    v[n] = null;
    return r;
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
}