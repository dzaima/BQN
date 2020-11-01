package APL.tools;

import APL.errors.DomainError;
import APL.types.*;
import APL.types.arrs.ChrArr;

import java.util.HashSet;

public class FmtInfo {
  public static FmtInfo dbg = new FmtInfo(99, 10, 10);
  
  public final int pp;
  public final int pns, pne; // use positional notation in (10*-pns)…(10*pne)
  public final HashSet<Value> v = new HashSet<>();
  
  public FmtInfo(int pp, int pns, int pne) {
    this.pp = pp;
    this.pns = pns;
    this.pne = pne;
  }
  
  public static String fmt(Value v) {
    if (v.r()==1) {
      if (v instanceof ChrArr) return ((ChrArr) v).s;
      StringBuilder b = new StringBuilder();
      for (Value c : v) b.append(c.asString());
      return b.toString();
    } else {
      if (v instanceof ChrArr) return Format.outputFmt(v);
      int h = v.shape[0];
      int w = v.shape[1];
      StringBuilder b = new StringBuilder();
      for (int y = 0; y < h; y++) {
        if (y!=0) b.append("\n");
        for (int x = 0; x < w; x++) b.append(v.get(y*w + x).asString());
      }
      return b.toString();
    }
  }
  
  
  
  public FmtInfo with(int pp, int pns, int pne) {
    if (pp < 1) throw new DomainError("Significant digit count must be ≥1");
    return new FmtInfo(pp, pns, pne);
  }
  public FmtInfo with(int[] args) {
    if (args.length==1) return with(args[0], pns, pne);
    else if (args.length==2) return with(args[0], args[1], args[1]);
    else if (args.length==3) return with(args[0], args[1], args[2]);
    else throw new DomainError("Cannot use "+args.length+"-item vector as formatting specification");
  }
}
