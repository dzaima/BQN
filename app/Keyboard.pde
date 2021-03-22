static class Keyboard extends Drawable {
  int xam, yam;
  int kw, kh; // key width/height
  JSONObject data;
  int[] cols;
  int defcol;
  int shiftMode; // 0 = none, 1 = temp, 2 = hold;
  Key[][] keys;
  String layout;
  Keyboard(int w, int h, int xam, int yam, JSONObject data) {
    this.w = w; // TODO not need this
    this.h = h;
    this.data = data;
    this.xam = xam;
    this.yam = yam;
    this.kw = w/xam;
    this.kh = h/yam;
    keys = new Key[yam][xam];
    for (int cy = 0; cy < yam; cy++) for (int cx = 0; cx < xam; cx++) keys[cy][cx] = new Key(cx, cy, this);
    JSONArray colsA = data.getJSONArray("colors");
    cols = new int[colsA.size()];
    for(int i = 0; i < cols.length; i++) {
      cols[i] = Integer.parseUnsignedInt(colsA.getString(i), 16);
    }
    defcol = cols[data.getInt("defcol")];
    loadLayout(data.getString("mainName"));
  }
  void loadLayout(String name) {
    layout = name;
    JSONArray arr = data.getJSONArray(name);
    for (int y = 0; y < yam; y++) {
      JSONArray row = arr.getJSONArray(y);
      for (int x = 0; x < xam; x++) {
        keys[y][x].load(row.getJSONObject(x));
      }
    }
    if (layoutUpdate != null) layoutUpdate.call(new ChrArr(data.getString("fullName")), new ChrArr(name));
    redraw();
  }
  void redraw() {
    if (w==0||h==0) return;
    for (Key[] row : keys) for (Key k : row) k.redraw();
  }
  
  Key start;
  
  void draw() {
    if (w==0||h==0) return;
    if (!pmousePressed && a.mousePressed && smouseIn()) {
      int mx = (a.mouseX-x) / kw;
      int my = (a.mouseY-y) / kh;
      if (mx >= 0 && my >= 0 && mx < xam && my < yam) {
        start = keys[my][mx];
        start.redraw();
      }
    }
    if (pmousePressed && !a.mousePressed && start != null) {
      Action a = findAction();
      Key t = start;
      start = null;
      t.redraw();
      if (a != null) {
        if (actionCalled != null) if (actionCalled.call(new HArr(new Value[]{new Num(t.x), new Num(t.y), new Num(actionId()), new ChrArr(kb.layout)})).equals(new ChrArr("stop"))) return;
        a.call();
      }
    }
    if (start != null) {
      Action at = findAction();
      if (at != null) {
        at.k.redraw(at);
        int time = a.millis() - mouseStart;
        if (time > 200) {
          if (at.rep!=-1 && a.frameCount%at.rep == 0) at.call();
        }
      }
    }
  }
  Action findAction() {
    return start.actions[actionId()];
  }
  int actionId() {
    if (dist(a.mouseX, a.mouseY, smouseX, smouseY) > kh/3) { // gesture
      int dx = a.mouseX - smouseX;
      int dy = a.mouseY - smouseY;
      if (Math.abs(dx) > Math.abs(dy)) {
        if (dx > 0) return 4;
        else        return 3;
      } else {
        if (dy > 0) return 2;
        else        return 1;
      }
    } else return 0;
  }
}

void keyboard(int w, int h, String file) {
  JSONObject o = loadJSONObject(file);
  JSONArray main = o.getJSONArray(o.getString("mainName"));
  kb = new Keyboard(w, h, main.getJSONArray(0).size(), main.size(), o);
}

static final String[] dirs = new String[]{"def", "up", "down", "left", "right"};
static final float[][] offsets = {
  {.5, .5 }, // center
  {.5, .2 }, // up
  {.5, .87}, // down
  {.2, .5 }, // left
  {.8, .5 }, // right
};
static final float[][] corners = {
  null,
  {0, 0, 1, 0},
  {0, 1, 1, 1},
  {0, 0, 0, 1},
  {1, 0, 1, 1},
};

static class Key {
  Keyboard b;
  
  int col = #222222;
  Action[] actions = new Action[5]; // C U D L R
  int x, y, w, h;
  Key(int x, int y, Keyboard b) {
    this.b = b;
    this.x = x;
    this.y = y;
    this.w = b.kw;
    this.h = b.kh;
  }
  
  void redraw() {
    redraw(null);
  }
  
  void redraw(Action hl) { // highlight
    if (w==0 || h==0) return;
    d.rectMode(CORNER);
    d.fill(b.start==this && hl==actions[0]? d.lerpColor(col, #aaaaaa, .1) : col);
    d.noStroke();
    int px = b.x + x*w;
    int py = b.y + y*h;
    d.rect(px, py, w, h);
    if (hl != null) {
      for(int i = 1; i < 5; i++) {
        Action a = actions[i];
        if (a == hl) {
          d.fill(d.lerpColor(col, #aaaaaa, .1));
          d.triangle(px+w/2, py+h/2, px + w*corners[i][0], py + h*corners[i][1], px + w*corners[i][2], py + h*corners[i][3]);
          break;
        }
      }
    }
    for(int i = 0; i < 5; i++) {
      Action a = actions[i];
      float[] offs = offsets[i];
      if (a == null) continue;
      String t = a.chr;
      if (b.shiftMode!=0 && t.length()==1) t = t.toUpperCase();
      d.fill(255);
      d.textAlign(CENTER, CENTER);
      float yoff = 0;
      float sz = i==0? h*.4f : h*.16f;
      d.textSize(sz);
      yoff+= sz*-.13f;
      
      textS(d, t, px + w*offs[0], py + h*offs[1] + yoff);
    }
  }
  void load(JSONObject o) {
    for (int i = 0; i < 5; i++) {
      JSONObject c = o.getJSONObject(dirs[i]);
      if (c == null) actions[i] = null;
      else actions[i] = new Action(c, b, this);
    }
    col = o.hasKey("col")? b.cols[o.getInt("col")] : b.defcol;
  }
}


static class Action {
  final String chr, spec, type, gotof;
  final int rep;
  final Keyboard b;
  final Key k;
  Action (JSONObject o, Keyboard b, Key k) {
    chr = o.getString("chr");
    String type = o.getString("type");
    if (type == null) this.type = chr;
    else this.type = type;
    
    spec = o.getString("spec");
    gotof = o.getString("goto");
    rep = o.hasKey("rep")? o.getInt("rep") : -1;
    this.b = b;
    this.k = k;
  }
  void call() {
    ctrl = shift = false;
    if (textInput == null) return;
    if (spec == null) {
      String toType = type;
      if (b.shiftMode != 0) {
        if (type.length() == 1) toType = toType.toUpperCase();
        if (b.shiftMode == 1) {
          b.shiftMode = 0;
          b.redraw();
        }
      }
      textInput.append(toType);
      return;
    }
    if (gotof != null) {
      b.loadLayout(gotof);
    }
    switch(spec) {
      case "none": return;
      case "del": textInput.ldelete(); return;
      case "rdel": textInput.rdelete(); return;
      case "clear": textInput.clear(); return;
      case "vkb": a.openKeyboard(); return;
      case "shift": 
        b.shiftMode++;
        if (b.shiftMode > 2) b.shiftMode = 0;
        b.redraw();
        return;
    }
    textInput.special(spec);
    //println("unknown type "+spec);
  }
}
