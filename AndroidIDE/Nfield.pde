static class Nfield extends Drawable implements TextReciever {
  ArrayList<Line> lns;
  int sx, sy, ex, ey;
  float xoff, dyoff, yoff; // yoff is the target yoff, dyoff is what's actually used for drawing
  boolean textChanged = true; // implies moved
  boolean moved = true;
  boolean bottom;
  boolean editable = true;
  
  int tt; // caret flicker timer
  Nfield(int x, int y, int w, int h) {
    super(x, y, w, h);
    sx = sy = ex = ey = 0;
    lns = new ArrayList();
    lns.add(new Line(""));
    setsz(scale);
  }
  
  SyntaxHighlight hl;
  Theme th = new Theme();
  
  float chrH, chrW; // text size, char width
  void setsz(float sz) {
    chrH = sz;
    d.textSize(chrH);
    chrW = d.textWidth('H');
  }
  void mouseWheel(int dir) {
    yoff-= dir*chrH*4;
  }
  
  void tick() {
    //println(allText(), sx,sy,ex,ey,lns.size(),len(0));
    if (!visible) return;
    if (a.mousePressed && !pmousePressed && smouseIn() && a.mouseButton!=CENTER) textInput = this;
    beginClip(d, x, y, w, h);
    d.textSize(chrH);
    if (a.mousePressed && smouseIn() && (MOBILE || a.mouseButton==CENTER)) {
      yoff+= a.mouseY-a.pmouseY; dyoff = yoff;
      xoff+= a.mouseX-a.pmouseX;
    }
    
    {
      int max = 0;
      for (Line l : lns) max = max(max, l.len);
      float maxx = (max - 2)*chrW;
      if (xoff < -maxx) xoff = (int) -maxx;
      if (w > (max + 5)*chrW) xoff = 0;
      if (xoff > 0) xoff = 0;
      
      float maxy = chrH * (lns.size() - 2);
      if (yoff < -maxy) yoff = (int) -maxy;
      
      if (bottom) {
        yoff = Math.min(yoff, h-2*chrH);
      } else {
        if (yoff > 0) yoff = 0;
      }
      dyoff = dyoff + (yoff-dyoff)*.6;
      if (Math.abs(yoff-dyoff) < 1) dyoff = yoff;
    }
    
    if (textChanged) moved = true;
    if (moved || this!=textInput) tt = 70;
    if (moved) {
      int py = (int)(chrH*ey + yoff+y  + chrH*.1); // cannot use posy because that uses dyoff 
      int yb = (int) (h-chrH*1.3);
      if (py < y   ) yoff =  -ey*chrH;
      if (py > y+yb) yoff =  -ey*chrH+yb;
      if (Math.abs(dyoff-yoff) > h/10) dyoff = yoff+Math.signum(dyoff-yoff)*(h/10);
      moved = false;
    }
    
    if (smouseIn()) {
      int cy = constrain(floor((a.mouseY-y-dyoff)/chrH), 0, lns.size()-1);
      int cx = constrain(round((a.mouseX-x-xoff)/chrW), 0, len(cy));
      if (MOBILE) {
        if (pmousePressed && !a.mousePressed && dist(a.mouseX, a.mouseY, smouseX, smouseY) < 10) {
          set(cy, cx);
        }
      } else if (a.mousePressed && a.mouseButton==LEFT) {
        if (shift || pmousePressed) setE(cy, cx);
        else set(cy, cx);
      }
    }
    
    d.fill(#101010);
    d.noStroke();
    d.rectMode(CORNER);
    d.rect(x, y, w, h);
    d.textAlign(LEFT, TOP);
    d.rectMode(CORNERS);
    if (sel()) { // draw selection
      d.fill(0x20ffffff);
      d.noStroke();
      boolean swp = swapped();
      if (swp) swap();
      PVector ps = pos(sy, sx);
      PVector pe = pos(ey, ex);
      if (sy == ey) {
        d.rect(ps.x, ps.y, pe.x, pe.y+chrH);
      } else {
        float p0 = posx(0);
        d.rect(ps.x, ps.y, posx(len(sy)), ps.y+chrH);
        d.rect(p0  , pe.y, pe.x         , pe.y+chrH);
        for (int i = sy+1; i <= ey-1; i++) d.rect(p0, posy(i), posx(len(i)), posy(i)+chrH);
      }
      if (swp) swap();
    }
    
    if (textChanged) {
      hl = new SyntaxHighlight(allText(), th, d);
      textChanged = false;
    }
    if (hl!=null) hl.draw(x + xoff, y + dyoff, y, y+h, chrH, hl.lnstarts[ey]+lns.get(ey).UTF16before(ex)); // draw text
    
    
    tt--;
    if (tt < 0) tt = 60;
    if ((tt>28 || tt<2) && editable) { // draw caret
      int dtt = tt>30? 3 : Math.abs(15-tt)-14;
      d.stroke(th.caret & (0x3fffffff | dtt<<30));
      PVector p = pos(ey, ex);
      d.line(p.x, p.y, p.x, p.y+chrH);
    }
    endClip(d);
  }
  float posx(int cx) {
    return x + chrW*cx + xoff;
  }
  float posy(int cy) {
    return chrH*cy + dyoff+y  + chrH*.1;
  }
  PVector pos(int cy, int cx) {
    return new PVector(posx(cx), posy(cy));
  }
  
  void redraw() {
    if (hl!=null) hl.g = d;
  }
  
  boolean sel() {
    return sx!=ex || sy!=ey;
  }
  boolean swapped() {
    return sy==ey? sx>ex : sy>ey;
  }
  void order() {
    if (swapped()) swap();
  }
  void swap() { int t;
    t = sx; sx = ex; ex = t;
    t = sy; sy = ey; ey = t;
  }
  void setS(int y, int x) { sx = x; sy = y; moved = true; }
  void setE(int y, int x) { ex = x; ey = y; moved = true; }
  void set(int y, int x) {
    setS(y, x);
    setE(y, x);
  }
  void allS() { setE(sy, sx); }
  void allE() { setS(ey, ex); }
  
  boolean mover(boolean shift) { moved = true;
    if (sel() && !shift) {
      if (ey==sy? ex>sx : ey>sy) allE(); else allS();
      return true;
    }
    Line l = lns.get(ey);
    if (ex == l.len) {
      if (ey+1 == lns.size()) return false;
      ex = 0;
      ey++;
    } else if (ctrl) {
      boolean m = mode(l.ints[ex]);
      while (ex!=l.len && mode(l.ints[ex])==m) ex++;
    } else ex++;
    if (!shift) allE();
    return true;
  }
  boolean movel(boolean shift) { moved = true;
    if (sel() && !shift) {
      if (ey==sy? ex>sx : ey>sy) allS(); else allE();
      return true;
    }
    if (ex == 0) {
      if (ey == 0) return false;
      ey--;
      ex = len(ey);
    } else if (ctrl) {
      Line l = lns.get(ey);
      boolean m = mode(l.ints[ex-1]);
      while (ex>0 && mode(l.ints[ex-1])==m) ex--;
    } else ex--;
    if (!shift) allE();
    return true;
  }
  boolean mode(int c) {
    return c>='a'&&c<='z' || c>='A'&&c<='Z';
  }
  void ldelete() { if (!editable) return; textChanged = true;
    if (!sel()) movel(true);
    order();
    if (sy==ey) {
      Line l = lns.get(sy);
      l.delete(sx, ex);
    } else {
      Line s = lns.get(sy); s.delete(sx, s.len);
      Line e = lns.get(ey); e.delete(0, ex);
      lns.subList(sy+1, ey+1).clear();
      s.append(e.ints);
    }
    allS();
  }
  void rdelete() {
    if (sel()) ldelete();
    else if (mover(true)) ldelete();
  }
  int len(int y) {
    return lns.get(y).len;
  }
  
  void append(String g) { if (!editable) return; textChanged = true;
    String[] gls = split(g, '\n');
    if (sel()) ldelete();
    
    if (gls.length == 1) {
      ex+= insert(ey, ex, gls[0]);
    } else {
      Line l = lns.get(sy);
      int[] tail = l.get(sx, l.len);
      l.delete(sx, l.len);
      l.append(gls[0]);
      for (int i = 1; i < gls.length-1; i++) lns.add(sy+i, new Line(gls[i]));
      Line e = new Line(gls[gls.length-1]);
      ex = e.len;
      e.append(tail);
      lns.add(sy+gls.length-1, e);
    }
    ey+= gls.length-1;
    allE();
  }
  void appendLns(String s) { textChanged = true;
    String[] ls = split(s, '\n');
    for (String c : ls) lns.add(new Line(c));
  }
  int insert(int y, int x, String s) { textChanged = true;
    Line l = lns.get(y);
    return l.insert(x, s);
  }
  void clear() {
    sx=ex=sy=ey=0; textChanged = true;
    lns.clear();
    lns.add(new Line(""));
  }
  void end() {
    ey = lns.size()-1;
    ex = len(ey);
    allE();
    dyoff = yoff = h-chrH*lns.size()-chrH;
  }
  
  String allText() {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < lns.size(); i++) {
      b.append(lns.get(i).toString());
      if (i != lns.size()-1) b.append('\n');
    }
    return b.toString();
  }
  String getsel() {
    if (sy == ey) {
      return tostr(lns.get(sy).get(Math.min(sx, ex), Math.max(sx, ex)));
    } else {
      boolean swp = swapped(); if (swp) swap();
      StringBuilder b = new StringBuilder();
      Line s = lns.get(sy);
      Line e = lns.get(ey);
      b.append(tostr(s.get(sx, s.len))).append('\n');
      for (int i = sy+1; i <= ey-1; i++) b.append(lns.get(i).toString()).append('\n');
      b.append(tostr(e.get(0, ex)));
      if (swp) swap();
      return b.toString();
    }
  }
  void special(String s) {
    if (s.equals("newline")) { textChanged = true;
      if (sel()) ldelete();
      Line l = lns.get(ey);
      int sa = 0;
      while (sa!=l.len && l.ints[sa]==32) sa++;
      int[] is = new int[sa];
      for (int i = 0; i < sa; i++) is[i] = 32;
      lns.add(ey+1, new Line(is));
      ey++; ex = sa;
      allE();
    } else if (s.equals("eval")) {
      eval();
    } else if (s.equals("left")) {
      movel(cshift());
    } else if (s.equals("right")) {
      mover(cshift());
    } else if (s.equals("up")) { moved = true;
      if (ey > 0) {
        ey--;
        ex = Math.min(ex, len(ey));
      } else ex = 0;
      if (!cshift()) allE();
    } else if (s.equals("down")) { moved = true;
      if (ey < lns.size()-1) {
        ey++;
        ex = Math.min(ex, len(ey));
      } else ex = len(ey);
      if (!cshift()) allE();
    } else if (s.equals("openPar")) {
      if (sel()) {
        String sel = getsel();
        ldelete();
        append("(");
        append(sel);
        append(")");
        movel(false);
      }
    } else if (s.equals("wrapPar")) {
      
    } else if (s.equals("undo")) {
      //hptr+= hsz-1;
      //hptr%= hsz;
      //to(history[hptr]);
      //modified = true;
    } else if (s.equals("redo")) {
      //hptr++;
      //hptr%= hsz;
      //to(history[hptr]);
      //modified = true;
    } else if (s.equals("paste")) {
      a.paste(this);
    } else if (s.equals("match")) {
      //int sel = hl.sel(fullPos());
      //if (sel != -1) to(sel);
    } else if (s.equals("home")) {
      Line l = lns.get(ey);
      if (ex==0) while (ex < l.len && l.ints[ex]==32) ex++;
      else { int i = 0;
        while (i!=l.len && l.ints[i]==32) i++;
        ex = i==ex? 0 : i;
      }
      if(ctrl) { ex=ey=0; }
      if(!cshift()) allE(); moved = true; // TODO short shift?
    } else if (s.equals("end")) {
      if(ctrl) ey=lns.size()-1; ex=len(ey);
      if(!cshift()) allE(); moved = true;
    } else if (s.equals("copy")) {
      if (!sel()) { sx = 0; ex = len(sy); }
      a.copy(getsel());
    } else if (s.equals("sall")) {
      sx=sy=0;
      ey=lns.size()-1; ex = len(ey);
    } else if (s.equals("pgdn") || s.equals("pgup")) { moved = true;
      int d = s.equals("pgdn")? 1 : -1;
      int ca = constrain(((int)(h/chrH)-3)*d+ey, 0, lns.size()-1)-ey;
      yoff-= ca*chrH; ey+= ca;
      ex = Math.min(ex, len(ey)); if (!shift) allE();
    } else if (s.equals("cut")) { if (!editable) { special("copy"); return; } textChanged = true;
      if (!sel()) {
        a.copy(lns.get(ey).toString()+"\n");
        lns.remove(ey);
        if (lns.size()==0) clear();
        else if (ey == lns.size()) ey--;
        ex = 0;
        allE();
      } else {
        special("copy");
        ldelete();
      }
    } else if (s.equals("")) {
    } else if (s.equals("")) {
    } else if (s.equals("")) {
    } else if (s.equals("")) {
    } else extraSpecial(s);
  }
  void extraSpecial(String s) {
    println("unknown special "+s);
  }
  void eval() {
  }
  void pasted(String s) {
    append(s);
  }
}

static class Line {
  int[] ints;
  int len;
  Line(String s) {
    ints = tocps(s);
    len = ints.length;
  }
  Line(int[] is) {
    ints = is;
    len = is.length;
  }
  String toString() {
    return tostr(ints);
  }
  void delete(int sx, int ex) {
    int[] n = new int[len-(ex-sx)];
    System.arraycopy(ints, 0, n, 0, sx);
    System.arraycopy(ints, ex, n, sx, len-ex);
    ints = n;
    len = n.length;
  }
  int insert(int x, String s) {
    int[] is = tocps(s);
    int[] n = new int[len+is.length];
    System.arraycopy(ints, 0, n, 0, x);
    System.arraycopy(is  , 0, n, x, is.length);
    System.arraycopy(ints, x, n, x+is.length, len-x);
    ints = n;
    len = ints.length;
    return is.length;
  }
  int[] get(int s, int e) {
    return Arrays.copyOfRange(ints, s, e);
  }
  void append(String s) { append(tocps(s)); }
  void append(int[] i) {
    int olen = len;
    ints = Arrays.copyOf(ints, len+i.length);
    System.arraycopy(i, 0, ints, olen, i.length);
    len = ints.length;
  }
  int UTF16before(int x) {
    int r = 0;
    for (int i = 0; i < x; i++) r+= Character.charCount(ints[i]);
    return r;
  }
}
static int[] tocps(String s) {
  int[] is = new int[s.length()];
  int rp = 0;
  int i = 0;
  while (i < is.length) {
    int cp = s.codePointAt(i);
    is[rp++] = cp;
    i+= Character.charCount(cp);
  }
  return Arrays.copyOf(is, rp);
}
static String tostr(int[] ints) {
  StringBuilder b = new StringBuilder();
  for (int c : ints) b.appendCodePoint(c);
  return b.toString();
}
