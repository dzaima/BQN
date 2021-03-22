abstract static class Tab extends SimpleMap {
  public String ln(FmtInfo f) { return "tab["+name()+"]"; }
  
  abstract String name();
  
  
  void opened() { }
  void close() { }
  
  Drawable vw;
  
  Value getv(String k) {
    switch (k) {
      case "name": return Main.toAPL(name());
      case "close": return new Fun() {
        public String ln(FmtInfo f) { return Tab.this+".close"; }
        public Value call(Value w) {
          topbar.close(Tab.this);
          return Num.ONE;
        }
      };
      default: return null;
    }
  }
  void setv(String k, Value v) {
    String s = k.toLowerCase();
    switch (k) {
      default: throw new DomainError("Setting non-existing key "+s+" for tab");
    }
  }
}


static class REPL extends Tab {
  
  final HView hist = new HView();
  final IField inp = new IField();
  
  REPL() {
    vw = new KBView(new Drawable() {
      void redraw() {
        int lnh = 4;
        d.fill(#101010);
        d.noStroke();
        d.rectMode(CORNER);
        d.rect(x, y, w, h);
        hist.upd(x, y, w, h-isz-lnh);
        inp.upd(x, y+h-isz, w, isz);
        textInput = inp;
        
        d.stroke(0x80D2D2D2);
        d.strokeWeight(1);
        d.line(hist.x+4, hist.y+hist.h+lnh/2, hist.x+hist.w-4, hist.y+hist.h+lnh/2);
        
      }
      void draw() {
        hist.draw();
        inp.draw();
      }
      void mouseWheel(int am) {
        if (hist.mouseInMe()) hist.mouseWheel(am);
      }
    });
  }
  
  Interpreter it = new DzaimaBQN();
  ArrayList<String> inputs = new ArrayList();
  String tmpSaved;
  int iptr = 0; // can be ==input.size()
  String name() { return "REPL"; }
  Value getv(String k) {
    if (k.equals("eq")) return Main.toAPL(inp.lns.get(0).toString());
    return super.getv(k);
  }
  void setv(String k, Value v) {
    if (k.equals("eq")) { inp.clear(); inp.append(((Value) v).asString()); }
    else super.setv(k, v);
  }
  void mouseWheel(int dir) {
    hist.mouseWheel(dir);
  }
  
  
  
  
  
  class HView extends Nfield {
    HView() {
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
    void draw() {
      // if (textInput == this) textInput = input;
      super.draw();
    }
    void lnDTapped(int y) {
      String l = lns.get(y).toString();
      if (l.startsWith("   ")) inp.setln(l.substring(3));
      textInput = inp;
    }
    void lnTapped(int y) { textInput = inp; }
    void dragged()       { textInput = inp; }
  }
  
  class IField extends Nfield {
    IField() {
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
    void redraw() { super.redraw();
      setsz(isz*.8);
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
          if (nm.equals("hsz")) hist.setsz(int(arg));
          else if (nm.equals("isz")) {
            isz = ceil(Float.parseFloat(arg)/.8);
            redrawAll();
          } else if (nm.equals("i")) {
            if (argl.equals("dzaima")) it = new DzaimaBQN();
          } else if (nm.equals("clear")) {
            hist.clear();
          } else if (nm.equals("g")) {
            topbar.toNew(new Grapher(it, arg));
          } else if (nm.equals("d")) {
            topbar.toNew(new P5Tab(arg.length()==0? null : arg));
          } else if (nm.equals("tsz")) {
            all.tsz = int(arg);
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
                  } catch (BQNError e) {
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
                 if (o instanceof FunBlock) topbar.toNew(new Ed(sc, vn, ((FunBlock) o).code.source()));
            else if (o instanceof Md1Block) topbar.toNew(new Ed(sc, vn, ((Md1Block) o).code.source()));
            else if (o instanceof Md2Block) topbar.toNew(new Ed(sc, vn, ((Md2Block) o).code.source()));
            else textln("cannot edit type "+(o instanceof Fun? o.getClass().getSimpleName() : o.humanType(false)));
          } else textln("Command "+nm+" not found");
          //else if (nm.equals(""))
          return;
        }
        
        String[] res = it.repl(line);
        for (String ln : res) textln(ln);
      } catch (Throwable t) {
        BQNError e = t instanceof BQNError? (BQNError)t : new ImplementationError(t);
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
        hist.end();
      } else if (s.equals("pgup") || s.equals("pgdn") || s.equals("home") || s.equals("end")) {
        hist.special(s);
      }
    }
    void textln(String ln) {
      hist.appendLns(ln);
    }
  }
  
}



abstract static class Editor extends Tab {
  String name;
  void eval() { }
  Nfield ta = new Nfield() {
    void saved() {
      save(ta.allText());
    }
    void eval() {
      Editor.this.eval();
    }
    void extraSpecial(String s) {
      if (s.equals("close")) {
        save(ta.allText());
        topbar.closeCurr();
      } else println("unknown special " + s);
    }
    void redraw() { super.redraw();
      textInput = ta; // TODO think about whether there's a better place to place this
    }
    void draw() { super.draw();
      Editor.this.draw();
    }
  };
  
  void draw() { }
  
  
  Editor(String name, String val) {
    this.name = name;
    ta.lineNumbering = true;
    ta.xoff = -1;
    ta.append(val);
    ta.setE(0, 0); ta.allE();
    
    vw = new KBView(ta);
  }
  abstract void save(String val);
  String name() {
    return name;
  }
  void mouseWheel(int dir) {
    ta.mouseWheel(dir);
  }
}


static class Grapher extends Tab {
  Graph g = new Graph();
  Interpreter it;
  
  final Nfield input = new Nfield() {
    void eval() {
      modified();
    }
    void redraw() { super.redraw();
      setsz(isz*.8);
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
        topbar.closeCurr();
      } else if (s.equals("newline")) {
        eval();
      } else println("unknown special " + s);
    }
  };
  Value last;
  
  Grapher(Interpreter it, String def) {
    this.it = it;
    input.multiline = false;
    input.append(def);
    
    vw = new KBView(new Drawable() {
      void redraw() {
        g.upd(x, y, w, h-isz);
        input.upd(x, y+h-isz, w, isz);
        textInput = input;
      }
      void draw() {
        g.draw();
        input.draw();
      }
      void mouseWheel(int dir) {
        g.mouseWheel(dir);
      }
    });
  }
  String name() {
    return "grapher";
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
