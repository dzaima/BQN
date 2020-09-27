abstract static class Plane extends Drawable {
  
  double fullX;
  double fullY;
  int freq = 20;
  double fullS;
  double lastDist; // ==0 => first multitouch frame
  double lastX, lastY;
  Plane() {
    fullS = 10;
    fullX = -100;
    fullY = -100;
  }
  
  abstract void drawG();
  
  boolean init;
  void redraw() {
    if (!init) {
      fullX = (x+w/2) / -fullS;
      fullY = (y+h/2) / -fullS;
      init = true;
    }
  }
  
  final void draw() {
    if (smouseY > y && smouseY < y+h) {
      if (a.touches.length == 2) {
        float ow = dist(a.touches[0].x, a.touches[0].y, a.touches[1].x, a.touches[1].y);
        double sc = ow/lastDist;
        double avgX = (a.touches[0].x + a.touches[1].x) / 2f;
        double avgY = (a.touches[0].y + a.touches[1].y) / 2f;
        if (lastDist != 0) {
          double pS = fullS;
          fullS*= sc;
          double scalechange = 1/fullS - 1/pS;
          fullX-= (avgX * scalechange);
          fullY-= (avgY * scalechange);
          fullX+= (lastX-avgX)/fullS;
          fullY+= (lastY-avgY)/fullS;
        }
        lastX = avgX;
        lastY = avgY;
        lastDist = ow;
      } else {
        lastDist = 0;
        if (a.touches.length == 1) {
          fullX += (a.pmouseX - a.mouseX)/fullS;
          fullY += (a.pmouseY - a.mouseY)/fullS;
        }
      }
    } else lastDist = 0;
    d.pushMatrix();
    beginClip(d, x, y, w, h);
    d.imageMode(CORNER);
    d.background(12);
    
    d.stroke(0xff666666);
    d.strokeWeight(1);
    
    
      
    double sx = fullX;
    double sy = fullY;
    
    double ex = fullX + a.width/fullS;
    double ey = fullY + a.height/fullS;
    double rsz = Math.log((ex-sx)/freq)/Math.log(10);
    double s1 = Math.pow(10, Math.floor(rsz));
    if (s1 == 0) { // prevent an infinite loop due to too much lost precision
      fullS/= 1.1;
      d.popMatrix();
      return;
    }
    double s2 = s1;
    double m1 = rsz % 1;
    if (m1 < 0) m1+= 1;
    if (m1 > .6)      { s1*= 5; s2*= 20; }
    else if (m1 > .3) { s1*= 2; s2*= 10; }
    else              { s1*= 1; s2*=  5; }
    
    d.textAlign(LEFT, BOTTOM);
    d.fill(0xffd2d2d2);
    float ts = max(d.width, d.height)/70f;
    d.textSize(ts);
    
    DecimalFormat df = new DecimalFormat("#.0");
    int dgs = (int) Math.ceil(Math.log(1/s2)/Math.log(10));
    df.setMaximumFractionDigits(dgs);
    df.setMinimumFractionDigits(dgs);
    
    float off = ts*.4;
    float ty = constrain(realY(0), off+y+ts, y+h-off);
    d.textAlign(LEFT, BOTTOM);
    for (double cx = Math.floor(sx/s1) * s1; cx < ex; cx+= s1) {
      if (cx + s1 == cx) { // infinite loop ↑
        fullS/= 1.1;
        d.popMatrix();
        return;
      }
      
      boolean big = Math.abs(mod(cx, s2)/s2-.5)>0.49;
      d.stroke(big? 0xff666666 : 0x40666666);
      int rx = realX(cx);
      d.line(rx, y, rx, y+h);
      if (big) d.text(df.format(cx).replaceAll("^-?\\.?0+$", "0"), rx, ty);
    }
    
    float tx = constrain(realX(0), x+off,
      x+w-off-max(
        d.textWidth(df.format(Math.floor(sy/s1) * s1)),
        d.textWidth(df.format(ey))
      )
    );
    
    for (double cy = Math.floor(sy/s1) * s1; cy < ey; cy+= s1) {
      if (cy + s1 == cy) { // infinite loop ↑
        fullS/= 1.1;
        d.popMatrix();
        return;
      }
      boolean big = Math.abs(mod(cy, s2)/s2-.5)>0.49;
      d.stroke(big? 0xff666666 : 0x40666666);
      int ry = realY(cy);
      d.line(x, ry, x+w, ry);
      if (big) d.text(df.format(-cy).replaceAll("^-?\\.?0+$", "0"), tx, ry);
    }
    
    drawG();
    
    endClip(d);
    d.popMatrix();
  }
  
  //translate((int)(-fullX * fullS), (int)(-fullY * fullS));
  //scale((float)fullS);
  int realX(double x) {
    return (int) ((x - fullX)*fullS);
  }
  int realY(double y) {
    return (int) ((y - fullY)*fullS);
  }
}

static double mod(double a, double b) {
  a%= b;
  if (a < 0) a+= b;
  return a;
}

static class Graph extends Plane {
  LL<Point> points = new LL<Point>();
  PQ<Double, Point> pq = new PQ<Double, Point>();
  double[] b;
  Value fn;
  int pts = 1000;
  float ph = 3;
  int mul = 1;
  boolean joined = true;
  
  
  
  void newFun(Value fn) {
    this.fn = fn;
    points.clear();
    pq.clear();
    bounds();
    add(b[0], points.start);
    add(b[1], points.last());
  }
  
  class Line {
    ArrayList<Double> ptsx = new ArrayList<Double>();
    ArrayList<Double> ptsy = new ArrayList<Double>();
    void add(double x, double y) {
      ptsx.add(x);
      ptsy.add(y);
    }
    void draw() {
      boolean drawing = false;
      int len = ptsx.size();
      for (int i = 0; i < len; i++) {
        double x = ptsx.get(i);
        double y = ptsy.get(i);
        if (Double.isNaN(y)) {
          if (drawing) {
            drawing = false;
            d.endShape();
          }
        } else if (y == Double.POSITIVE_INFINITY) {
          if (drawing) {
            d.vertex(realX(x), realY(fullY));
            drawing = false;
            d.endShape();
          }
        } else if (y==Double.NEGATIVE_INFINITY) {
          if (drawing) {
            d.vertex(realX(x), realY(fullY + d.height/fullS));
            drawing = false;
            d.endShape();
          }
        } else {
          if (!drawing) {
            drawing = true;
            d.beginShape();
          }
          d.vertex(realX(x), realY(-y));
        }
      }
      if (drawing) d.endShape();
    }
  }
  
  void drawG() {
    bounds();
    double sCut = b[0]-b[2];
    double eCut = b[1]+b[2];
    double sEnd = b[0]+b[2];
    double eSrt = b[1]-b[2];
    LLNode<Point> n = points.first();
    while (n != points.end) {
      if (n.v.x < sCut) remove(n);
      else break;
      n = n.next;
    }
    boolean sInR = n != points.end && n.v.x < sEnd; // start in range
    n = points.last();
    while (n != points.start) {
      if (n.v.x > eCut) remove(n);
      else break;
      n = n.prev;
    }
    boolean eInR = n != points.start && n.v.x > eSrt; 
    
    if (!sInR) add(b[0], points.start);
    if (!eInR) add(b[1], points.last());
    long nt = System.nanoTime();
    int ptsadded = 0; // for debugging, uncommented
    while (pq.size() > 0) {
      PQNode<Double, Point> bg = pq.biggest();
      if (bg.m > b[2]) {
        Point p = bg.v;
        if (mul > 1) {
          
          ArrayList<Point> ps = new ArrayList<Point>();
          ps.add(split((p.x + p.pnode.next.v.x)/2, p.pnode, null));
          while (pq.size() > 0 && ps.size() < mul) {
            bg = pq.biggest();
            if (bg.m <= b[2]) break;
            p = bg.v;
            ps.add(split((p.x + p.pnode.next.v.x)/2, p.pnode, null));
          }
          double[] ds = new double[ps.size()];
          for (int i = 0; i < ds.length; i++) {
            p = ps.get(i);
            ds[i] = p.x;
          }
          Value res;
          try {
            res = (Value) fn.call(new DoubleArr(ds));
          } catch (Throwable e) { res = null; }
          
          for (int i = 0; i < ds.length; i++) {
            ps.get(i).y = new double[0];
            if (res != null) try {
              ps.get(i).y = res.get(i).asDoubleArr();
            } catch (Throwable t) { /*ignore */ }
          }
          ptsadded+= ds.length;
          
          
          
        } else {
          add((p.x + p.pnode.next.v.x)/2,  p.pnode);
          ptsadded++;
        }
      } else break;
      if (System.nanoTime()-nt > 5E6) break;
    }
    //println(points.size, ptsadded, ptsadded*1f/(System.nanoTime()-nt)*1E9, frameRate);
    
    while (pq.size() > 0) {
      PQNode<Double, Point> sm = pq.smallest(); // can't be PQ<Double, Point>.Item because Processing :|
      if ((Double) sm.m < b[2]/4) {
        remove(sm.v.pnode);
      } else break;
    }
    
    d.noFill();
    d.stroke(0xffd2d2d2);
    d.strokeWeight(ph);
    n = points.first();
    if (joined && n != points.end && n.next != points.end) {
      ArrayList<Line> lns = new ArrayList<Line>();
      n = n.next;
      while (n != points.end) {
        LLNode<Point> p = n.prev;
        double[] na = n.v.y;
        if (lns.size() == na.length) {
          for (int i = 0; i < na.length; i++) {
            lns.get(i).add(n.v.x, na[i]);
          }
        } else {
          for(Line l : lns) l.draw();
          lns.clear();
          for(double y : na) {
            Line ln = new Line();
            ln.add(n.v.x, y);
            lns.add(ln);
          }
        }
        n = n.next;
      }
      for(Line l : lns) l.draw();
    } else {
      d.fill(0xffd2d2d2);
      d.noStroke();
      //beginShape(POINTS);
      //strokeWeight(ph);
      while (n != points.end) {
        //for (double y : n.v.y) d.vertex ((float)(n.v.x), -(float)(y));
          for (double y : n.v.y) d.ellipse(realX(n.v.x), realY(-y), ph, ph);
        n = n.next;
      }
      //endShape();
    }
  }
  
  void add(double pos, LLNode<Point> l) {
    double[] res;
    try {
      res = ((Value) fn.call(new Num(pos))).asDoubleArr();
    } catch (Throwable e) {
      res = new double[0];
    }
    split(pos, l, res);
  }
  
  Point split(double pos, LLNode<Point> l, double[] res) {
    Point p = new Point(pos, res);
    LLNode<Point> r = l.next;
    LLNode<Point> c = l.addAfter(p);
    p.pnode = c;
    if (l != points.start) {
      if (l.v.pqr != null) {
        l.v.pqr.remove();
        l.v.pqr = null;
      }
      addPQ(l, c);
    }
    if (r != points.end) addPQ(c, r);
    return p;
  }
  void addPQ(LLNode<Point> l, LLNode<Point> r) {
    double d = r.v.x - l.v.x;
    l.v.pqr = pq.add(d, l.v);
  }
  void remove(LLNode<Point> n) {
    n.remove();
    LLNode<Point> l = n.prev;
    LLNode<Point> r = l.next;
    if (n.v.pqr != null) { n.v.pqr.remove(); n.v.pqr = null; }
    if (r == points.end && l.v.pqr != null) {
      l.v.pqr.remove();
      l.v.pqr=null;
    }
    if (l != points.start && r != points.end) {
      l.v.pqr.remove();
      addPQ(l, r);
    }
  }
  
  
  
  void bounds() {
    b = new double[] {
      fullX, // starting visible x
      fullX + d.width/fullS, // ending visible x
      (d.width/fullS) / pts,
    };
  }
  
  
  class LL<T> {
    LLNode<T> start = new SNode<T>(this);
    LLNode<T> end = new ENode<T>(this);
    
    LL() {
      start.next = end;
      end.prev = start;
    }
    
    int size = 0;
    
    void addLast(T v) {
      end.addBefore(v);
    }
    
    void addFirst(T v) {
      start.addAfter(v);
    }
    
    void clear() {
      start.next = end;
      end.prev = start;
      size = 0;
    }
    LLNode<T> last() {
      return end.prev;
    }
    LLNode<T> first() {
      return start.next;
    }
  }
  class SNode<T> extends LLNode<T> {
    SNode(LL<T> ll) {
      super(ll, null, null, null);
    }
    void addBefore() { throw new IllegalStateException("adding before starting element"); }
  }
  class ENode<T> extends LLNode<T> {
    ENode(LL<T> ll) {
      super(ll, null, null, null);
    }
    void addAfter() { throw new IllegalStateException("adding after ending element"); }
  }
  class LLNode<T> {
    LLNode<T> prev;
    LLNode<T> next;
    LL<T> ll;
    T v;
    LLNode (LL<T> ll, LLNode<T> p, LLNode<T> n, T v) {
      prev = p;
      next = n;
      this.ll = ll;
      this.v = v;
    }
    LLNode<T> addAfter(T v) {// println(this, next, v);
      LLNode<T> n = new LLNode<T>(ll, this, next, v);
      next.prev = n;
      next = n;
      ll.size++;
      return n;
    }
    LLNode<T> addBefore(T v) {
      LLNode<T> n = new LLNode<T>(ll, prev, this, v);
      prev.next = n;
      prev = n;
      ll.size++;
      return n;
    }
    void remove() {
      prev.next = next;
      next.prev = prev;
      ll.size--;
      if(rmd)throw null;rmd=true;
    }
    boolean rmd;
  }
  
  class Point {
    double x;
    double[] y;
    LLNode<Point> pnode;
    PQNode pqr;
    Point (double x, double[] y) {
      this.x = x;
      this.y = y;
    }
    String toString() {
      return x+"";
    }
  }
  
  void mouseWheel(int dir) {
    double sc = Math.pow(.8, dir);
    double pS = fullS;
    fullS*= sc;
    double scalechange = 1/fullS - 1/pS;
    fullX-= (a.mouseX * scalechange);
    fullY-= (a.mouseY * scalechange);
  }

}
