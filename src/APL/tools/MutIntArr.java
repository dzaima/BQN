package APL.tools;

import APL.types.arrs.IntArr;

import java.util.Arrays;

public class MutIntArr {
  public int[] is;
  public int sz;
  public int pos; // arbitrary variable for personal usage
  public MutIntArr(int initial) {
    is = new int[initial];
  }
  
  public void add(int i) {
    if (sz>=is.length) is = Arrays.copyOf(is, is.length*2);
    is[sz] = i;
    sz++;
  }
  public IntArr get() {
    return new IntArr(Arrays.copyOf(is, sz));
  }
}
