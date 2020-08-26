package APL.types.functions.builtins.dops;

import APL.Main;
import APL.tools.Indexer;
import APL.types.*;
import APL.types.arrs.DoubleArr;
import APL.types.functions.*;
import APL.types.functions.builtins.fns2.LBoxUBBuiltin;

public class AtBuiltin extends Dop {
  @Override public String repr() {
    return "@";
  }
  
  public Value call(Value f, Value g, Value x, DerivedDop derv) {
    return at(f, g, x, this);
  }
  
  public static Value at(Value f, Value g, Value x, Callable blame) {
    int ia = x.ia;
    if (g instanceof Fun) {
      Value vba = g.call(x);
      boolean[] ba = new boolean[ia];
      int matchingCount = 0;
      for (int i = 0; i < ia; i++) {
        ba[i] = Main.bool(vba.get(i));
        if (ba[i]) matchingCount++;
      }
      Value fa;
      if (f instanceof Fun) {
        Value[] matching = new Value[matchingCount];
        int ptr = 0;
        for (int i = 0; i < ia; i++) {
          if (ba[i]) matching[ptr++] = x.get(i);
        }
        fa = f.call(Arr.create(matching));
      } else fa = f;
      Value[] ra = new Value[ia];
      if (fa.rank == 0) {
        Value inner = fa.get(0);
        for (int i = 0; i < ia; i++) {
          if (ba[i]) ra[i] = inner;
          else ra[i] = x.get(i);
        }
      } else {
        int ptr = 0;
        for (int i = 0; i < ia; i++) {
          if (ba[i]) ra[i] = fa.get(ptr++);
          else ra[i] = x.get(i);
        }
      }
      return Arr.create(ra, x.shape);
    } else {
      
      Indexer.PosSh poss = Indexer.poss(g, x.shape, blame);
      Value repl;
      if (f instanceof Fun) {
        Value arg = LBoxUBBuiltin.on(poss, x);
        repl = f.call(arg);
      } else {
        repl = f;
      }
      return with(x, poss, repl, blame);
    }
  }
  
  public static Value with(Value o, Indexer.PosSh poss, Value n, Callable blame) {
    if (o.quickDoubleArr() && n.quickDoubleArr()) {
      double[] res = o.asDoubleArrClone();
      int[] is = poss.vals;
      if (n.rank == 0) {
        double f0 = n.first().asDouble();
        // noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < is.length; i++) res[is[i]] = f0;
      } else {
        double[] nd = n.asDoubleArr();
        Arr.eqShapes(n.shape, poss.sh, blame);
        for (int i = 0; i < is.length; i++) res[is[i]] = nd[i];
      }
      return o.rank==0? Num.of(res[0]) : new DoubleArr(res, o.shape);
    }
    Value[] res = o.valuesClone();
    int[] is = poss.vals;
    if (n.rank == 0) {
      Value f0 = n.first();
      // noinspection ForLoopReplaceableByForEach
      for (int i = 0; i < is.length; i++) res[is[i]] = f0;
    } else {
      Arr.eqShapes(n.shape, poss.sh, blame);
      for (int i = 0; i < is.length; i++) res[is[i]] = n.get(i);
    }
    return Arr.create(res, o.shape);
  }
}