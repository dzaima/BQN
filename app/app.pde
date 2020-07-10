import java.text.DecimalFormat;
import java.io.PrintStream;
import java.util.Scanner;
import java.nio.charset.StandardCharsets;
import java.net.URLConnection;
import java.net.URL;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;


static PGraphics d;
static app a;


static ArrayList<Drawable> screen;

static TextReciever textInput;
static Keyboard kb;

static TopBar topbar;
static int top = 30;
static int isz = 30;
static int scale;
static int freey() { // y position of keyboard start
  return a.height-kb.h;
}
//static StrOS os;
static REPL mainREPL;



void setup() {
  d = g;
  a = this;
  psSetup();
  if (screen==null) screen = new ArrayList();
  background(#0a0a0a);
  textFont(createFont("BQN386.ttf", 48));
  int max = max(displayWidth, displayHeight);
  scale = (int)(max/(MOBILE? 50 : 86));
  println(scale);
  top = MOBILE? scale*3/2 : scale;
  
  
  newKb();
  if (topbar==null) { // don't reset variables if orientation has changed
    //os = new StrOS();
    topbar = new TopBar(0, 0, width, top);
    topbar.toNew(mainREPL = new REPL());
    topbar.show();
  }
  redrawAll();
}
static boolean redraw;
void newKb() {
  if (MOBILE) {
    if (width>height) keyboard(0, 0, width, width/3, "L.json");
    else              keyboard(0, 0, width, (int)(width*.8), "P.json");
  } else keyboard(0, 0, 0, 0, "L.json");
}
static void redrawAll() {
  //if (width != w || h != height) surface.setSize(w, h);
  redraw = true;
  a.newKb();
  topbar.resize(d.width, top);
}
static boolean pmousePressed;
static int smouseX, smouseY;
static int mouseStart;
void draw() {
  if (!pmousePressed && mousePressed) {
    smouseX = mouseX;
    smouseY = mouseY;
    mouseStart = millis();
  }
  psDraw();
  
  for (int i = screen.size()-1; i >= 0; i--) {
    screen.get(i).tick();
  }
  if (redraw) {
    background(#101010);
    for (Drawable d : screen) {
      d.redraw();
    }
    redraw = false;
  }
  //String s = os.get();
  //if (s.length() != 0) {
  //  if (mainREPL != null) mainREPL.historyView.appendLns(s);
  //}
  pmousePressed = mousePressed;
}
static boolean shift, ctrl;
void handleExtraJAVA2D(boolean pressed) {
  if (key==65535) {
    if (keyCode == 16) shift = pressed;
    if (keyCode == 17) ctrl  = pressed;
  }
}
void keyPressed(KeyEvent e) {
  e = fixKE(e);
  if (sketchRenderer().equals(JAVA2D) && !MOBILE) {
    handleExtraJAVA2D(true);
  } else {
    shift = e.isShiftDown();
    ctrl  = e.isControlDown();
  }
  //if (key == 'Q') {
  //  surface.setSize(height, width);
  //  redrawAll();
  //}
  //println("P", +key, keyCode, shift, ctrl, e.getNative());
  //println("P", e.getNative());
  if (key == 18 && keyCode == 82) {
    redrawAll();
    return;
  }
  if (textInput != null) {
    if (key == 65535) {
           if (keyCode == java.awt.event.KeyEvent.VK_PAGE_UP   && ctrl) topbar.move(-1);
      else if (keyCode == java.awt.event.KeyEvent.VK_PAGE_DOWN && ctrl) topbar.move( 1);
      else handleCoded(keyCode);
    } else {
      if (ctrl && key>='0' && key<='9') {
        int i = Math.min(key=='0'?9:key-'1', topbar.tabs.size()-1);
        topbar.to(topbar.tabs.get(i));
      }
      else if (key == 8) textInput.ldelete();
      else if (key ==  26 && keyCode ==  90) textInput.special("undo");
      else if (key ==  25 && keyCode ==  89) textInput.special("redo");
      else if (key ==   3 && keyCode ==  67) textInput.special("copy");
      else if (key ==  22 && keyCode ==  86) textInput.special("paste");
      else if (key ==  24 && keyCode ==  88) textInput.special("cut");
      else if (key == 127 && keyCode == 127) textInput.rdelete();
      else if (key ==  19 && keyCode ==  83) textInput.special("eval");
      else if (key ==   1 && keyCode ==  65) textInput.special("sall");
      else if (key ==  23 && keyCode ==  87) textInput.special("close");
      else if (key == 10) textInput.special("newline");
      else if (key >= 54589 && key <= 54633) { // double-strucks
        textInput.append(new String(new char[]{55349, (char)(key+2048)})); // yay…
      }
      else textInput.append(Character.toString(key));
    }
  }
  //println(+key, keyCode);
}
void keyReleased(KeyEvent e) {
  if (sketchRenderer().equals(JAVA2D) && !MOBILE) {
    handleExtraJAVA2D(false);
  } else {
    shift = e.isShiftDown();
    ctrl  = e.isControlDown();
  }
}

static boolean shift() {
  return shift || (textInput!=null? kb.shiftMode>0 : false);
}
static boolean cshift() {
  boolean r = shift || (kb!=null? kb.shiftMode>0 : false);
  if (kb!=null && kb.shiftMode>0) kb.shiftMode = 2;
  return r;
}
//static void textS(PGraphics g, char s, float x, float y) {
//  g.text(s, x, y + (MOBILE? g.textSize*.333 : 0));
//}