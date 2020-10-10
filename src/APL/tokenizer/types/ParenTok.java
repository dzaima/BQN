package APL.tokenizer.types;

import APL.tokenizer.Token;

public class ParenTok extends Token {
  public final Token ln;
  
  public ParenTok(String line, int spos, int epos, Token ln) {
    super(line, spos, epos);
    this.ln = ln;
  }
  
  @Override public String toRepr() {
    return "(" + ln.toRepr() + ")";
  }
}