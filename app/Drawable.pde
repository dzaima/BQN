abstract static class Drawable {
  int x, y;
  int w, h;
  boolean visible;
  Drawable() { x=y=-100; w=h=100; }
  abstract void draw(); // called on every frame; won't be called when not visible
  void setVisible(boolean v) { this.visible = v; }  // will be followed by a redraw() call if visible
  abstract void redraw(); // be careful on calling!
  final void upd(int x, int y, int w, int h) { // update everything; won't be called when not visible
    this.x = x; this.y = y;
    this.w = w; this.h = h;
    redraw();
  }
  
  void mouseWheel(int dir) { }
  boolean mouseInMe() {
    return a.mouseX > x && a.mouseY > y && a.mouseX < x+w && a.mouseY < y+h;
  }
  boolean smouseIn() {
    return smouseX > x && smouseY > y && smouseX < x+w && smouseY < y+h;
  }
  void pasted(String s) {
    throw new Error(this+" didn't ask for a clipboard!");
  }
}
