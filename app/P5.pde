static class P5Tab extends Tab {
  
  SaveNfield code;
  
  P5Tab(String fname) {
    this.name = fname==null? "unnamed" : fname;
    
    code = new SaveNfield(fname) {
      void eval() {
        P5Tab.this.eval();
      }
      void extraSpecial(String s) {
        if (s.equals("close")) {
          // TODO warn about closing without saving
          topbar.closeCurr();
        } else println("unknown special " + s);
      }
      void redraw() { super.redraw();
        textInput = code; // TODO think about whether there's a better place to place this
      }
    };
    code.lineNumbering = true;
    code.xoff = -1;
    code.set(0, 0);
    
    vw = new KBView(code);
  }
  
  void mouseWheel(int dir) {
    code.mouseWheel(dir);
  }
  
  String name;
  String name() { return name; }
  
  P5Disp e;
  void eval() {
    String s = code.allText();
    if (e!=null) e.stop();
    
    Sys sys = ((DzaimaBQN)mainREPL.it).sys;
    Scope sc = new Scope(sys.gsc);
    
    e = new WP5Disp();
    // e = new TP5Disp();
    
    final P5Obj p5 = new P5Obj(e);
    sc.set("p5", p5);
    
    try {
      Main.exec(s, sc, new Value[0]);
    } catch(Throwable t) {
      t.printStackTrace();
      glSys.report(t);
      e.stop(); e=null;
      return;
    }
    
    
    if (p5.sz==null) p5.sz = new int[]{100, 100};
    
    e.start(p5.sz, new P5Impl() {
      public void setup(PGraphics g) {
        inv(e, p5.setup, Num.ONE);
        g.background(0);
        p5.g.g = g;
      }
      public void draw(PGraphics g) {
        inv(e, p5.draw, Num.ONE);
        p5.lm.tick(); p5.mm.tick(); p5.rm.tick();
        p5.fc++;
      }
      public void resized(PGraphics g, int w, int h) {
        g.background(0);
        p5.sz = new int[]{w, h};
        inv(e, p5.resized, new IntArr(new int[]{w,h}));
      }
      public void mouseEvent(MouseEvent e, boolean p) {
        int mb = e.getButton();
        if (!MOBILE) {
          Object n = e.getNative();
          if (n instanceof java.awt.event.MouseEvent) {
            int b = ((java.awt.event.MouseEvent) n).getButton();
            if      (b==java.awt.event.MouseEvent.BUTTON1) mb = LEFT;
            else if (b==java.awt.event.MouseEvent.BUTTON2) mb = CENTER;
            else if (b==java.awt.event.MouseEvent.BUTTON3) mb = RIGHT;
          }
        }
        p5.mb(mb, p);
      }
      public void closed() { }
    });
  }
}

static void inv(P5Disp e, Value f, Value x) {
  try {
    if (f!=null) f.call(x);
  } catch (Throwable t) {
    t.printStackTrace();
    glSys.report(t);
    e.stop();
  }
}

static class P5Obj extends SimpleMap {
  public String ln(FmtInfo f) { return "P5"; }
  
  P5Disp e;
  static final int a=0; // to make sure i don't accidentally use methods on PApplet
  P5Obj(P5Disp e) {
    this.e = e;
  }
  Value setup, draw, resized;
  GObj g = new GObj(null);
  int[] sz; // length==0 means fullscreen
  int fc;
  MB lm=new MB(LEFT), mm=new MB(CENTER), rm=new MB(RIGHT);
  void mb(int b, boolean p) {
    switch(b) {
      case CENTER: mm.p(p); break;
      case RIGHT : rm.p(p); break;
      default    : lm.p(p); break;
    }
  }
  void setv(String k, Value v) {
    switch (k) {
      case "size": case "sz":
        if (sz!=null) throw new DomainError("Calling p5.Size twice");
        if (v.ia!=2 && v.ia!=0) throw new DomainError("p5.size expected to be set to two integers or an empty array for fullscreen");
        sz = v.asIntVec();
        break;
      case "draw": draw = v; break;
      case "setup": setup = v; break;
      case "resized": resized = v; break;
      default: throw new DomainError("Setting non-existing key "+k+" for p5");
    }
  }
  Value getv(String k) {
    switch (k) {
      case "draw"   : return draw;
      case "setup"  : return setup;
      case "resized": return resized;
      case "size": case "sz": return sz==null? null : new IntArr(sz);
      case "g": return g;
      case "fc": case "framecount": return new Num(fc);
      case "mp": return new IntArr(e.mpos());
      case "mm": return mm; case "rm": return rm; case "lm": return lm;
      // case "fps": return new Num(a.frameRate);
      case "touches": int[] ts = e.touches(); return new IntArr(ts, new int[]{ts.length/2, 2});
      case "img": return new FnBuiltin() {
        public String ln(FmtInfo f) { return "p5.Img"; }
        public Value call(Value x) {
          if (x.r()!=2) throw new DomainError("p5.Img: Expected argument to be a rank 2 array");
          return new ImgObj(x);
        }
      };
      default: return null;
    }
  }
}
static class MB extends SimpleMap {
  public String ln(FmtInfo f) { return "p5."+(d==LEFT? "lm" : d==CENTER? "mm" : "rm"); }
  
  int d;
  MB(int d) { this.d = d; }
  
  boolean p;
  void p(boolean p) { this.p = p; }
  boolean pp;
  void tick() { pp = p; }
  
  void setv(String k, Value v) {
    switch (k) {
      default: throw new DomainError("Setting non-existing key "+k+" for p5");
    }
  }
  Value getv(String k) {
    switch (k) {
      case "p" : return     p? Num.ONE : Num.ZERO;
      case "pp": return    pp? Num.ONE : Num.ZERO;
      case "c" : return p&!pp? Num.ONE : Num.ZERO;
      case "r" : return !p&pp? Num.ONE : Num.ZERO;
      default: return null;
    }
  }
}

static class GObj extends SimpleMap {
  public String ln(FmtInfo f) { return "g"; }
  PGraphics g;
  GObj(PGraphics g) {
    this.g = g;
  }
  
  Value getv(String k) {
    if (g==null) throw new DomainError("Cannot use g before setup");
    switch (k) {
      case "background": case "bg": return new Fun() {
        public String ln(FmtInfo f) { return "g.Bg"; }
        public Value call(Value x) {
          g.background(col(x));
          return x;
        }
      };
      case "text": return new Fun() {
        public String ln(FmtInfo f) { return "g.Text"; }
        public Value call(Value w, Value x) {
          g.textSize(fg(w, 2));
          g.fill(col(w.get(3)));
          if (w.ia>4) g.textAlign(pick(w.get(4), "left",LEFT, "center",CENTER, "right",RIGHT),
                                  pick(w.get(5), "top",TOP, "center",CENTER, "bottom",BOTTOM, "baseline",BASELINE));
          g.text(Format.outputFmt(x), fg(w, 0), fg(w, 1));
          return x;
        }
      };
      case "ln": return new Fun() {
        public String ln(FmtInfo f) { return "g.Ln"; }
        public Value call(Value x) { return call(EmptyArr.SHAPE0N, x); }
        public Value call(Value w, Value x) { Value[] cs = cs(x); // args: x0 y0 x1 y1 col w=0
          int ta=w.ia+cs.length; if (ta!=5 && ta!=6) throw new DomainError("g.Ln: Expected 5 or 6 total items (x0,y0,x1,y1,color,[width])");
          float[] x0 = fs(p(cs,w,0)); float[] y0 = fs(p(cs,w,1));
          float[] x1 = fs(p(cs,w,2)); float[] y1 = fs(p(cs,w,3));
          int[] col =cols(p(cs,w,4)); float[] sw = fs(p(cs,w,5,Num.ONE));
          for (int i = 0; i < x0.length; i++) {
            g.stroke(col[i]); g.strokeWeight(sw[i]);
            g.line(x0[i], y0[i], x1[i], y1[i]);
          }
          return Num.ONE;
        }
      };
      case "rect": return new Fun() {
        public String ln(FmtInfo f) { return "g.Rect"; }
        public Value call(Value x) { return call(EmptyArr.SHAPE0N, x); }
        public Value call(Value w, Value x) { Value[] cs = cs(x); // args: x0 y0 x1 y1 fcol scol=0 w=0
          int ta=w.ia+cs.length; if (ta!=5 && ta!=7) throw new DomainError("g.Rect: Expected 5 or 7 total items (x0,y0,x1,y1,fill,[stroke,weight])");
          float[] x0 = fs(p(cs,w,0)); float[] y0 = fs(p(cs,w,1));
          float[] x1 = fs(p(cs,w,2)); float[] y1 = fs(p(cs,w,3));
          int[]   fc=cols(p(cs,w,4)); int[]   sc=cols(p(cs,w,5,new Num(0x00ffffff)));
          float[] sw = fs(p(cs,w,6,Num.ZERO)); g.rectMode(CORNERS);
          for (int i = 0; i < x0.length; i++) {
            g.fill(fc[i]); g.stroke(sc[i]); g.strokeWeight(sw[i]);
            g.rect(x0[i], y0[i], x1[i], y1[i]);
          }
          return Num.ONE;
        }
      };
      case "poly": return new Fun() {
        public String ln(FmtInfo f) { return "g.Poly"; }
        public Value call(Value w, Value x) { // fill‿[stroke‿w] g.Poly x0‿y0‿x1‿y1‿x2‿y2‿…
          float[] fs = fs(x); if (fs.length%2!=0) throw new DomainError("g.Poly: Expected even number of items in 𝕩");
          if (w.ia!=1 && w.ia!=3) throw new DomainError("g.Poly: Expected 1 or 3 items in 𝕨 (fill,[stroke,weight])");
          g.fill(col(w.get(0)));
          if (w.ia==1) g.noStroke();
          else { g.stroke(col(w.get(1))); g.strokeWeight((float)w.get(2).asDouble()); }
          g.beginShape();
          for (int i = 0; i < fs.length; i+= 2) g.vertex(fs[i], fs[i+1]);
          g.endShape(CLOSE);
          return Num.ONE;
        }
      };
      case "ellipse": return new Fun() {
        public String ln(FmtInfo f) { return "g.Ellipse"; }
        public Value call(Value x) { return call(EmptyArr.SHAPE0N, x); }
        public Value call(Value w, Value x) { Value[] cs = cs(x); // args: x0 y0 x1 y1 fcol scol=0 w=0
          int ta=w.ia+cs.length; if (ta!=5 && ta!=7) throw new DomainError("g.Ellipse: Expected 5 or 7 total items (x,y,w,h,fill,[stroke,weight])");
          float[] xc = fs(p(cs,w,0)); float[] yc = fs(p(cs,w,1));
          float[] xd = fs(p(cs,w,2)); float[] yd = fs(p(cs,w,3));
          int[]   fc=cols(p(cs,w,4)); int[]   sc=cols(p(cs,w,5,new Num(0x00ffffff)));
          float[] sw = fs(p(cs,w,6,Num.ZERO)); g.ellipseMode(DIAMETER);
          for (int i = 0; i < xc.length; i++) {
            g.fill(fc[i]); g.stroke(sc[i]); g.strokeWeight(sw[i]);
            g.ellipse(xc[i], yc[i], xd[i], yd[i]);
          }
          return Num.ONE;
        }
      };
      case "circle": return new Fun() {
        public String ln(FmtInfo f) { return "g.Circle"; }
        public Value call(Value x) { return call(EmptyArr.SHAPE0N, x); }
        public Value call(Value w, Value x) { Value[] cs = cs(x); // args: x0 y0 x1 y1 fcol scol=0 w=0
          int ta=w.ia+cs.length; if (ta!=4 && ta!=6) throw new DomainError("g.Circle: Expected 4 or 6 total items (x,y,d,fill,[stroke,weight])");
          float[] xc = fs(p(cs,w,0)); float[] yc = fs(p(cs,w,1));
          float[] d  = fs(p(cs,w,2));
          int[] fc=cols(p(cs,w,3));
          int[] sc=cols(p(cs,w,4,new Num(0x00ffffff))); float[] sw = fs(p(cs,w,5,Num.ZERO));
          g.ellipseMode(DIAMETER);
          for (int i = 0; i < xc.length; i++) {
            g.fill(fc[i]); g.stroke(sc[i]); g.strokeWeight(sw[i]);
            g.ellipse(xc[i], yc[i], d[i], d[i]);
          }
          return Num.ONE;
        }
      };
      case "img": return new Fun() {
        public String ln(FmtInfo f) { return "g.Img"; }
        public Value call(Value x) { return call(new IntArr(new int[]{0, 0}), x); }
        public Value call(Value w, Value x) {
          int[] pos = w.asIntVec(); if (pos.length!=2 && pos.length!=4) throw new DomainError("g.Img: Expected 2 or 4 arguments in 𝕨");
          ImgObj img;
          if (x instanceof ImgObj) img = (ImgObj) x;
          else if (x.r()!=2) throw new DomainError("g.Img: Expected image or rank 2 array as 𝕩");
          img = new ImgObj(x); g.imageMode(CORNER);
          if (pos.length>2) g.image(img.i, pos[0], pos[1], pos[2], pos[3]);
          else g.image(img.i, pos[0], pos[1]);
          return Num.ONE;
        }
      };
      default: return null;
    }
  }
  void setv(String k, Value v) {
    if (g==null) throw new DomainError("Cannot use g before setup");
    switch (k) {
      
      default: throw new DomainError("Setting non-existing key "+k+" for g");
    }
  }
}

static class ImgObj extends SimpleMap {
  public String ln(FmtInfo f) { return "img"; }
  PImage i;
  ImgObj(PImage i) { this.i = i; }
  ImgObj(Value px) {
    int[] fc=cols(px);
    i = a.createImage(px.shape[1], px.shape[0], ARGB);
    i.loadPixels();
    System.arraycopy(fc, 0, i.pixels, 0, i.pixels.length);
  }
  void setv(String k, Value v) {
    switch (k) {
      default: throw new DomainError("Setting non-existing key "+k+" for image");
    }
  }
  Value getv(String k) {
    switch (k) {
      case "pixels": i.loadPixels(); return new IntArr(i.pixels, new int[]{i.height, i.width});
      case "sz": case "size": return new IntArr(new int[]{i.height, i.width});
      case "w": return Num.of(i.width );
      case "h": return Num.of(i.height);
      default: return null;
    }
  }
}

static int pick(Value v, Object... a) {
  String vs = v.asString();
  for (int i = 0; i < a.length; i+= 2) {
    if (vs.equals(a[i])) return (int) a[i+1];
  }
  throw new DomainError("Unexpected value \""+vs+"\"");
}

static Value p(Value[] cols, Value extra, int pos, Value def) {
  if (pos>=cols.length) {
    pos-= cols.length;
    return new SingleItemArr(pos>=extra.ia? def : extra.get(pos), cols[0].shape);
  }
  return cols[pos];
}
static Value p(Value[] cols, Value extra, int pos) {
  if (pos>=cols.length) {
    pos-= cols.length;
    if (pos>=extra.ia) throw new LengthError("Not enough provided items");
    return new SingleItemArr(extra.get(pos), cols[0].shape);
  }
  return cols[pos];
}
static Value[] cs(Value v) {
  if (v.ia==0) throw new DomainError("Expected at least one column");
  if (v.r() == 1) {
    Value[] res = v.values(); boolean cp = false;
    int[] sh0 = null;
    for (Value c : res) if (c.r()!=0) { sh0 = c.shape; break; }
    if (sh0==null) return res;
    for (int i = 0; i < res.length; i++) {
      Value c = res[i];
      if (!Arrays.equals(sh0, c.shape)) {
        if (c.r()!=0) throw new DomainError("Expected all vectors to have equal shapes");
        if (!cp) { cp=true; res = res.clone(); }
        res[i] = new SingleItemArr(c.first(), sh0);
      }
    }
    return res;
  }
  if (v.r() == 2) return CellBuiltin.cells(new TransposeBuiltin().call(v));
  throw new DomainError("Expected rank∊1‿2, got shape "+Main.formatAPL(v.shape));
}
static float[][] csF(Value[] cs) {
  if (cs.length==0) return new float[0][0];
  float[][] res = new float[cs.length][];
  for (int i = 0; i < res.length; i++) {
    res[i] = fs(cs[i]);
  }
  return res;
}

static float[] fs(Value x) {
  double[] ds = x.asDoubleArr();
  float[] fs = new float[ds.length];
  for (int j = 0; j < fs.length; j++) fs[j] = (float)ds[j];
  return fs;
}
static int[] is(Value x) {
  return x.asIntArr();
}
static int[] cols(Value x) {
  if (x.quickIntArr()) return x.asIntArr();
  int[] res = new int[x.ia];
  for (int i = 0; i < x.ia; i++) {
    res[i] = col(x.get(i));
  }
  return res;
}

static int col(Value v) {
  if (v instanceof Num) return (int)(long) ((Num) v).asDouble(); // weird to make sure ints >2⋆31 wrap to negatives
  String s = v.asString();
  if (s.length() == 1) {
    int i = Integer.parseInt(s, 16);
    return i*0x111111 | 0xff000000;
  }
  if (s.length() == 6) return Integer.parseInt(s, 16) | 0xff000000;
  if (s.length() == 8) return (int) Long.parseLong(s, 16);
  if (s.length() == 2) {
    int i = Integer.parseInt(s, 16);
    return i | (i<<8) | (i<<16) | 0xff000000;
  }
  if (s.length() == 3) {
    int i = Integer.parseInt(s, 16);
    return ((i&0xf)*17) | ((i&0xf0)*17 << 4) | ((i&0xf00)*17 << 8) | 0xff000000;
  }
  throw new DomainError("bad color "+v);
}
static float fg(Value v, int i) {
  if (v.r()>1) throw new RankError("Expected vector argument, got shape "+Main.formatAPL(v.shape), null);
  if (i >= v.ia) throw new LengthError("Expected at least "+(i+1)+" items, got "+v.ia);
  return (float) v.get(i).asDouble();
}

abstract static class P5Disp {
  AtomicBoolean running = new AtomicBoolean();
  abstract void start(int[] sz, P5Impl u); // only called once
  abstract void stop(); // should handle multiple calls from multiple threads safely
  abstract int[] touches();
  abstract int[] mpos();
}
abstract static class P5Impl {
  abstract void setup(PGraphics g);
  abstract void draw(PGraphics g);
  abstract void mouseEvent(MouseEvent e, boolean pressed);
  abstract void resized(PGraphics g, int w, int h);
  abstract void closed();
}









static class WP5Disp extends P5Disp {
  PWindow win = new PWindow();
  void start(int[] sz, P5Impl u) {
    assert running.compareAndSet(false, true);
    win.create(sz, u, this);
  }
  void stop() {
    if (running.compareAndSet(true, false)) {
      win.close();
      win = null;
    }
  }
  void close() {
    running.set(false);
  }
  int[] touches() {
    return touchIs(win);
  }
  int[] mpos() {
    return new int[]{win.mouseX, win.mouseY};
  }
}
static class TP5Disp extends P5Disp {
  P5Tab tab;
  P5Impl u;
  void start(int[] sz, P5Impl u) {
    boolean was = running.compareAndSet(false, true); assert was;
    this.u = u;
    topbar.toNew(tab = new P5Tab(sz));
    //new Error("start called").printStackTrace();
  }
  void stop() {
    //println("p1", running.get());
    //new Error("stop called").printStackTrace();
    if (running.compareAndSet(true, false)) {
      //println("p2", running.get());
      u.closed();
      topbar.close(tab);
    } // else println("p2 --");
  }
  
  class P5Tab extends Tab {
    final P5TabDrawable vwc;
    P5Tab(int[] sz) {
      vw = vwc = new P5TabDrawable(sz);
    }
    void close() {
      stop();
    }
    
    class P5TabDrawable extends Drawable {
      int tw, th;
      boolean fullscreen;
      PGraphics g;
      P5TabDrawable(int[] sz) {
        if (sz.length==2) {
          tw = sz[0];
          th = sz[1];
        } else tw=th=100;
        g = a.createGraphics(tw, th);
        fullscreen = sz.length!=2;
      }
      boolean firstRedraw = true;
      void redraw() {
        if ((tw!=w || th!=h) && fullscreen  ||  firstRedraw) {
          firstRedraw = false;
          if (fullscreen) {
            tw = w;
            th = h;
          }
          g = a.createGraphics(tw, th);
          g.beginDraw();
          u.resized(g, tw, th);
          g.endDraw();
        }
        d.fill(#111111);
        d.noStroke();
        d.rectMode(CORNER);
        d.rect(x,y,w,h);
        draw();
      }
      boolean firstDraw = true;
      void draw() {
        g.beginDraw();
        if(firstDraw) u.setup(g);
        u.draw(g);
        g.endDraw();
        beginClip(d, x, y, x+w, y+h);
        d.image(g, dx(), dy());
        endClip(d);
      }
      void mouseEvent(MouseEvent e, boolean pressed) {
        u.mouseEvent(e, pressed);
      }
    }
    String name() {
      return "P5Tab";
    }
    int dx() { return vwc.x+(vwc.w-vwc.tw)/2; }
    int dy() { return vwc.y+(vwc.h-vwc.th)/2; }
  }
  int[] touches() {
    int[] ts = touchIs(a);
    int tx = tab.dx();
    int ty = tab.dy();
    for (int i = 0; i < ts.length; i+= 2) {
      ts[i  ]-= tx;
      ts[i+1]-= ty;
    }
    return ts;
  }
  int[] mpos() {
    return new int[]{a.mouseX-tab.dx(), a.mouseY-tab.dy()};
  }
}
