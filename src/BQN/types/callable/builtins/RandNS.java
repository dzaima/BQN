package BQN.types.callable.builtins;

import BQN.errors.*;
import BQN.tools.FmtInfo;
import BQN.types.*;
import BQN.types.arrs.*;
import BQN.types.mut.SimpleMap;

import java.util.*;

public class RandNS extends SimpleMap {
  public String ln(FmtInfo f) { return "(random generator)"; }
  
  private final Random r;
  public RandNS(long seed) {
    r = new Random(seed);
  }
  
  private final Value range = new RB("Range") {
    public Value call(Value x) {
      int xi = x.asInt();
      if (xi==0) return new Num(r.nextDouble());
      return new Num(r.nextInt(xi));
    }
    public Value call(Value w, Value x) {
      int[] wi = w.asIntArr();
      int xv = x.asInt();
      int ia = 1;
      for (int i : wi) ia*= i;
      if (xv==0) {
        double[] ra = new double[ia];
        for (int i = 0; i < ia; i++) ra[i] = r.nextDouble();
        return new DoubleArr(ra, wi);
      } else {
        int[] ra = new int[ia];
        for (int i = 0; i < ia; i++) ra[i] = r.nextInt(xv);
        return new IntArr(ra, wi);
      }
    }
  };
  
  private final Value deal = new RB("Deal") {
    public Value call(Value x) {
      return call(x,x);
    }
    public Value call(Value w, Value x) {
      int wi = w.asInt();
      int xi = x.asInt();
      
      ArrayList<Integer> vs = new ArrayList<>(w.ia);
      for (int i = 0; i < xi; i++) vs.add(i);
      Collections.shuffle(vs, r);
      int[] res = new int[wi];
      for (int i = 0; i < wi; i++) res[i] = vs.get(i);
      return new IntArr(res);
    }
  };
  
  public Value getv(String s) {
    switch (s) {
      case "range": return range;
      case "deal": return deal;
    }
    throw new ValueError("No key "+s+" in random generator");
  }
  
  public void setv(String s, Value v) {
    throw new DomainError("Assigning into random generator");
  }
  
  private abstract class RB extends FnBuiltin {
    public final String name;
    public RB(String name) {
      this.name = "(random generator)."+name;
    }
    public String ln(FmtInfo f) { return name; }
  }
}