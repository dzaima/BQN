package BQN.tools;


import BQN.Main;
import BQN.errors.*;
import BQN.types.*;
import BQN.types.arrs.EmptyArr;

import java.util.*;

public final class Indexer implements Iterable<int[]>, Iterator<int[]> {
  private final int[] shape;
  private final int rank;
  private final int[] c;
  private final int ia;
  private int ci = 0;
  
  private final int[] offsets;
  private static int[] zeroOffsets = new int[4];
  
  public Indexer(int[] sh, int[] offsets) { // offsets can be larger than sh 
    shape = sh;
    rank = sh.length;
    this.offsets = offsets;
    c = Arrays.copyOf(offsets, sh.length);
    long tia = 1;
    for (int i : sh) { tia*= i; if (tia<0) throw new DomainError("Array shape too big"); }
    ia = (int)tia;
  }
  public Indexer(int[] sh) {
    shape = sh;
    rank = sh.length;
    c = new int[sh.length];
    if (sh.length < zeroOffsets.length) offsets = zeroOffsets;
    else zeroOffsets = this.offsets = new int[sh.length];
    long tia = 1;
    for (int i : sh) { tia*= i; if (tia>Integer.MAX_VALUE) throw new DomainError("Array shape too big"); }
    ia = (int)tia;
  }
  
  public int pos() {
    return ci-1;
  }
  
  public boolean hasNext() {
    return ci < ia;
  }
  public int[] next() {
    if (ci > 0) {
      c[rank - 1]++;
      int dim = rank - 1;
      while (c[dim] == shape[dim]+offsets[dim]) {
        if (dim == 0) break;
        c[dim] = offsets[dim];
        c[dim - 1]++;
        dim--;
      }
    }
    ci++;
    return c;
  }
  
  public static int[] add(int[] a, int b) {
    int[] res = new int[a.length];
    for (int i = 0; i < res.length; i++) res[i] = a[i] + b;
    return res;
  }
  public static int[] sub(int[] a, int b) {
    int[] res = new int[a.length];
    for (int i = 0; i < res.length; i++) res[i] = a[i] - b;
    return res;
  }
  
  public static int[] sub(int[] a, int[] b) {
    int[] res = new int[a.length];
    for (int i = 0; i < res.length; i++) res[i] = a[i] - b[i];
    return res;
  }
  public static int[] add(int[] a, int[] b) {
    int[] res = new int[a.length];
    for (int i = 0; i < res.length; i++) res[i] = a[i] + b[i];
    return res;
  }
  
  public static int fromShape(int[] shape, int[] pos) {
    int x = 0;
    for (int i = 0; i < shape.length; i++) {
      x+= pos[i];
      if (i != shape.length-1) x*= shape[i+1];
    }
    return x;
  }
  public static int fromShapeChk(int[] sh, int[] pos, Callable blame) {
    if (sh.length != pos.length) throw new RankError(blame+": indexing at wrong rank (shape ≡ "+Main.fArr(sh)+"; pos ≡ "+Main.fArr(pos)+")", blame);
    int x = 0;
    for (int i = 0; i < sh.length; i++) {
      int c = pos[i];
      x+= c;
      if (c<0 || c>=sh[i]) throw new LengthError(blame+": indexing out-of-bounds (shape ≡ "+Main.fArr(sh)+"; pos ≡ "+Main.fArr(pos)+")", blame);
      if (i != sh.length-1) x*= sh[i+1];
    }
    return x;
  }
  public static int fromShapeChk(int[] sh, Value pos, Callable blame) {
    if (pos.r() > 1) throw new DomainError(blame+": index rank should be ≤1 (shape ≡ "+Main.fArr(pos.shape)+")", blame);
    if (sh.length != pos.ia) throw new RankError(blame+": indexing at wrong rank (shape ≡ "+Main.fArr(sh)+"; pos ≡ "+pos+")", blame);
    int x = 0;
    int[] ds = pos.asIntArr();
    for (int i = 0; i < sh.length; i++) {
      int c = ds[i];
      x+= c;
      if (c<0 || c>=sh[i]) throw new LengthError(blame+": indexing out-of-bounds (shape ≡ "+Main.fArr(sh)+"; pos ≡ "+pos+")", blame);
      if (i != sh.length-1) x*= sh[i+1];
    }
    return x;
  }
  
  public static class PosSh { // multiple results ._.
    public final int[] vals;
    public final int[] sh;
    public PosSh(int[] vals, int[] sh) {
      this.vals = vals;
      this.sh = sh;
    }
  }
  
  // checks for rank & bound errors
  // •VI←1 and sh.length≡1 allows for a shortcut of items (1 2 3 ←→ <1 2 3)
  public static PosSh poss(Value v, int[] ish, Callable blame) {
    // if (v instanceof Primitive) return new PosSh(new int[]{v.asInt()}, Rank0Arr.SHAPE);
    if (Main.vind) { // •VI←1
      boolean deep = false;
      int[] rsh = null;
      if (!(v.quickDepth1())) {
        for (Value c : v) {
          if (!(c instanceof Primitive)) {
            if (!deep) {
              rsh = c.shape;
              deep = true;
            } else Arr.eqShapes(c.shape, rsh, blame);
          }
        }
      }
      if (v.r() > 1) throw new RankError(blame+": rank of indices must be 1 (shape ≡ "+Main.fArr(v.shape)+")", blame);
      if (!(!deep && ish.length==1) && ish.length!=v.ia) throw new LengthError(blame+": amount of index parts should equal rank ("+v.ia+" index parts, shape ≡ "+Main.fArr(ish)+")", blame);
      if (!deep) { // either the rank==1 case or a single position
        int[] res = v.asIntArr();
        if (ish.length == 1) return new PosSh(res, new int[]{res.length});
        return new PosSh(new int[]{fromShapeChk(ish, res, blame)}, EmptyArr.NOINTS);
      }
      
      int[] res = new int[Arr.prod(rsh)];
      for (int i = 0; i < v.ia; i++) {
        Value c = v.get(i);
        if (c instanceof Primitive) {
          int n = c.asInt();
          if (n<0 || n>=ish[i]) throw new LengthError(blame+": indexing out-of-bounds (shape ≡ "+Main.fArr(ish)+"; pos["+i+"] ≡ "+c+")", blame);
          for (int j = 0; j < res.length; j++) res[j]+= n;
        } else {
          int[] ns = c.asIntArr();
          for (int j = 0; j < ns.length; j++) {
            int n = ns[j];
            n-= 0;
            res[j]+= n;
            if (n<0 || n>=ish[i]) throw new LengthError(blame+": indexing out-of-bounds (shape ≡ "+Main.fArr(ish)+"; pos["+i+"] ≡ "+n+")", blame);
          }
        }
        if (i != v.ia-1) {
          for (int j = 0; j < res.length; j++) res[j]*= ish[i+1];
        }
      }
      return new PosSh(res, rsh);
    } else { // •VI←0
      int[] rsh = v.shape;
      if (v.quickDoubleArr()) {
        int[] res = v.asIntArr();
        if (v.ia == 0) return new PosSh(EmptyArr.NOINTS, rsh);
        if (ish.length != 1) throw new RankError(blame+": indexing at rank 1 (indexing by scalars in shape "+Main.fArr(ish)+" array)", blame);
        for (int c : res) if (c<0 || c>=ish[0]) throw new LengthError(blame+": indexing out-of bounds (shape ≡ "+rsh[0]+"; pos ≡ " + c + ")", blame);
        return new PosSh(res, rsh);
      }
      
      int[] res = new int[v.ia];
      for (int i = 0; i < v.ia; i++) res[i] = fromShapeChk(ish, v.get(i), blame);
      return new PosSh(res, rsh);
    }
  }
  
  // v is an index vector
  // checks for rank & bound errors and returns a ravel index
  public static int vec(Value v, int[] ish, Callable blame) {
    if (ish.length!=v.ia) throw new LengthError(blame+": amount of index parts should equal rank ("+v.ia+" index parts, shape ≡ "+Main.fArr(ish)+")", blame);
    if (v.r() != 1) throw new RankError(blame+": rank of indices must be 1 (shape ≡ "+Main.fArr(v.shape)+")", blame);
    int ind = 0;
    int[] vi = v.asIntArr();
    for (int i = 0; i < v.ia; i++) {
      int n = vi[i];
      if (n<0) n+= ish[i];
      if (n<0 || n>=ish[i]) throw new LengthError(blame+": indexing out-of-bounds (shape ≡ "+Main.fArr(ish)+"; pos["+i+"] ≡ "+n+")", blame);
      ind = ish[i]*ind + n;
    }
    return ind;
  }
  
  // same, with scalar index into vector
  public static int scal(int n, int[] ish, Callable blame) {
    if (ish.length!=1) throw new LengthError(blame+": amount of index parts should equal rank (scalar index, shape ≡ "+Main.fArr(ish)+")", blame);
    int o = n;
    if (n<0) n+= ish[0];
    if (n<0 || n>=ish[0]) throw new LengthError(blame+": indexing out-of-bounds (shape ≡ "+Main.fArr(ish)+"; pos ≡ "+Num.formatInt(o)+")", blame);
    return n;
  }
  
  // ↑ but with ish unwrapped
  public static int scal(int n, int am, Callable blame) {
    int o = n;
    if (n<0) n+= am;
    if (n<0 || n>=am) throw new LengthError(blame+": indexing out-of-bounds (getting "+Num.formatInt(o)+" from "+am+")", blame);
    return n;
  }
  
  public Iterator<int[]> iterator() {
    return this;
  }
}