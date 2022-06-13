package dzaima;

public abstract class SubTk extends Tk {
  int s, e;
  String t;
  public SubTk(String t, int s, int e) {
    this.t = t;
    this.s = s;
    this.e = e;
  }
  public String src() {
    return t.substring(s, e);
  }
}
