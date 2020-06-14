package APL.tokenizer.types;

import APL.tokenizer.Token;

public class ParenTok extends Token {
  public final LineTok ln;
  
  public ParenTok(String line, int spos, int epos, LineTok ln) {
    super(line, spos); end(epos);
    this.ln = ln;
  }
  
  @Override public String toRepr() {
    return "(" + ln.toRepr() + ")";
  }
}