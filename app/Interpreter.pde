

abstract static class Interpreter {
  abstract String[] repl(String ln);
  abstract Value exec(String s);
}








static class AppMap extends SimpleMap {
  String ln(FmtInfo f) { return "app"; }
  Interpreter it;
  
  AppMap(Interpreter it) {
    this.it = it;
  }
  
  void setv(String k, Value v) {
    String s = k.toLowerCase();
    switch (s) {
      case "update":
        layoutUpdate = (Fun) v;
        layoutUpdate.call(new ChrArr(kb.data.getString("fullName")), new ChrArr(kb.layout));
        kb.redraw();
      return;
      case "action": actionCalled = (Fun) v; return;
      default: throw new DomainError("Setting non-existing key "+s+" for app");
    }
  }
  Value getv(String k) {
    String s = k.toLowerCase();
    if (s.matches("t\\d+")) {
      int i = Integer.parseInt(s.substring(1));
      if (i < topbar.tabs.size()) return topbar.tabs.get(i);
    }
    switch (s) {
      case "layout": return new ChrArr(kb.data.getString("fullName"));
      case "set": return new Fun() {
        public String ln(FmtInfo f) { return "app.set"; }
        public Value call(Value w, Value x) {
          int[] is = w.asIntVec();
          int xp  = is[0];
          int yp  = is[1]; Key key = kb.keys[yp][xp];
          int dir = is[2];
          key.actions[dir] = new Action(a.parseJSONObject(x.asString()), kb, key);
          return Num.ONE;
        }
      };
      case "graph": return new Fun() {
        public String ln(FmtInfo f) { return "app.graph"; }
        public Value call(Value w) {
          Grapher g = new Grapher(it, w.asString());
          topbar.toNew(g);
          return g;
        }
      };
      case "cpy": return new Fun() {
        public String ln(FmtInfo f) { return "app.cpy"; }
        public Value call(Value w) {
          if (w.r() == 1) {
            w = Arr.create(w.values(), w.shape);
            if (w instanceof ChrArr) {
              a.copy(w.asString());
              return Num.ONE;
            }
          }
          a.copy(w.toString());
          return Num.ONE;
        }
      };
      case "redraw": redrawAll(); return Num.ONE;
      case "ts": {
        Value[] vs = new Value[topbar.tabs.size()];
        for (int i = 0; i < vs.length; i++) vs[i] = topbar.tabs.get(i);
        return new HArr(vs);
      }
      case "t": return all.ctab;
      default: return null;
    }
  }
}

DzaimaBQN dbqn = new DzaimaBQN();

static Sys glSys = new Sys() {
  void off(int i) {
    System.exit(i);
  }
  String input() {
    return "";
  }
  boolean hasInput() { return false; }
  void println(String s) {
    System.out.println(s);
    mainREPL.hist.appendLns(s);
    mainREPL.hist.end();
  }
};
static Scope dzaimaSC = glSys.gsc;
static Fun layoutUpdate, actionCalled;
static {
  Main.colorful = false;
  Comp.compileStart = -1;
}

static class DzaimaBQN extends Interpreter {
  //final Sys sys = new Sys() {
  //  void off(int code) {
  //    System.exit(code);
  //  }
  //  String input() {
  //    return "";
  //  }
  //  void println(String s) {
  //    System.out.println(s);
  //    mainREPL.hist.appendLns(s);
  //    mainREPL.hist.end();
  //  }
  //};
  final Sys sys = glSys;
  
  DzaimaBQN() {
    sys.gsc.set("app", new AppMap(this));
  }
  
  Value exec(String code) {
    return Main.exec(code, sys.csc, sys.defArgs);
  }
  
  String[] repl(String ln) {
    sys.lineCatch(ln);
    return new String[0];
  }
  
  String[] get(String code) {
    try {
      Obj v = Main.exec(code, dzaimaSC, sys.defArgs);
      if (v == null) return new String[0];
      return v.toString().split("\n");
    } catch (BQNError e) {
      e.print(sys);
      return new String[0];
    } catch (Throwable e) {
      //ArrayList<String> lns = new ArrayList();
      //lns.add(e + ": " + e.getMessage());
      //if (Main.faulty != null && Main.faulty.getToken() != null) {
      //  String s = repeat(" ", Main.faulty.getToken().spos);
      //  lns.add(Main.faulty.getToken().raw);
      //  lns.add(s + "^");
      //}
      //e.printStackTrace();
      //return lns.toArray(new String[0]);
      BQNError ae = e instanceof BQNError? (BQNError)e : new ImplementationError(e);
      ae.print(sys);
      return new String[0];
    }
  }
  String[] special(String ex) {
    try {
      sys.ucmd(ex);
    } catch (Throwable e) {
      e.printStackTrace();
    }
    return new String[0];
  }
}
static class Ed extends Editor {
  Scope sc;
  Ed(Scope sc, String name, String val) {
    super(name, val);
    this.sc = sc;
  }
  void save(String val) {
    println(val);
    try {
      // println(name, Main.exec(val, sc, sc.sys.defArgs));
      sc.set(name, Main.exec(val, sc, sc.sys.defArgs));
    } catch (Throwable t) {
      println(t.getMessage());
      glSys.lastError = t instanceof BQNError? (BQNError) t : new ImplementationError(t);
    }
  }
}
