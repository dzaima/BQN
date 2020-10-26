import java.util.concurrent.atomic.AtomicBoolean;
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

static TabSelect all;

static TextReciever textInput;
static Keyboard kb;

static TopBar topbar;
static int scale;
static REPL mainREPL;

static int isz;


void setup() {
  d = g;
  a = this;
  psSetup();
  background(#0a0a0a);
  textFont(createFont("BQN386.ttf", 48));
  int max = max(displayWidth, displayHeight);
  scale = (int)(max/(MOBILE? 50 : 86));
  println("scale: "+scale);
  
  isz = ceil(scale/.8);
  
  newKb();
  if (all==null) { // don't reset variables if orientation has changed
    all = new TabSelect();
    topbar = all.tb;
    topbar.toNew(mainREPL = new REPL());
  }
  redrawAll();
}

void mousePressed (MouseEvent e) { all.mouseEvent(e, true ); }
void mouseReleased(MouseEvent e) { all.mouseEvent(e, false); }

static boolean redraw;
void newKb() {
  if (MOBILE) {
    if (width>height) keyboard(width, width/3, "L.json");
    else              keyboard(width, (int)(width*.8), "P.json");
  } else keyboard(0, 0, "L.json");
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
  if (!focused) shift=ctrl=false;
  all.draw();
  
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
  println(+key,keyCode);
  if (key==5 && keyCode==69) {
    println("clearing..");
    background(0);
    return;
  }
  if (key==4 && keyCode==68) {
    println("redrawing..");
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
      else if (key ==  19 && keyCode ==  83) textInput.special("save");
      else if (key ==  18 && keyCode ==  82) textInput.special("eval");
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

static void redrawAll() {
  all.upd(0, 0, a.width, a.height);
}



static String toPrintable(Value v) { // TODO make part of BQN
  if (v.r()<=1) return v.asString();
  if (v.r()==2) {
    for (Value c : v) if (!(c instanceof Char)) throw new DomainError("Expected all-char array");
    return v.toString();
  }
  throw new DomainError("stringifying "+v);
}
static String readFile(String name) {
  return new String(a.loadBytes(name), StandardCharsets.UTF_8);
}
