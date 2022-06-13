package dzaima;

public class ParTk extends ArrTk {
  public ParTk(Vec<Tk> tks) {
    super(tks);
  }
  
  public String src() {
    return "("+ct()+")";
  }
  
  public ArrTk mod(Vec<Tk> n) {
    return new ParTk(n);
  }
}
