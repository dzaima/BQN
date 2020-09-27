static class TabSelect extends Drawable {
  int tsz;
  TopBar tb;
  Tab ctab;
  TabSelect() {
    tsz = MOBILE? scale*3/2 : scale;
    tb = new TopBar(this);
  }
  void setTsz(int ntsz) {
    tsz = ntsz;
    redraw();
  }
  void redraw() {
    tb.upd(x, y, w, tsz);
    redrawTab();
  }
  void redrawTab() {
    ctab.vw.upd(x, y+tsz, w, h-tsz);
  }
  void draw() {
    tb.draw();
    ctab.vw.draw();
  }
  void mouseWheel(int am) {
    ctab.vw.mouseWheel(am);
  }
}
static class TopBar extends Drawable {
  ArrayList<Tab> tabs = new ArrayList();
  TabSelect ts;
  TopBar(TabSelect ts) {
    this.ts = ts;
  }
  void draw() {
    if (smouseIn() && a.mousePressed && !pmousePressed) {
      d.textSize(h*.8);
      int cx = x;
      for (Tab t : tabs) {
        String n = t.name();
        int dx = max(2*h, ceil(d.textWidth(n)) + h/2);
        if (a.mouseX > cx && a.mouseX < cx + dx) to(t);
        cx+= dx;
      }
    }
  }
  void redraw() {
    d.textSize(h*.8);
    d.rectMode(CORNER);
    d.fill(#222222);
    d.noStroke();
    d.rect(x, y, w, h);
    
    int cx = x;
    for (Tab t : tabs) {
      String n = t.name();
      int dx = max(2*h, ceil(d.textWidth(n)) + h/2);
      if (t == ts.ctab) {
        d.fill(#333333);
        d.rect(cx, y, dx, h);
      }
      cx+= dx;
    }
    
    d.fill(#D2D2D2);
    d.textAlign(CENTER, CENTER);
    cx = x;
    for (Tab t : tabs) {
      String n = t.name();
      int dx = max(2*h, ceil(d.textWidth(n)) + h/2);
      d.text(n, cx + dx/2, y + h*.4);
      cx+= dx;
    }
  }
  void to(Tab t) {
    if (ts.ctab!=null) ts.ctab.vw.setVisible(false);
    ts.ctab = t;
    t.vw.setVisible(true);
    ts.redraw();
  }
  void move(int d) {
    int i = tabs.indexOf(ts.ctab) + d;
    i%= tabs.size();
    if (i < 0) i+= tabs.size();
    to(tabs.get(i));
  }
  void toNew(Tab t) {
    tabs.add(t);
    to(t);
  }
  void add(Tab t) {
    tabs.add(t);
    redraw();
  }
  
  void close() {
    close(ts.ctab);
  }
  void close(Tab t) {
    if (tabs.size() == 1) return;
    int i = tabs.indexOf(t);
    tabs.remove(i);
    if (t==ts.ctab) to(tabs.get(Math.max(0, i-1)));
    else ts.redraw();
  }
}


static class KBView extends Drawable {
  Drawable ct;
  KBView(Drawable ct) {
    this.ct = ct;
  }
  void redraw() {
    ct.upd(x, y       , w, h-kb.h);
    kb.upd(x, y+h-kb.h, w, kb.h  );
  }
  void mouseWheel(int am) {
    ct.mouseWheel(am);
  }
  void draw() { ct.draw(); kb.draw(); }
}
