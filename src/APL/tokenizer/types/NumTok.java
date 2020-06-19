package APL.tokenizer.types;

import APL.tokenizer.Token;
import APL.types.Num;

public class NumTok extends Token {
  public final Num val;
  
  public NumTok(String line, int spos, int epos, double d) {
    super(line, spos, epos);
    this.val = new Num(d);
    val.token = this;
    type = 'a';
  }
  
  @Override public String toTree(String p) {
    return p+"num : " + val + "\n";
  }
  
  @Override public String toRepr() {
    return source();
  }
}