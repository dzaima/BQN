package APL.tools;

import APL.types.*;
import APL.types.arrs.ChrArr;

import java.util.HashSet;

public class FmtInfo {
  public static FmtInfo dbg = new FmtInfo(99, -10, 10);
  
  public final int pp;
  public final int scs, sce;
  public final HashSet<Value> v = new HashSet<>();
  
  public FmtInfo(int pp, int scs, int sce) {
    this.pp = pp;
    this.scs = scs;
    this.sce = sce;
  }
  
  
  public static String tmp(Value v) {
    return fmt(v.pretty(new FmtInfo(Num.pp, Num.eEr, Num.sEr)));
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
}
