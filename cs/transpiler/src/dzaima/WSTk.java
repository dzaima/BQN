package dzaima;

public class WSTk extends SubTk {
  public static Tk ln = new WSTk("\n");
  public static Tk sp = new WSTk(" ");
  
  public WSTk(String t, int s, int e) {
    super(t, s, e);
  }
  
  public WSTk(String s) {
    super(s, 0, s.length());
  }
}
