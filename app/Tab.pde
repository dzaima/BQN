abstract static class Tab extends SimpleMap {
  abstract void show();
  abstract void hide();
  abstract String name();
  void mouseWheel(int dir) { }
  Value getv(String k) {
    switch (k) {
      case "name": return Main.toAPL(name());
      case "close": return new Fun() {
        public String repr() { return Tab.this+".close"; }
        public Value call(Value w) {
          topbar.close(Tab.this);
          return Num.ONE;
        }
      };
      default: return Null.NULL;
    }
  }
  void setv(String k, Value v) {
    String s = k.toLowerCase();
    switch (k) {
      default: throw new DomainError("setting non-existing key "+s+" for tab");
    }
  }
  String toString() { return "tab["+name()+"]"; }
}


static class REPL extends Tab {
  
  final HView historyView;
  final IField input;
  final Drawable line = new Drawable(0, 0, 0, 0) {
    void redraw() {
      d.stroke(0x80D2D2D2);
      d.strokeWeight(1);
      d.line(historyView.x+4, historyView.y+historyView.h+2, historyView.x+historyView.w-4, historyView.y+historyView.h+2);
    }
  };
  Interpreter it = new DzaimaBQN();
  ArrayList<String> inputs = new ArrayList();
  String tmpSaved;
  int iptr = 0; // can be ==input.size()
  REPL() {
    historyView = new HView(0, top, a.width, 340-top); // new ROText(0, top, a.width, 340-top);
    input = new IField(0, 350, a.width, 40);
  }
  void show() {
    int ih = int(input.chrH*1.2);
    d.noStroke();
    d.fill(#101010);
    d.rectMode(CORNER);
    d.rect(0, top, d.width, freey()-top-ih);
    input.move(0, freey()-ih, d.width, ih);
    historyView.move(0, top, d.width, freey()-top-ih-6);
    historyView.end();
    input.show();
    historyView.show();
    line.show();
    textInput = input;
  }
  void hide() {
    input.hide();
    historyView.hide();
    line.hide();
    if (textInput == input) textInput = null;
  }
  String name() {
    return "REPL";
  }
  Value getv(String k) {
    if (k.equals("eq")) return Main.toAPL(input.lns.get(0).toString());
    return super.getv(k);
  }
  void setv(String k, Value v) {
    if (k.equals("eq")) { input.clear(); input.append(((Value) v).asString()); }
    else super.setv(k, v);
  }
  void mouseWheel(int dir) {
    historyView.mouseWheel(dir);
  }
  
  
  
  
  
  class HView extends Nfield {
    HView(int x, int y, int w, int h) {
      super(x, y, w, h);
      bottom = true;
      editable = false;
    }
    boolean highlight() { return false; }
    void end() {
      ey = lns.size()-1;
      ex = 0;
      allE();
      dyoff = yoff = h-chrH*lns.size()-chrH;
    }
    void tick() {
      // if (textInput == this) textInput = input;
      super.tick();
    }
    void lnDTapped(int y) {
      String l = lns.get(y).toString();
      if (l.startsWith("   ")) input.setln(l.substring(3));
      textInput = input;
    }
    void lnTapped(int y) { textInput = input; }
    void dragged()       { textInput = input; }
  }
  
  class IField extends Nfield {
    IField(int x, int y, int w, int h) {
      super(x, y, w, h);
      multiline = false;
    }
    Line ln() {
      return lns.get(0);
    }
    void setln(String ln) { textChanged = true;
      lns.set(0, new Line(ln));
    }
    boolean highlight() {
      Line ln = ln();
      return ln.len>0 && ln.ints[0]!=':' && ln.ints[0]!=')';
    }
    void eval() {
      try {
        tmpSaved = null;
        String line = ln().toString();
        if (line.length()==0) return;
        inputs.add(line);
        iptr = inputs.size();
        textln("   "+line);
        println("   "+line);
        if (line.startsWith(":")) {
          String cmd = line.substring(1);
          int i = cmd.indexOf(" "); 
          String nm = i==-1? cmd : cmd.substring(0, i);
          final String arg = i==-1? "" : cmd.substring(i+1);
          String argl = arg.toLowerCase();
          if (nm.equals("hsz")) historyView.setsz(int(arg));
          else if (nm.equals("isz")) {
            input.setsz(isz = int(arg));
            redrawAll();
          } else if (nm.equals("i")) {
            if (argl.equals("dyalog")) it = new Dyalog();
            if (argl.equals("dzaima")) it = new DzaimaBQN();
          } else if (nm.equals("clear")) {
            historyView.clear();
          } else if (nm.equals("g")) {
            topbar.toNew(new Grapher(it, arg));
          } else if (nm.equals("tsz")) {
            top = int(arg);
            redrawAll();
          } else if (nm.equals("f") || nm.equals("fx")) {
            final boolean ex = nm.equals("fx");
            String[] ps = arg.split("/");
            String[] lns = a.loadStrings(arg);
            topbar.toNew(new Editor(ps[ps.length-1], lns==null? "" : join(lns, "\n")) {
              public void save(String t) {
                try {
                  a.saveStrings(arg, new String[]{t});
                  if (ex) try {
                    it.exec(ta.allText());
                  } catch (APLError e) {
                    e.print(((DzaimaBQN)it).sys);
                  }
                  //if (ex) Main.exec(ta.allText(), ((DzaimaBQN) it).sys.gsc);
                } catch (Throwable e) {
                  e.printStackTrace();
                }
              }
            });
          } else if (nm.equals("ed")) {
            if (!(it instanceof DzaimaBQN)) { textln(":ed only available on dzaima/BQN"); return; }
            Scope sc = ((DzaimaBQN)it).sys.csc;
            String vn = arg;
            if (vn.startsWith("_")) {
              vn = vn.substring(1);
              if (vn.endsWith("_")) vn = vn.substring(0, vn.length()-1);
            }
            vn = vn.toLowerCase();
            sc = sc.owner(vn);
            if (sc == null) { textln("variable "+vn+" didn't exist"); return; }
            Value o = sc.get(vn);
                 if (o instanceof Dfn ) topbar.toNew(new Ed(sc, vn, ((Dfn ) o).code.source()));
            else if (o instanceof Dmop) topbar.toNew(new Ed(sc, vn, ((Dmop) o).code.source()));
            else if (o instanceof Ddop) topbar.toNew(new Ed(sc, vn, ((Ddop) o).code.source()));
            else textln("cannot edit type "+(o instanceof Fun? o.getClass().getSimpleName() : o.humanType(false)));
          } else textln("Command "+nm+" not found");
          //else if (nm.equals(""))
          return;
        }
        
        String[] res = it.repl(line);
        for (String ln : res) textln(ln);
      } catch (Throwable t) {
        APLError e = t instanceof APLError? (APLError)t : new ImplementationError(t);
        e.print(((DzaimaBQN)it).sys);
      }
    }
    void extraSpecial(String s) {
      final String line = ln().toString();
      if (s.equals("up")) {
        if (inputs.size() == 0) return;
        
        if (line.length() != 0 && iptr == inputs.size()) tmpSaved = line;
        iptr = Math.max(0, iptr-1);
        setln(inputs.get(iptr));
        set(0, ln().len);
      } else if (s.equals("down")) {
        if (inputs.size() == 0) return;
        
        iptr++;
        if (iptr >= inputs.size()) {
          iptr = inputs.size();
          setln(tmpSaved==null? "" : tmpSaved);
        } else {
          setln(inputs.get(iptr));
        }
        set(0, ln().len);
      } else if (s.equals("copy")) {
        append("app.cpy");
      } else if (s.equals("newline")) {
        eval();
        clear();
        historyView.end();
      } else if (s.equals("pgup") || s.equals("pgdn") || s.equals("home") || s.equals("end")) {
        historyView.special(s);
      }
    }
    void textln(String ln) {
      historyView.appendLns(ln);
    }
  }
  
}



abstract static class Editor extends Tab {
  String name;
  Nfield ta;
  Editor(String name, String val) {
    this.name = name;
    ta = new Nfield(0, 0, 10, 10) {
      void eval() {
        save(ta.allText());
      }
      void extraSpecial(String s) {
        if (s.equals("close")) {
          eval();
          topbar.close();
        } else println("unknown special " + s);
      }
    };
    ta.lineNumbering = true;
    ta.xoff = -1;
    ta.append(val);
    ta.setE(0, 0); ta.allE();
  }
  abstract void save(String val);
  void show() {
    ta.move(0, top, d.width, freey()-top);
    ta.show();
    textInput = ta;
  }
  void hide() {
    ta.hide();
  }
  String name() {
    return name;
  }
  void mouseWheel(int dir) {
    ta.mouseWheel(dir);
  }
}


static class Grapher extends Tab {
  Graph g;
  final Nfield input;
  Value last;
  Interpreter it;
  Grapher(final Interpreter it, String def) {
    g = new Graph(0, top, d.width, freey()-top-isz);
    input = new Nfield(0, 350, d.width, 40) {
      void eval() {
        modified();
      }
      void modified() {
        if (it instanceof DzaimaBQN) {
          try {
            last = it.exec(lns.get(0).toString());
            g.newFun(last);
          } catch (Throwable e) {
            last = null;
          }
        }
      }
      void extraSpecial(String s) {
        if (s.equals("close")) {
          eval();
          topbar.close();
        } else if (s.equals("newline")) {
          eval();
        } else println("unknown special " + s);
      }
    };
    input.multiline = false;
    input.append(def);
  }
  
  void show() {
    int ih = int(input.chrH*1.2);
    g.move(0, top, d.width, freey()-top-ih);
    g.show();
    input.move(0, freey()-ih, d.width, ih);
    input.show();
    textInput = input;
  }
  void hide() {
    g.hide();
    input.hide();
  }
  String name() {
    return "grapher";
  }
  void mouseWheel(int dir) {
    g.mouseWheel(dir);
  }
  Value getv(String k) {
    if (k.equals("eq")) return Main.toAPL(input.lns.get(0).toString());
    if (k.equals("am")) return new Num(g.pts);
    if (k.equals("ln")) return new Num(g.joined? 1 : 0);
    if (k.equals("sz")) return new Num(g.ph);
    if (k.equals("x" )) return new Num(g.fullX + (g.x + g.w/2)/g.fullS);
    if (k.equals("y" )) return new Num(g.fullY + (g.y + g.h/2)/g.fullS);
    if (k.equals("w" )) return new Num(g.w/g.fullS);
    if (k.equals("freq")) return new Num(g.freq);
    if (k.equals("batch")) return new Num(g.mul);
    if (k.equals("gd")) return new DoubleArr(new double[]{g.pts, g.joined? 1 : 0, g.ph, g.mul});
    return super.getv(k);
  }
  void setv(String k, Value v) {
    if (k.equals("eq")) { input.clear(); input.append(((Value) v).asString()); }
    else if (k.equals("am"   )) g.pts = ((Value)v).asInt();
    else if (k.equals("ln"   )) g.joined = Main.bool(v);
    else if (k.equals("sz"   )) g.ph = (float)((Value)v).asDouble();
    else if (k.equals("batch")) g.mul = ((Value)v).asInt();
    else if (k.equals("x")) g.fullX = ((Value) v).asDouble() - (g.x + g.w/2)/g.fullS;
    else if (k.equals("y")) g.fullY = ((Value) v).asDouble() - (g.y + g.h/2)/g.fullS;
    else if (k.equals("w")) {
      double wnt = g.w / (float) ((Value) v).asDouble();
      double sc = wnt / g.fullS;
      double pS = g.fullS;
      g.fullS*= sc;
      double scalechange = 1/g.fullS - 1/pS;
      g.fullX-= ((g.x+g.w/2) * scalechange);
      g.fullY-= ((g.y+g.h/2) * scalechange);
    }
    else if (k.equals("freq")) g.freq = ((Value) v).asInt();
    else if (k.equals("gd")) { Value a = (Value) v; setv("am", a.get(0)); setv("ln", a.get(1)); setv("sz", a.get(2)); setv("batch", a.get(3)); }
    else super.setv(k, v);
  }
}
