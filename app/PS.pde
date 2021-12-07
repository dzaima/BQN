//* PC
import java.awt.Toolkit;
static boolean MOBILE = false;
class FakeTouch { // for interoperability with android mode
  int x, y;
  FakeTouch(int x, int y) { this.x = x; this.y = y; }
}
FakeTouch[] touches = new FakeTouch[0];
static int[] touchIs(PApplet a) {
  return a.mousePressed? new int[]{a.mouseX, a.mouseY} : EmptyArr.NOINTS;
}
void settings() {
  //size(540, 830);
  //size(1200, 800, JAVA2D);
  size(960, 540);
}
int pw, ph;
void psSetup() {
  surface.setResizable(true);
}
void psDraw() {
  if (mousePressed) touches = new FakeTouch[] { new FakeTouch(mouseX, mouseY) };
  else touches = new FakeTouch[0];
  if (pw!=width || ph!=height) {
    redrawAll();
    pw = width;
    ph = height;
  }
}

import java.awt.datatransfer.*;
import java.awt.Toolkit;
void copy(String s) {
  StringSelection stringSelection = new StringSelection(s);
  Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
  clipboard.setContents(stringSelection, null);
}

void paste(Drawable d) {
  try {
    d.pasted((String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor));
  } catch (Throwable e) {
    e.printStackTrace();
  }
}
void mouseWheel(MouseEvent e) {
  if (all!=null && all.ctab!=null) all.ctab.vw.mouseWheel(e.getCount());
}
void openKeyboard() {};

void handleCoded(int keyCode) {
       if (keyCode == java.awt.event.KeyEvent.VK_BACK_SPACE) textInput.ldelete();
  else if (keyCode == java.awt.event.KeyEvent.VK_UP        ) textInput.special("up");
  else if (keyCode == java.awt.event.KeyEvent.VK_LEFT      ) textInput.special("left");
  else if (keyCode == java.awt.event.KeyEvent.VK_DOWN      ) textInput.special("down");
  else if (keyCode == java.awt.event.KeyEvent.VK_RIGHT     ) textInput.special("right");
  else if (keyCode == java.awt.event.KeyEvent.VK_HOME      ) textInput.special("home");
  else if (keyCode == java.awt.event.KeyEvent.VK_END       ) textInput.special("end");
       if (keyCode == java.awt.event.KeyEvent.VK_PAGE_UP   ) textInput.special("pgup");
  else if (keyCode == java.awt.event.KeyEvent.VK_PAGE_DOWN ) textInput.special("pgdn");
}
KeyEvent fixKE(KeyEvent e) {
  return e;
}

static void beginClip(PGraphics g, float x, float y, float x2, float y2) {
  g.clip(x, y, x2, y2);
}
static void endClip(PGraphics g) {
  g.noClip();
}


static class PWindow extends PApplet {
  int tw, th;
  boolean smooth;
  P5Impl tu;
  WP5Disp td;
  void create(int[] sz, boolean smooth, P5Impl u, WP5Disp d) {
    if (sz.length==2) {
      tw = sz[0];
      th = sz[1];
    } else tw=-1;
    tu = u;
    td = d;
    this.smooth = smooth;
    PApplet.runSketch(new String[]{getClass().getName()}, this);
  }
  void settings() {
    if (tw==-1) fullScreen();
    else size(tw, th);
    if (!smooth) noSmooth();
  }
  void setup() {
    tu.setup(g);
    surface.setResizable(true); // TODO configurable
  }
  boolean actuallyExited;
  int pw=-1,ph=-2;
  void draw() {
    if(pw!=width || ph!=height) {
      tu.resized(g, width, height);
      pw = width;
      ph = height;
    }
    if (finished) return;
    tu.draw(g);
  }
  
  void exit() { // weird roundabout setup of exiting so that there's no case of exiting twice breaking things
    finished = true;
    td.stop();
  }
  void close() {
    finished = true;
    exitCalled = true;
  }
  void exitActual() {
    tu.closed();
    if(surface instanceof processing.awt.PSurfaceAWT) {
      ((javax.swing.JFrame)((processing.awt.PSurfaceAWT.SmoothCanvas)((processing.awt.PSurfaceAWT)surface).getNative()).getFrame()).dispose(); // when this finally breaks, feel free to delete :)
    }
  }
  void mousePressed (MouseEvent e) { tu.mouseEvent(e, true ); }
  void mouseReleased(MouseEvent e) { tu.mouseEvent(e, false); }
  
  void keyPressed (KeyEvent e) { tu.keyEvent(e, key, true); keyCode=key=0; } // bad processing and your tendency to close the sketch :|
  void keyReleased(KeyEvent e) { tu.keyEvent(e, key, false); }
}
static void selectOutput2(String prompt, String callbackName, File f, Object callbackObj) {
  a.selectOutput(prompt, callbackName, f, callbackObj);
}

static int fixMouseButton(MouseEvent e, int mb) {
  Object n = e.getNative();
  if (n instanceof java.awt.event.MouseEvent) {
    int b = ((java.awt.event.MouseEvent) n).getButton();
    if      (b==java.awt.event.MouseEvent.BUTTON1) mb = LEFT;
    else if (b==java.awt.event.MouseEvent.BUTTON2) mb = CENTER;
    else if (b==java.awt.event.MouseEvent.BUTTON3) mb = RIGHT;
  }
  return mb;
}

static Value modArray(KeyEvent e) {
  if (e.getNative() instanceof java.awt.event.KeyEvent) {
    java.awt.event.KeyEvent ne = (java.awt.event.KeyEvent) e.getNative();
    return boolarr(ne.isControlDown(), ne.isShiftDown(), ne.isAltDown(), ne.isAltGraphDown(), ne.isMetaDown());
  }
  return boolarr(false, false, false, false, false);
}

static String getKeyCodeText(int code) {
  return java.awt.event.KeyEvent.getKeyText(code);
}
      
/*/ // ANDROID

static void selectOutput2(String prompt, String callbackName, File f, Object callbackObj) { }

import android.content.ClipboardManager;
import android.content.*;
import android.app.*;

static boolean MOBILE = true;

String gottenClip;
Drawable clipRec;

void settings() {
  //fullScreen();
  size(displayWidth, displayHeight);
}
void psSetup() {
  orientation(PORTRAIT);
}
void psDraw() {
  if (gottenClip != null) {
    clipRec.pasted(gottenClip);
    gottenClip = null;
  }
}
static int[] touchIs(PApplet a) {
  int[] is = new int[a.touches.length*2];
  for (int i = 0; i < a.touches.length; i++) {
    is[i*2  ] = (int)a.touches[i].x;
    is[i*2+1] = (int)a.touches[i].y;
  }
  return is;
}

void prepareClip() {
  if (cba == null) {
    cba = getActivity();
    cbcm = (ClipboardManager) cba.getSystemService(Context.CLIPBOARD_SERVICE);
  }
}

Activity cba;
ClipboardManager cbcm;


void copy(final String s) {
  getActivity().runOnUiThread(new Runnable() {
    public void run() {
      prepareClip();
      ClipData clip = android.content.ClipData.newPlainText("wtf", s);
      cbcm.setPrimaryClip(clip);
    }
  });
}
void paste(Drawable rec) {
  clipRec = rec;
  getActivity().runOnUiThread(new Runnable() {
    public void run() {
      prepareClip();
      if (cbcm.hasPrimaryClip()) {
        ClipData clip = cbcm.getPrimaryClip();
        gottenClip = clip.getItemAt(0).coerceToText(cba).toString();
      }
    }
  });
}
void handleCoded(int keyCode) {
  if (ctrl) {
    if (keyCode == android.view.KeyEvent.KEYCODE_C) textInput.special("copy");
    if (keyCode == android.view.KeyEvent.KEYCODE_V) textInput.special("paste");
    if (keyCode == android.view.KeyEvent.KEYCODE_Z) textInput.special("undo");
    if (keyCode == android.view.KeyEvent.KEYCODE_Y) textInput.special("redo");
    if (keyCode == android.view.KeyEvent.KEYCODE_X) textInput.special("cut");
    if (keyCode == android.view.KeyEvent.KEYCODE_A) textInput.special("sall");
    if (keyCode == android.view.KeyEvent.KEYCODE_S) textInput.special("save");
    if (keyCode == android.view.KeyEvent.KEYCODE_ENTER) textInput.special("eval");
    
  }
       if (keyCode == android.view.KeyEvent.KEYCODE_DEL       ) textInput.ldelete();
  else if (keyCode == android.view.KeyEvent.KEYCODE_DPAD_UP   ) textInput.special("up");
  else if (keyCode == android.view.KeyEvent.KEYCODE_DPAD_LEFT ) textInput.special("left");
  else if (keyCode == android.view.KeyEvent.KEYCODE_DPAD_DOWN ) textInput.special("down");
  else if (keyCode == android.view.KeyEvent.KEYCODE_DPAD_RIGHT) textInput.special("right");
  else if (keyCode == android.view.KeyEvent.KEYCODE_MOVE_HOME ) textInput.special("home");
  else if (keyCode == android.view.KeyEvent.KEYCODE_MOVE_END  ) textInput.special("end");
}
KeyEvent fixKE(KeyEvent e) {
  android.view.KeyEvent n = (android.view.KeyEvent) e.getNative();
  return new KeyEvent(e.getNative(), e.getMillis(), e.getAction(), 
    (n.isShiftPressed() ? Event.SHIFT : 0) +
    (n. isCtrlPressed() ? Event.CTRL  : 0) +
    (n. isMetaPressed() ? Event.META  : 0) +
    (n.  isAltPressed() ? Event.ALT   : 0)
  , e.getKey(), e.getKeyCode());
}

import processing.a2d.*;
static void beginClip(PGraphics g, float x, float y, float x2, float y2) {
  if (g instanceof PGraphicsAndroid2D) ((PGraphicsAndroid2D)g).canvas.save();
  g.clip(x, y, x2, y2);
}
static void endClip(PGraphics g) {
  if (g instanceof PGraphicsAndroid2D) ((PGraphicsAndroid2D)g).canvas.restore();
  else g.noClip();
}

static class PWindow extends PApplet {
  void create(int[] sz, boolean smooth, P5Impl u, WP5Disp d) { throw new Error("stub"); }
  void close() { throw new Error("stub"); }
}

static int fixMouseButton(MouseEvent e, int mb) {
  return mb;
}

static Value modArray(KeyEvent e) {
  return boolarr(false, false, false, false, false);
}
static String getKeyCodeText(int code) {
  return new String(Character.toChars(code));
}

//*/


//import java.awt.Color;
//import java.awt.RenderingHints;
//import java.awt.Graphics2D;
//import java.util.Map;
static void textS(PGraphics g, String s, float x, float y) {
  //if (g.getNative() instanceof Graphics2D) {
  //  Graphics2D g2d = (Graphics2D) g.getNative();
  //  g2d.setRenderingHint(
  //    RenderingHints.KEY_ANTIALIASING,
  //    RenderingHints.VALUE_ANTIALIAS_OFF);
  //  g2d.setRenderingHint(
  //    RenderingHints.KEY_TEXT_ANTIALIASING,
  //    RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HBGR);
  //  g2d.setColor(new Color(g.fillColor));
  //  g2d.drawString(s, (int)x, (int)(y+g.textSize));
  //} else {
    g.text(s, x, y + (MOBILE? g.textSize*.2 : 0)); // .333
  //}
}
