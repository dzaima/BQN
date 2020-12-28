package APL.tokenizer.types;

import APL.types.Num;

public class NumTok extends ConstTok {
  
  public NumTok(String line, int spos, int epos, double d) {
    super(line, spos, epos, Num.of(d));
  }
  
  @Override public String toTree(String p) {
    return p+"num : " + val + "\n";
  }
  
  @Override public String toRepr() {
    return source();
  }
}